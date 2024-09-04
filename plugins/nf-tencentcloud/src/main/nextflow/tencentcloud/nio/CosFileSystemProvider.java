package nextflow.tencentcloud.nio;

import static com.google.common.collect.Sets.difference;
import static java.lang.String.format;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.AccessControlList;
import com.qcloud.cos.model.COSObjectId;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.CopyObjectRequest;
import com.qcloud.cos.model.Grant;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.Owner;
import com.qcloud.cos.model.Permission;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.model.Tag.Tag;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import nextflow.extension.FilesEx;
import nextflow.file.CopyOptions;
import nextflow.file.FileHelper;
import nextflow.file.FileSystemTransferAware;
import nextflow.tencentcloud.nio.util.CosMultipartOptions;
import nextflow.tencentcloud.nio.util.CosObjectSummaryLookup;
import nextflow.tencentcloud.omics.config.CosConfig;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spec:
 *
 * URI: cos://{bucket}/{key}
 *
 * FileSystem roots: /{bucket}/
 *
 * Treatment of Cos objects: - If a key ends in "/" it's considered a directory
 * *and* a regular file. Otherwise, it's just a regular file. - It is legal for
 * a key "xyz" and "xyz/" to exist at the same time. The latter is treated as a
 * directory. - If a file "a/b/c" exists but there's no "a" or "a/b/", these are
 * considered "implicit" directories. They can be listed, traversed and deleted.
 *
 * Deviations from FileSystem provider API: - Deleting a file or directory
 * always succeeds, regardless of whether the file/directory existed before the
 * operation was issued i.e. Files.delete() and Files.deleteIfExists() are
 * equivalent.
 *
 *
 * Future versions of this provider might allow for a strict mode that mimics
 * the semantics of the FileSystem provider API on a best effort basis, at an
 * increased processing cost.
 */
public class CosFileSystemProvider extends FileSystemProvider implements FileSystemTransferAware {

    private static final Logger log = LoggerFactory.getLogger(CosFileSystemProvider.class);

    final Map<String, CosFileSystem> fileSystems = new HashMap<>();

    private final CosObjectSummaryLookup cosObjectSummaryLookup = new CosObjectSummaryLookup();

    @Override
    public String getScheme() {
        return "cos";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        Preconditions.checkNotNull(uri, "uri is null");
        Preconditions.checkArgument(uri.getScheme().equals("cos"), "uri scheme must be 'cos': '%s'", uri);

        final String bucketName = CosPath.bucketName(uri);
        synchronized (fileSystems) {
            if (fileSystems.containsKey(bucketName)) {
                throw new FileSystemAlreadyExistsException(
                        "Cos filesystem already exists. Use getFileSystem() instead");
            }

            final CosConfig cosConfig = new CosConfig(env, bucketName);
            final CosFileSystem result = createFileSystem(uri, cosConfig);
            fileSystems.put(bucketName, result);
            return result;
        }
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        final String bucketName = CosPath.bucketName(uri);
        final FileSystem fileSystem = this.fileSystems.get(bucketName);

        if (fileSystem == null) {
            throw new FileSystemNotFoundException(
                    "Cos filesystem not yet created. Use newFileSystem() instead");
        }

        return fileSystem;
    }

