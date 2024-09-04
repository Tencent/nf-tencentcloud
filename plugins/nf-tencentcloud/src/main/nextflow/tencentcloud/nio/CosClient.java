package nextflow.tencentcloud.nio;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.AccessControlList;
import com.qcloud.cos.model.Bucket;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.CopyObjectRequest;
import com.qcloud.cos.model.CopyResult;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.GetObjectTaggingRequest;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.Tag.Tag;
import com.qcloud.cos.transfer.Copy;
import com.qcloud.cos.transfer.TransferManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nextflow.extension.FilesEx;
import nextflow.tencentcloud.nio.util.CosMultipartOptions;
import nextflow.tencentcloud.omics.config.CosConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CosClient {

    private static final Logger log = LoggerFactory.getLogger(CosClient.class);

    private CosConfig config;

    private COSClient client;

    private Long uploadChunkSize = Long.valueOf(CosMultipartOptions.DEFAULT_CHUNK_SIZE);

    private Integer uploadMaxThreads = 10;

    public CosClient(CosConfig config) {
        this.client = new COSClient(config.getCredentials(), config.getClientConfig());
        this.config = config;
    }

    public CosConfig getConfig() {
        return config;
    }

    public List<Bucket> listBuckets() {
        return client.listBuckets();
    }

    public ObjectListing listObjects(ListObjectsRequest request) {
        return client.listObjects(request);
    }

    public COSObject getObject(String bucketName, String key) {
        return client.getObject(bucketName, key);
    }

    public PutObjectResult putObject(String bucket, String key, File file) {
        PutObjectRequest req = new PutObjectRequest(bucket, key, file);
        return client.putObject(req);
    }

    public PutObjectResult putObject(
            String bucket, String keyName, InputStream inputStream,
            ObjectMetadata metadata, List<Tag> tags, String contentType) {
        PutObjectRequest req =
                new PutObjectRequest(bucket, keyName, inputStream, metadata);
        if (contentType != null) {
            metadata.setContentType(contentType);
        }
        if (log.isTraceEnabled()) {
            log.trace("Cos PutObject request {}", req);
        }
        return client.putObject(req);
    }

    public void deleteObject(String bucket, String key) {
        client.deleteObject(bucket, key);
    }

    public void copyObject(CopyObjectRequest req, List<Tag> tags, String contentType, String storageClass) {
        ObjectMetadata meta = req.getNewObjectMetadata() != null
                ? req.getNewObjectMetadata() : new ObjectMetadata();
        if (contentType != null) {
            meta.setContentType(contentType);
            req.setNewObjectMetadata(meta);
        }
        if (storageClass != null) {
            req.setStorageClass(storageClass);
        }
        if (log.isTraceEnabled()) {
            log.trace("Cos CopyObject request {}", req);
        }

        client.copyObject(req);
    }

    public COSClient getClient() {
        return client;
    }

    public AccessControlList getObjectAcl(String bucketName, String key) {
        return client.getObjectAcl(bucketName, key);
    }

    public ObjectMetadata getObjectMetadata(String bucketName, String key) {
        return client.getObjectMetadata(bucketName, key);
    }

    public List<Tag> getObjectTags(String bucketName, String key) {
        return client.getObjectTagging(new GetObjectTaggingRequest(bucketName, key)).getTagSet();
    }

    public ObjectListing listNextBatchOfObjects(ObjectListing objectListing) {
        return client.listNextBatchOfObjects(objectListing);
    }

    /**
     * multipartCopyObject
     * copy file from cos to cos,in multipart mode
     *
     * @param cosSource source cos path
     * @param cosTarget target cos path
     * @param objectSize file object size
     * @param opts copy options
     * @param tags file copy tags
     * @param contentType file content type
     * @param storageClass file storage class
     */
    public void multipartCopyObject(
            CosPath cosSource, CosPath cosTarget, Long objectSize, CosMultipartOptions opts,
            List<Tag> tags, String contentType, String storageClass) {

        CosClient srcClient = cosSource.getFileSystem().getClient();
        CosClient dstClient = cosTarget.getFileSystem().getClient();
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        TransferManager transferManager = new TransferManager(dstClient.getClient(), threadPool);
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(srcClient.getConfig().getRegion(),
                cosSource.getBucket(),
                cosSource.getKey(), cosTarget.getBucket(), cosTarget.getKey());
        try {
            Copy copy = transferManager.copy(copyObjectRequest, srcClient.getClient(), null);
            // Returns an asynchronous copy result, you can synchronously
            // call waitForCopyResult to wait for the copy to finish.
            // If successful, it returns CopyResult, if it fails, it throws an exception.
            CopyResult copyResult = copy.waitForCopyResult();
        } catch (CosServiceException e) {
            e.printStackTrace();
            throw e;
        } catch (CosClientException e) {
            e.printStackTrace();
            throw e;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        transferManager.shutdownNow();

    }

    public void downloadFile(CosPath source, File target) {
        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest(source.getBucket(), source.getKey());
            ObjectMetadata objectMetadata = client.getObject(getObjectRequest, target);
        } catch (Exception e) {
            log.debug("Cos download file: cos://{}/{} interrupted", source.getBucket(), source.getKey());
            throw e;
        }
    }

    /**
     * downloadDirectory
     * download cos path to file io
     *
     * @param source cos source path
     * @param targetFile download target file
     * @throws IOException io exception
     */
    public void downloadDirectory(CosPath source, File targetFile) throws IOException {
        final Path target = targetFile.toPath();

        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {

            public FileVisitResult preVisitDirectory(Path current, BasicFileAttributes attr)
                    throws IOException {
                // get the *delta* path against the source path
                Path rel = source.relativize(current);
                String delta = rel != null ? rel.toString() : null;
                Path newFolder = delta != null ? target.resolve(delta) : target;
                if (log.isTraceEnabled()) {
                    log.trace("Copy DIR: " + current + " -> " + newFolder);
                }
                // this `copy` creates the new folder, but does not copy the contained files
                Files.createDirectory(newFolder);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path current, BasicFileAttributes attr) {
                // get the *delta* path against the source path
                Path rel = source.relativize(current);
                String delta = rel != null ? rel.toString() : null;
                Path newFile = delta != null ? target.resolve(delta) : target;
                if (log.isTraceEnabled()) {
                    log.trace("Copy file: " + current + " -> " + FilesEx.toUriString(newFile));
                }

                downloadFile((CosPath) current, newFile.toFile());

                return FileVisitResult.CONTINUE;
            }

        };

        Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, visitor);

    }

    public void uploadFile(File source, CosPath target) {
        PutObjectRequest req = new PutObjectRequest(target.getBucket(), target.getKey(), source);
        // initiate transfer
        // await for completion
        try {
            PutObjectResult upload = client.putObject(req);
        } catch (Exception e) {
            log.debug("Cos upload file: cos://{}/{} interrupted", target.getBucket(), target.getKey());
            Thread.currentThread().interrupt();
        }
    }

    private final ThreadLocal<List<Tag>> uploadTags = new ThreadLocal<>();

    public void uploadDirectory(File source, CosPath target) {
        for (File file : source.listFiles()) {
            CosPath t = (CosPath) target.resolve(file.getName());
            if (file.isFile()) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(t.getBucket(), t.getKey(), file);
                try {
                    client.putObject(putObjectRequest);
                } catch (Exception e) {
                    log.debug("Cos upload file: cos://{}/{} interrupted", t.getBucket(), t.getKey());
                    Thread.currentThread().interrupt();
                }
                System.out.println("Uploaded: " + file.getAbsolutePath() + " to " + t.toUri());
            } else if (file.isDirectory()) {
                uploadDirectory(file, t);
            }
        }
    }

}
