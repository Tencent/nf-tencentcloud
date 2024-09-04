package nextflow.tencentcloud.nio;

import static java.util.Objects.requireNonNull;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.AbortMultipartUploadRequest;
import com.qcloud.cos.model.COSObjectId;
import com.qcloud.cos.model.CompleteMultipartUploadRequest;
import com.qcloud.cos.model.InitiateMultipartUploadRequest;
import com.qcloud.cos.model.InitiateMultipartUploadResult;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PartETag;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.SSEAlgorithm;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.model.UploadPartRequest;
import com.qcloud.cos.utils.Base64;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;
import nextflow.tencentcloud.nio.util.ByteBufferInputStream;
import nextflow.tencentcloud.nio.util.CosMultipartOptions;
import nextflow.util.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CosOutputStream extends OutputStream {
    private static final Logger log = LoggerFactory.getLogger(CosOutputStream.class);

    private static final int MIN_MULTIPART_UPLOAD = 5 * 1024 * 1024;
    private final COSClient cosClient;
    private final COSObjectId objectId;
    private StorageClass storageClass;
    private SSEAlgorithm storageEncryption;
    private String contentType;
    private volatile boolean closed;
    private volatile boolean aborted;
    private volatile String uploadId;
    private Queue<PartETag> partETags;
    private final CosMultipartOptions request;
    private final Queue<ByteBuffer> bufferPool = new ConcurrentLinkedQueue<ByteBuffer>();
    private ExecutorService executor;
    private ByteBuffer buf;
    private MessageDigest md5;
    private Phaser phaser;
    private int partsCount;
    private final int bufferSize;
    private final AtomicInteger bufferCounter = new AtomicInteger();

    public CosOutputStream(final COSClient cosClient, COSObjectId objectId, CosMultipartOptions request) {
        this.cosClient = requireNonNull(cosClient);
        this.objectId = requireNonNull(objectId);
        this.request = request;
        this.bufferSize = request.getBufferSize();
    }

    private ByteBuffer expandBuffer(ByteBuffer byteBuffer) {
        final float expandFactor = 2.5f;
        final int newCapacity = Math.min((int) (byteBuffer.capacity() * expandFactor), bufferSize);
        ((java.nio.Buffer) byteBuffer).flip();
        ByteBuffer expanded = ByteBuffer.allocate(newCapacity);
        expanded.order(byteBuffer.order());
        expanded.put(byteBuffer);
        return expanded;
    }

    public CosOutputStream setStorageClass(String storageClass) {
        if (storageClass != null) {
            this.storageClass = StorageClass.fromValue(storageClass);
        }
        return this;
    }

    public CosOutputStream setContentType(String type) {
        this.contentType = type;
        return this;
    }

    private MessageDigest createMd5() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot find a MD5 algorithm provider", e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (closed) {
            throw new IOException("Can't write into a closed stream");
        }
        if (buf == null) {
            buf = allocate();
            md5 = createMd5();
        } else if (!buf.hasRemaining()) {
            if (buf.position() < bufferSize) {
                buf = expandBuffer(buf);
            } else {
                flush();
                // create a new buffer
                buf = allocate();
                md5 = createMd5();
            }
        }

        buf.put((byte) b);
        // update the md5 checksum
        md5.update((byte) b);
    }

    @Override
    public void flush() throws IOException {
        if (uploadBuffer(buf, false)) {
            buf = null;
            md5 = null;
        }
    }

    private ByteBuffer allocate() {

        if (partsCount == 0) {
            final int initialSize = 100 * 1024;
            return ByteBuffer.allocate(initialSize);
        }

        ByteBuffer result = bufferPool.poll();
        if (result != null) {
            result.clear();
        } else {
            log.debug("Allocating new buffer of {} bytes, total buffers {}",
                    bufferSize, bufferCounter.incrementAndGet());
            result = ByteBuffer.allocate(bufferSize);
        }

        return result;
    }

    private boolean uploadBuffer(ByteBuffer buf, boolean last) throws IOException {
        if (buf == null || buf.position() == 0) {
            return false;
        }

        if (buf.position() < MIN_MULTIPART_UPLOAD && !last) {
            return false;
        }

        if (partsCount == 0) {
            init();
        }

        // set the buffer in read mode and submit for upload
        executor.submit(task(buf, md5.digest(), ++partsCount));

        return true;
    }

    private void init() throws IOException {
        uploadId = initiateMultipartUpload().getUploadId();
        if (uploadId == null) {
            throw new IOException("Failed to get a valid multipart upload ID from Tencent Cos");
        }
        executor = getOrCreateExecutor(request.getMaxThreads());
        partETags = new LinkedBlockingQueue<>();
        phaser = new Phaser();
        phaser.register();
        log.trace("[Cos phaser] Register - Starting Cos upload: {}; chunk-size: {}; max-threads: {}",
                uploadId, bufferSize, request.getMaxThreads());
    }


    private Runnable task(final ByteBuffer buffer, final byte[] checksum, final int partIndex) {
        phaser.register();
        log.trace("[Cos phaser] Task register");
        return new Runnable() {
            @Override
            public void run() {
                try {
                    uploadPart(buffer, checksum, partIndex, false);
                } catch (IOException e) {
                    final StringWriter writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    log.error("Upload: {} > Error for part: {}\nCaused by: {}", uploadId, partIndex, writer.toString());
                } finally {
                    log.trace("[Cos phaser] Task arriveAndDeregisterphaser");
                    phaser.arriveAndDeregister();
                }
            }
        };

    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }

        if (uploadId == null) {
            if (buf != null) {
                putObject(buf, md5.digest());
            } else {
                // this is needed when trying to upload an empty
                putObject(new ByteArrayInputStream(new byte[]{}), 0, createMd5().digest());
            }
        } else {
            // -- upload remaining chunk
            if (buf != null) {
                uploadBuffer(buf, true);
            }

            // -- shutdown upload executor and await termination
            log.trace("[Cos phaser] Close arriveAndAwaitAdvance");
            phaser.arriveAndAwaitAdvance();

            // -- complete upload process
            completeMultipartUpload();
        }

        closed = true;
    }

    private InitiateMultipartUploadResult initiateMultipartUpload() throws IOException {
        final InitiateMultipartUploadRequest request = //
                new InitiateMultipartUploadRequest(objectId.getBucket(), objectId.getKey());
        final ObjectMetadata metadata = new ObjectMetadata();

        if (storageClass != null) {
            request.setStorageClass(storageClass);
        }

        if (storageEncryption != null) {
            metadata.setSSEAlgorithm(storageEncryption.toString());
            request.setObjectMetadata(metadata);
        }

        if (contentType != null) {
            metadata.setContentType(contentType);
            request.setObjectMetadata(metadata);
        }

        if (log.isTraceEnabled()) {
            log.trace("Cos initiateMultipartUpload {}", request);
        }

        try {
            return cosClient.initiateMultipartUpload(request);
        } catch (final CosClientException e) {
            throw new IOException("Failed to initiate Tencent Cos multipart upload", e);
        }
    }

    private void uploadPart(final ByteBuffer buf, final byte[] checksum, final int partNumber,
            final boolean lastPart) throws IOException {
        ((java.nio.Buffer) buf).flip();
        ((java.nio.Buffer) buf).mark();

        int attempt = 0;
        boolean success = false;
        try {
            while (!success) {
                attempt++;
                int len = buf.limit();
                try {
                    log.trace("Uploading part {} with length {} attempt {} for {} ",
                            partNumber, len, attempt, objectId);
                    uploadPart(new ByteBufferInputStream(buf), len, checksum, partNumber, lastPart);
                    success = true;
                } catch (CosClientException | IOException e) {
                    if (attempt == request.getMaxAttempts()) {
                        throw new IOException("Failed to upload multipart data to Tencent Cos", e);
                    }

                    log.debug("Failed to upload part {} attempt {} for {} -- Caused by: {}",
                            partNumber, attempt, objectId, e.getMessage());
                    sleep(request.getRetrySleep());
                    buf.reset();
                }
            }
        } finally {
            if (!success) {
                closed = true;
                abortMultipartUpload();
            }
            bufferPool.offer(buf);
        }

    }

    private void uploadPart(final InputStream content, final long contentLength,
            final byte[] checksum, final int partNumber, final boolean lastPart)
            throws IOException {

        if (aborted) {
            return;
        }

        final UploadPartRequest request = new UploadPartRequest();
        request.setBucketName(objectId.getBucket());
        request.setKey(objectId.getKey());
        request.setUploadId(uploadId);
        request.setPartNumber(partNumber);
        request.setPartSize(contentLength);
        request.setInputStream(content);
        request.setLastPart(lastPart);
        request.setMd5Digest(Base64.encodeAsString(checksum));

        final PartETag partETag = cosClient.uploadPart(request).getPartETag();
        log.trace("Uploaded part {} with length {} for {}: {}",
                partETag.getPartNumber(), contentLength, objectId, partETag.getETag());
        partETags.add(partETag);

    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.trace("Sleep was interrupted -- Cause: {}", e.getMessage());
        }
    }

    private synchronized void abortMultipartUpload() {
        if (aborted) {
            return;
        }

        log.debug("Aborting multipart upload {} for {}", uploadId, objectId);
        try {
            cosClient.abortMultipartUpload(new AbortMultipartUploadRequest(
                    objectId.getBucket(), objectId.getKey(), uploadId));
        } catch (final CosClientException e) {
            log.warn("Failed to abort multipart upload {}: {}", uploadId, e.getMessage());
        }
        aborted = true;
        log.trace("[Cos phaser] MultipartUpload arriveAndDeregister");
        phaser.arriveAndDeregister();
    }

    private void completeMultipartUpload() throws IOException {
        // if aborted upload just ignore it
        if (aborted) {
            return;
        }

        final int partCount = partETags.size();
        log.trace("Completing upload to {} consisting of {} parts", objectId, partCount);

        try {
            cosClient.completeMultipartUpload(new CompleteMultipartUploadRequest(
                    objectId.getBucket(), objectId.getKey(), uploadId, new ArrayList<>(partETags)));
        } catch (final CosClientException e) {
            throw new IOException("Failed to complete Tencent Cos multipart upload", e);
        }

        log.trace("Completed upload to {} consisting of {} parts", objectId, partCount);

        uploadId = null;
        partETags = null;
    }

    private void putObject(ByteBuffer buf, byte[] checksum) throws IOException {
        ((java.nio.Buffer) buf).flip();
        putObject(new ByteBufferInputStream(buf), buf.limit(), checksum);
    }

    private void putObject(final InputStream content, final long contentLength, byte[] checksum) throws IOException {

        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(contentLength);
        meta.setContentMD5(Base64.encodeAsString(checksum));

        final PutObjectRequest request = new PutObjectRequest(objectId.getBucket(), objectId.getKey(), content, meta);

        if (storageClass != null) {
            request.setStorageClass(storageClass);
        }

        if (storageEncryption != null) {
            meta.setSSEAlgorithm(storageEncryption.toString());
        }

        if (contentType != null) {
            meta.setContentType(contentType);
        }

        if (log.isTraceEnabled()) {
            log.trace("Cos putObject {}", request);
        }

        try {
            cosClient.putObject(request);
        } catch (final CosClientException e) {
            throw new IOException("Failed to put data into Tencent Cos object", e);
        }
    }

    private static volatile ExecutorService executorSingleton;

    static synchronized ExecutorService getOrCreateExecutor(int maxThreads) {
        if (executorSingleton == null) {
            executorSingleton = ThreadPoolManager.create("CosStreamUploader", maxThreads);
        }
        return executorSingleton;
    }
}