    /**
     * Deviation from spec: throws FileSystemNotFoundException if FileSystem
     * hasn't yet been initialized. Call newFileSystem() first.
     * Need credentials. Maybe set credentials after? how?
     */
    @Override
    public Path getPath(URI uri) {
        Preconditions.checkArgument(uri.getScheme().equals(getScheme()), "URI scheme must be %s", getScheme());
        if (uri.getHost() != null && !uri.getHost().isEmpty()) {
            return getFileSystem(uri).getPath("/" + uri.getHost(), uri.getPath());
        }
        return getFileSystem(uri).getPath(uri.getPath());
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
            throws IOException {

        Preconditions.checkArgument(dir instanceof CosPath, "path must be an instance of %s", CosPath.class.getName());
        final CosPath cosPath = (CosPath) dir;

        return new DirectoryStream<Path>() {
            @Override
            public void close() throws IOException {
                // nothing to do here
            }

            @Override
            public Iterator<Path> iterator() {
                return new CosIterator(cosPath.getFileSystem(), cosPath.getBucket(), cosPath.getKey() + "/");
            }
        };
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... options)
            throws IOException {
        Preconditions.checkArgument(options.length == 0,
                "OpenOptions not yet supported: %s",
                ImmutableList.copyOf(options)); // TODO

        Preconditions.checkArgument(path instanceof CosPath,
                "path must be an instance of %s", CosPath.class.getName());
        CosPath cosPath = (CosPath) path;

        Preconditions.checkArgument(!cosPath.getKey().equals(""),
                "cannot create InputStream for root directory: %s", FilesEx.toUriString(cosPath));

        InputStream result;
        try {
            result = cosPath
                    .getFileSystem()
                    .getClient()
                    .getObject(cosPath.getBucket(), cosPath.getKey())
                    .getObjectContent();

            if (result == null) {
                throw new IOException(String.format("The specified path is a directory: %s",
                        FilesEx.toUriString(cosPath)));
            }
        } catch (CosServiceException e) {
            if (e.getStatusCode() == 404) {
                throw new NoSuchFileException(path.toString());
            }
            // otherwise throws a generic IO exception
            throw new IOException(String.format("Cannot access file: %s", FilesEx.toUriString(cosPath)), e);
        }

        return result;
    }

    @Override
    public OutputStream newOutputStream(final Path path, final OpenOption... options) throws IOException {
        Preconditions.checkArgument(path instanceof CosPath,
                "path must be an instance of %s", CosPath.class.getName());
        CosPath cosPath = (CosPath) path;

        // validate options
        if (options.length > 0) {
            Set<OpenOption> opts = new LinkedHashSet<>(Arrays.asList(options));

            // cannot handle APPEND here -> use newByteChannel() implementation
            if (opts.contains(StandardOpenOption.APPEND)) {
                return super.newOutputStream(path, options);
            }

            if (opts.contains(StandardOpenOption.READ)) {
                throw new IllegalArgumentException("READ not allowed");
            }

            final boolean create = opts.remove(StandardOpenOption.CREATE);
            final boolean createNew = opts.remove(StandardOpenOption.CREATE_NEW);
            final boolean truncateExisting = opts.remove(StandardOpenOption.TRUNCATE_EXISTING);

            // remove irrelevant/ignored options
            opts.remove(StandardOpenOption.WRITE);
            opts.remove(StandardOpenOption.SPARSE);

            if (!opts.isEmpty()) {
                throw new UnsupportedOperationException(opts.iterator().next() + " not supported");
            }

            if (!(create && truncateExisting)) {
                if (exists(cosPath)) {
                    if (createNew || !truncateExisting) {
                        throw new FileAlreadyExistsException(FilesEx.toUriString(cosPath));
                    }
                } else {
                    if (!createNew && !create) {
                        throw new NoSuchFileException(FilesEx.toUriString(cosPath));
                    }
                }
            }
        }

        return createUploaderOutputStream(cosPath);
    }

    @Override
    public boolean canUpload(Path source, Path target) {
        return FileSystems.getDefault().equals(source.getFileSystem()) && target instanceof CosPath;
    }

    @Override
    public boolean canDownload(Path source, Path target) {
        return source instanceof CosPath && FileSystems.getDefault().equals(target.getFileSystem());
    }

    @Override
    public void download(Path remoteFile, Path localDestination, CopyOption... options) throws IOException {
        final CosPath source = (CosPath) remoteFile;

        final CopyOptions opts = CopyOptions.parse(options);
        // delete target if it exists and REPLACE_EXISTING is specified
        if (opts.replaceExisting()) {
            FileHelper.deletePath(localDestination);
        } else if (Files.exists(localDestination)) {
            throw new FileAlreadyExistsException(localDestination.toString());
        }

        final Optional<CosFileAttributes> attrs = readAttr1(source);
        final boolean isDir = attrs.isPresent() && attrs.get().isDirectory();
        final String type = isDir ? "directory" : "file";
        final CosClient cosClient = source.getFileSystem().getClient();
        log.debug("Cos download {} from={} to={}", type, FilesEx.toUriString(source), localDestination);
        if (isDir) {
            cosClient.downloadDirectory(source, localDestination.toFile());
        } else {
            cosClient.downloadFile(source, localDestination.toFile());
        }
    }

    @Override
    public void upload(Path localFile, Path remoteDestination, CopyOption... options) throws IOException {
        final CosPath target = (CosPath) remoteDestination;

        CopyOptions opts = CopyOptions.parse(options);
        LinkOption[] linkOptions = (opts.followLinks())
                ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};

        // attributes of source file
        if (Files.readAttributes(localFile, BasicFileAttributes.class, linkOptions).isSymbolicLink()) {
            throw new IOException(
                    "Uploading of symbolic links not supported - offending path: " + localFile);
        }

        final Optional<CosFileAttributes> attrs = readAttr1(target);
        final boolean exits = attrs.isPresent();

        // delete target if it exists and REPLACE_EXISTING is specified
        if (opts.replaceExisting()) {
            FileHelper.deletePath(target);
        } else if (exits) {
            throw new FileAlreadyExistsException(target.toString());
        }

        final boolean isDir = Files.isDirectory(localFile);
        final String type = isDir ? "directory" : "file";
        log.debug("Cos upload {} from={} to={}", type, localFile, FilesEx.toUriString(target));
        final CosClient cosClient = target.getFileSystem().getClient();
        if (isDir) {
            cosClient.uploadDirectory(localFile.toFile(), target);
        } else {
            cosClient.uploadFile(localFile.toFile(), target);
        }
    }

