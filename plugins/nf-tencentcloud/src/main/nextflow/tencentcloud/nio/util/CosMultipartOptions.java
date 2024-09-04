package nextflow.tencentcloud.nio.util;

@SuppressWarnings("unchecked")
public class CosMultipartOptions {

    public static final int DEFAULT_CHUNK_SIZE = 100 << 20;  // 100 MiB

    public static final int DEFAULT_BUFFER_SIZE = 10485760;

    public static final long DEFAULT_MAX_COPY_SIZE = 5_000_000_000L;

    public static final int DEFAULT_MAX_ATTEMPTS = 5;

    public static final int DEFAULT_RETRY_SLEEP = 500;

    private final int chunkSize;

    private final int maxThreads;

    private final int bufferSize;

    private final long maxCopySize;

    private final int maxAttempts;

    private final long retrySleep;


    {
        chunkSize = DEFAULT_CHUNK_SIZE;
        maxThreads = Runtime.getRuntime().availableProcessors() * 3;
        bufferSize = DEFAULT_BUFFER_SIZE;
        maxCopySize = DEFAULT_MAX_COPY_SIZE;
        maxAttempts = DEFAULT_MAX_ATTEMPTS;
        retrySleep = DEFAULT_RETRY_SLEEP;
    }

    public CosMultipartOptions() {

    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getRetrySleep() {
        return retrySleep;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public long getMaxCopySize() {
        return maxCopySize;
    }

    @Override
    public String toString() {
        return "chunkSize=" + chunkSize
                + "; maxThreads=" + maxThreads
                + "; maxAttempts=" + maxAttempts
                + "; retrySleep=" + retrySleep;
    }

}
