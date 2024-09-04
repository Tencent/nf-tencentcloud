package nextflow.tencentcloud.nio;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.qcloud.cos.model.Bucket;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

public class CosFileSystem extends FileSystem {

    private final CosFileSystemProvider provider;
    private final CosClient client;
    private final String bucketName;

//    private static final Logger log = LoggerFactory.getLogger(CosFileSystem.class);

    public CosFileSystem(CosFileSystemProvider provider, CosClient client, URI uri) {
        this.provider = provider;
        this.client = client;
        this.bucketName = CosPath.bucketName(uri);
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() {
        this.provider.fileSystems.remove(bucketName);
    }

    @Override
    public boolean isOpen() {
        return this.provider.fileSystems.containsKey(bucketName);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return CosPath.PATH_SEPARATOR;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        ImmutableList.Builder<Path> builder = ImmutableList.builder();

        for (Bucket bucket : client.listBuckets()) {
            builder.add(new CosPath(this, bucket.getName()));
        }

        return builder.build();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return ImmutableList.of();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return ImmutableSet.of("basic");
    }

    @Override
    public Path getPath(String first, String... more) {
        if (more.length == 0) {
            return new CosPath(this, first);
        }

        return new CosPath(this, first, more);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

    public CosClient getClient() {
        return client;
    }

    public String getBucketName() {
        return bucketName;
    }
}