    private CosOutputStream createUploaderOutputStream(CosPath fileToUpload) {
        CosClient cosClient = fileToUpload.getFileSystem().getClient();

        final String storageClass = fileToUpload.getStorageClass() != null
                ? fileToUpload.getStorageClass() : StorageClass.Standard.toString();
        final CosMultipartOptions opts = new CosMultipartOptions();
        final COSObjectId objectId = fileToUpload.toCosObjectId();
        CosOutputStream stream = new CosOutputStream(cosClient.getClient(), objectId, opts)
                .setStorageClass(storageClass)
                .setContentType(fileToUpload.getContentType());
        return stream;
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path,
            Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {
        Preconditions.checkArgument(path instanceof CosPath,
                "path must be an instance of %s", CosPath.class.getName());
        final CosPath cosPath = (CosPath) path;
        // we resolve to a file inside the temp folder with the cospath name
        final Path tempFile = createTempDir().resolve(path.getFileName().toString());

        try {
            InputStream is = cosPath.getFileSystem().getClient()
                    .getObject(cosPath.getBucket(), cosPath.getKey())
                    .getObjectContent();

            if (is == null) {
                throw new IOException(String.format("The specified path is a directory: %s", path));
            }

            Files.write(tempFile, IOUtils.toByteArray(is));
        } catch (CosServiceException e) {
            if (e.getStatusCode() != 404) {
                throw new IOException(String.format("Cannot access file: %s", path), e);
            }
        }

        // and we can use the File SeekableByteChannel implementation
        final SeekableByteChannel seekable = Files.newByteChannel(tempFile, options);
        final List<Tag> tags = ((CosPath) path).getTagsList();
        final String contentType = ((CosPath) path).getContentType();

        return new SeekableByteChannel() {
            @Override
            public boolean isOpen() {
                return seekable.isOpen();
            }

            @Override
            public void close() throws IOException {

                if (!seekable.isOpen()) {
                    return;
                }
                seekable.close();
                // upload the content where the seekable ends (close)
                if (Files.exists(tempFile)) {
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(Files.size(tempFile));
                    metadata.setContentType(Files.probeContentType(tempFile));

                    try (InputStream stream = Files.newInputStream(tempFile)) {
                        /*
                         FIXME: if the stream is {@link InputStream#markSupported()} i can reuse the same stream
                         and evict the close and open methods of probeContentType. By this way:
                         metadata.setContentType(new Tika().detect(stream, tempFile.getFileName().toString()));
                        */
                        cosPath.getFileSystem()
                                .getClient()
                                .putObject(cosPath.getBucket(), cosPath.getKey(), stream, metadata, tags, contentType);
                    }
                } else {
                    // delete: check option delete_on_close
                    cosPath.getFileSystem().
                            getClient().deleteObject(cosPath.getBucket(), cosPath.getKey());
                }
                // and delete the temp dir
                Files.deleteIfExists(tempFile);
                Files.deleteIfExists(tempFile.getParent());
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                return seekable.write(src);
            }

            @Override
            public SeekableByteChannel truncate(long size) throws IOException {
                return seekable.truncate(size);
            }

            @Override
            public long size() throws IOException {
                return seekable.size();
            }

            @Override
            public int read(ByteBuffer dst) throws IOException {
                return seekable.read(dst);
            }

            @Override
            public SeekableByteChannel position(long newPosition)
                    throws IOException {
                return seekable.position(newPosition);
            }

            @Override
            public long position() throws IOException {
                return seekable.position();
            }
        };
    }

    /**
     * Deviations from spec: Does not perform atomic check-and-create. Since a
     * directory is just an Cos object, all directories in the hierarchy are
     * created or it already existed.
     */
    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs)
            throws IOException {

        // FIXME: throw exception if the same key already exists at tencent cos

        CosPath cosPath = (CosPath) dir;

        Preconditions.checkArgument(attrs.length == 0,
                "attrs not yet supported: %s", ImmutableList.copyOf(attrs)); // TODO

        List<Tag> tags = cosPath.getTagsList();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);

        String keyName = cosPath.getKey()
                + (cosPath.getKey().endsWith("/") ? "" : "/");

        cosPath.getFileSystem()
                .getClient()
                .putObject(cosPath.getBucket(), keyName,
                        new ByteArrayInputStream(new byte[0]), metadata, tags, null);
    }

    @Override
    public void delete(Path path) throws IOException {
        Preconditions.checkArgument(path instanceof CosPath,
                "path must be an instance of %s", CosPath.class.getName());

        CosPath cosPath = (CosPath) path;

        if (Files.notExists(path)) {
            throw new NoSuchFileException("the path: " + FilesEx.toUriString(cosPath) + " does not exist");
        }

        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                if (stream.iterator().hasNext()) {
                    throw new DirectoryNotEmptyException("the path: "
                            + FilesEx.toUriString(cosPath)
                            + " is a directory and is not empty");
                }
            }
        }

        // we delete the two objects (sometimes exists the key '/' and sometimes not)
        cosPath.getFileSystem().getClient()
                .deleteObject(cosPath.getBucket(), cosPath.getKey());
        cosPath.getFileSystem().getClient()
                .deleteObject(cosPath.getBucket(), cosPath.getKey() + "/");
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options)
            throws IOException {
        Preconditions.checkArgument(source instanceof CosPath,
                "source must be an instance of %s", CosPath.class.getName());
        Preconditions.checkArgument(target instanceof CosPath,
                "target must be an instance of %s", CosPath.class.getName());

        if (isSameFile(source, target)) {
            return;
        }

        CosPath cosSource = (CosPath) source;
        CosPath cosTarget = (CosPath) target;
        /*
         * Preconditions.checkArgument(!cosSource.isDirectory(),
         * "copying directories is not yet supported: %s", source); // TODO
         * Preconditions.checkArgument(!cosTarget.isDirectory(),
         * "copying directories is not yet supported: %s", target); // TODO
         */
        ImmutableSet<CopyOption> actualOptions = ImmutableSet.copyOf(options);
        verifySupportedOptions(EnumSet.of(StandardCopyOption.REPLACE_EXISTING),
                actualOptions);

        if (!actualOptions.contains(StandardCopyOption.REPLACE_EXISTING)) {
            if (exists(cosTarget)) {
                throw new FileAlreadyExistsException(format(
                        "target already exists: %s", FilesEx.toUriString(cosTarget)));
            }
        }

        CosClient client = cosSource.getFileSystem().getClient();

        final ObjectMetadata sourceObjMetadata = cosSource.getFileSystem().
                getClient().getObjectMetadata(cosSource.getBucket(), cosSource.getKey());
        final CosMultipartOptions opts = new CosMultipartOptions();
        final long maxSize = opts.getMaxCopySize();
        final long length = sourceObjMetadata.getContentLength();
        final List<Tag> tags = ((CosPath) target).getTagsList();
        final String contentType = ((CosPath) target).getContentType();
        final String storageClass = ((CosPath) target).getStorageClass();

        if (length <= maxSize) {
            CopyObjectRequest copyObjRequest = new CopyObjectRequest(cosSource.getBucket(),
                    cosSource.getKey(), cosTarget.getBucket(), cosTarget.getKey());
            log.trace("Copy file via copy object - source: source={}, target={}, tags={}, storageClass={}",
                    cosSource, cosTarget, tags, storageClass);
            client.copyObject(copyObjRequest, tags, contentType, storageClass);
        } else {
            log.trace("Copy file via multi upload - source: source={}, target={}, tags={}, storageClass={}",
                    cosSource, cosTarget, tags, storageClass);
            client.multipartCopyObject(cosSource, cosTarget, length, opts, tags, contentType, storageClass);
        }
    }


    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        for (CopyOption it : options) {
            if (it == StandardCopyOption.ATOMIC_MOVE) {
                throw new IllegalArgumentException(
                        "Atomic move not supported by Cos file system provider");
            }
        }
        copy(source, target, options);
        delete(source);
    }

    @Override
    public boolean isSameFile(Path path1, Path path2) throws IOException {
        return path1.isAbsolute() && path2.isAbsolute() && path1.equals(path2);
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        CosPath cosPath = (CosPath) path;
        Preconditions.checkArgument(cosPath.isAbsolute(),
                "path must be absolute: %s", cosPath);

        CosClient client = cosPath.getFileSystem().getClient();

        if (modes == null || modes.length == 0) {
            // when no modes are given, the method is invoked
            // by `Files.exists` method, therefore just use summary lookup
            cosObjectSummaryLookup.lookup((CosPath) path);
            return;
        }

        // get ACL and check if the file exists as a side-effect
        AccessControlList acl = getAccessControl(cosPath);

        for (AccessMode accessMode : modes) {
            switch (accessMode) {
                case EXECUTE:
                    throw new AccessDeniedException(cosPath.toString(), null,
                            "file is not executable");
                case READ:
                    // todo: current use ACL owner directly, need to add acl check logic
                    if (!hasPermissions(acl, acl.getOwner(),
                            EnumSet.of(Permission.FullControl, Permission.Read))) {
                        throw new AccessDeniedException(cosPath.toString(), null,
                                "file is not readable");
                    }
                    break;
                case WRITE:
                    // todo: current use ACL owner directly, need to add acl check logic
                    if (!hasPermissions(acl, acl.getOwner(),
                            EnumSet.of(Permission.FullControl, Permission.Write))) {
                        throw new AccessDeniedException(cosPath.toString(), null,
                                format("bucket '%s' is not writable",
                                        cosPath.getBucket()));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * check if the param acl has the same owner than the parameter owner and
     * have almost one of the permission set in the parameter permissions
     *
     * @param acl
     * @param owner
     * @param permissions almost one
     * @return
     */
    private boolean hasPermissions(AccessControlList acl, Owner owner,
            EnumSet<Permission> permissions) {
        boolean result = false;
        for (Grant grant : acl.getGrants()) {
            if (grant.getGrantee().getIdentifier().equals(owner.getId())
                    && permissions.contains(grant.getPermission())) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        Preconditions.checkArgument(path instanceof CosPath,
                "path must be an instance of %s", CosPath.class.getName());
        CosPath cosPath = (CosPath) path;
        if (type.isAssignableFrom(BasicFileAttributeView.class)) {
            try {
                return (V) new CosFileAttributesView(readAttr0(cosPath));
            } catch (IOException e) {
                throw new RuntimeException("Unable read attributes for file: "
                        + FilesEx.toUriString(cosPath), e);
            }
        }
//        throw new UnsupportedOperationException("Not a valid Cos file system provider file attribute view: " +
//                type.getName() + ",cos path:" + FilesEx.toUriString(cosPath));
        return null;
    }


    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
            throws IOException {
        Preconditions.checkArgument(path instanceof CosPath,
                "path must be an instance of %s", CosPath.class.getName());
        CosPath cosPath = (CosPath) path;
        if (type.isAssignableFrom(BasicFileAttributes.class)) {
            return (A) ("".equals(cosPath.getKey())
                    // the root bucket is implicitly a directory
                    ? new CosFileAttributes("/", null, 0, true, false)
                    // read the target path attributes
                    : readAttr0(cosPath));
        }
        // not support attribute class
        throw new UnsupportedOperationException(format("only %s supported", BasicFileAttributes.class));
    }

    private Optional<CosFileAttributes> readAttr1(CosPath cosPath) throws IOException {
        try {
            return Optional.of(readAttr0(cosPath));
        } catch (NoSuchFileException e) {
            return Optional.<CosFileAttributes>empty();
        }
    }

    private CosFileAttributes readAttr0(CosPath cosPath) throws IOException {
        COSObjectSummary objectSummary = cosObjectSummaryLookup.lookup(cosPath);

        // parse the data to BasicFileAttributes.
        FileTime lastModifiedTime = null;
        if (objectSummary.getLastModified() != null) {
            lastModifiedTime = FileTime.from(objectSummary.getLastModified().getTime(),
                    TimeUnit.MILLISECONDS);
        }

        long size = objectSummary.getSize();
        boolean directory = false;
        boolean regularFile = false;
        String key = objectSummary.getKey();
        // check if is a directory and the key of this directory exists in tencent cos
        if (objectSummary.getKey().equals(cosPath.getKey() + "/") && objectSummary.getKey().endsWith("/")) {
            directory = true;
        } else if ((!objectSummary.getKey().equals(cosPath.getKey()) || "".equals(cosPath.getKey()))
                // is a directory but does not exist in tencent cos
                && objectSummary.getKey().startsWith(cosPath.getKey())) {
            directory = true;
            // no metadata, we fake one
            size = 0;
            // delete extra part
            key = cosPath.getKey() + "/";
        } else {
            // is a file:
            regularFile = true;
        }

        return new CosFileAttributes(key, lastModifiedTime, size, directory, regularFile);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value,
            LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected CosFileSystem createFileSystem(URI uri, CosConfig cosConfig) {

        CosClient client;

        client = new CosClient(cosConfig);

        return new CosFileSystem(this, client, uri);
    }

    protected String getProp(Properties props, String... keys) {
        for (String k : keys) {
            if (props.containsKey(k)) {
                return props.getProperty(k);
            }
        }
        return null;
    }

    private <T> void verifySupportedOptions(Set<? extends T> allowedOptions,
            Set<? extends T> actualOptions) {
        Sets.SetView<? extends T> unsupported = difference(actualOptions,
                allowedOptions);
        Preconditions.checkArgument(unsupported.isEmpty(),
                "the following options are not supported: %s", unsupported);
    }

    /**
     * check that the paths exists or not
     *
     * @param path CosPath
     * @return true if exists
     */
    private boolean exists(CosPath path) {
        try {
            cosObjectSummaryLookup.lookup(path);
            return true;
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    /**
     * Get the Control List, if the path does not exist
     * (because the path is a directory and this key isn't created at tencent cos)
     * then return the ACL of the first child.
     *
     * @param path {@link CosPath}
     * @return AccessControlList
     * @throws NoSuchFileException if not found the path and any child
     */
    private AccessControlList getAccessControl(CosPath path) throws NoSuchFileException {
        COSObjectSummary obj = cosObjectSummaryLookup.lookup(path);
        // check first for file:
        return path.getFileSystem().getClient().getObjectAcl(obj.getBucketName(), obj.getKey());
    }

    /**
     * create a temporal directory to create streams
     *
     * @return Path temporal folder
     * @throws IOException
     */
    protected Path createTempDir() throws IOException {
        return Files.createTempDirectory("temp-cos-");
    }

}
