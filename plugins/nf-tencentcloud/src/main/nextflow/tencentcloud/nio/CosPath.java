package nextflow.tencentcloud.nio;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcloud.cos.model.COSObjectId;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.Tag.Tag;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nextflow.file.TagAwareFile;


public class CosPath implements Path, TagAwareFile {

//    private static final Logger log = LoggerFactory.getLogger(CosPath.class);

    // fileSystem implementation
    private final CosFileSystem fileSystem;
    // bucket for fileSystem
    private final String bucket;
    // key list for fileSystem
    private final List<String> parts;

    public static final String PATH_SEPARATOR = "/";
    private COSObjectSummary objectSummary;
    private Map<String, String> tags;
    private String contentType;
    private String storageClass;

    public CosPath(CosFileSystem fileSystem, String path) {
        this(fileSystem, path, "");
    }

    /**
     * Create a new CosPath from a path string.
     * @param fileSystem CosFileSystem
     * @param first first part of path
     * @param more more parts of path
     */
    public CosPath(CosFileSystem fileSystem, String first,
            String... more) {

        String bucket = null;
        // split first part to get the bucket and other keys
        List<String> parts = Lists.newArrayList(Splitter.on(PATH_SEPARATOR).split(first));

        // java nio has no separator at end of files
        if (first.endsWith(PATH_SEPARATOR)) {
            parts.remove(parts.size() - 1);
        }

        // absolute path
        if (first.startsWith(PATH_SEPARATOR)) {
            Preconditions.checkArgument(!parts.isEmpty(),
                    "path must start with bucket name");
            Preconditions.checkArgument(!parts.get(1).isEmpty(),
                    "bucket name must be not empty");

            // absolute path, bucket is the first part
            bucket = parts.get(1);

            // absolute path, others are the other parts
            parts = parts.subList(2, parts.size());
        }

        // bucket has no / in the name
        if (bucket != null) {
            bucket = bucket.replace("/", "");
        }

        // split others parts
        List<String> moreSplitted = Lists.newArrayList();
        for (String part : more) {
            moreSplitted.addAll(Lists.newArrayList(Splitter.on(PATH_SEPARATOR).split(part)));
        }

        parts.addAll(moreSplitted);

        this.bucket = bucket;
        this.parts = parseKeyPart(parts);
        this.fileSystem = fileSystem;
    }

    private CosPath(CosFileSystem fileSystem, String bucket,
            Iterable<String> keys) {
        this.bucket = bucket;
        this.parts = parseKeyPart(keys);
        this.fileSystem = fileSystem;
    }


    @Override
    public CosFileSystem getFileSystem() {
        return this.fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return bucket != null;
    }

    @Override
    public Path getRoot() {
        if (isAbsolute()) {
            // root is empty key for the bucket
            return new CosPath(fileSystem, bucket, ImmutableList.of());
        }

        // not absolute, no root
        return null;
    }

    @Override
    public Path getFileName() {
        // file name return relative path for key(no bucket)
        if (!parts.isEmpty()) {
            return new CosPath(fileSystem, null, parts.subList(parts.size() - 1,
                    parts.size()));
        } else {
            // bucket has not parts, return null
            return null;
        }
    }

    @Override
    public Path getParent() {
        // bucket has not parts, so no parent
        if (parts.isEmpty()) {
            return null;
        }

        // bucket has not parts, so no parent
        if (parts.size() == 1 && (bucket == null || bucket.isEmpty())) {
            return null;
        }

        // remove last parts and return
        return new CosPath(fileSystem, bucket,
                parts.subList(0, parts.size() - 1));
    }

    @Override
    public int getNameCount() {
        return parts.size();
    }

    @Override
    public Path getName(int index) {
        // return null bucket(for relative path) and last part of the parts
        return new CosPath(fileSystem, null, parts.subList(index, index + 1));
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return new CosPath(fileSystem, null, parts.subList(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        // other is null
        if (other == null) {
            return false;
        }

        // other is not cosPath
        if (!(other instanceof CosPath)) {
            return false;
        }

        CosPath otherPath = (CosPath) other;

        // other is longer than this
        if (otherPath.getNameCount() > this.getNameCount()) {
            return false;
        }

        // other is empty
        if (otherPath.parts.isEmpty() && otherPath.bucket == null
                && (!this.parts.isEmpty() || this.bucket != null)) {
            return false;
        }

        // other bucket not equal
        if ((otherPath.getBucket() != null && !otherPath.getBucket().equals(this.getBucket()))
                || (otherPath.getBucket() == null && this.getBucket() != null)) {
            return false;
        }

        // bucket equal but parts not equal
        for (int i = 0; i < otherPath.parts.size(); i++) {
            if (!otherPath.parts.get(i).equals(this.parts.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean startsWith(String path) {
        CosPath other = new CosPath(this.fileSystem, path);
        return this.startsWith(other);
    }

    @Override
    public boolean endsWith(Path other) {
        // other is null
        if (other == null) {
            return false;
        }

        // other is not cosPath
        if (!(other instanceof CosPath)) {
            return false;
        }

        CosPath otherPath = (CosPath) other;

        // other is longer than this, so not ends with
        if (otherPath.getNameCount() > this.getNameCount()) {
            return false;
        }

        // other is empty, so not ends with
        if (otherPath.getNameCount() == 0
                && this.getNameCount() != 0) {
            return false;
        }

        // other bucket not equal
        if ((otherPath.getBucket() != null && !otherPath.getBucket().equals(this.getBucket()))
                || (otherPath.getBucket() != null && this.getBucket() == null)) {
            return false;
        }

        // check every parts from last to others' first
        int i = otherPath.parts.size() - 1;
        int j = this.parts.size() - 1;
        while (i >= 0 && j >= 0) {
            if (!otherPath.parts.get(i).equals(this.parts.get(j))) {
                return false;
            }
            i--;
            j--;
        }
        return true;
    }

    @Override
    public boolean endsWith(String other) {
        return this.endsWith(new CosPath(this.fileSystem, other));
    }

    @Override
    public Path normalize() {
        if (parts == null || parts.isEmpty()) {
            return this;
        }
        return new CosPath(fileSystem, bucket, normalize0(parts));
    }

    private Iterable<String> normalize0(List<String> parts) {
        final String s0 = Path.of(String.join(PATH_SEPARATOR, parts)).normalize().toString();
        return Lists.newArrayList(Splitter.on(PATH_SEPARATOR).split(s0));
    }

    @Override
    public Path resolve(Path other) {
        Preconditions.checkArgument(other instanceof CosPath,
                "other must be an instance of %s", CosPath.class.getName());
        CosPath cosPath = (CosPath) other;
        if (cosPath.isAbsolute()) {
            return cosPath;
        }
        if (cosPath.parts.isEmpty()) { // other is relative and empty
            return this;
        }
        return new CosPath(fileSystem, bucket, concat(parts, cosPath.parts));
    }

    @Override
    public Path resolve(String other) {
        return resolve(new CosPath(this.getFileSystem(), other));
    }

    @Override
    public Path resolveSibling(Path other) {
        Preconditions.checkArgument(other instanceof CosPath,
                "other must be an instance of %s", CosPath.class.getName());
        CosPath cosPath = (CosPath) other;
        Path parent = getParent();
        if (parent == null || cosPath.isAbsolute()) {
            return cosPath;
        }
        if (cosPath.parts.isEmpty()) {
            return parent;
        }
        return new CosPath(fileSystem, bucket, concat(
                parts.subList(0, parts.size() - 1), cosPath.parts));
    }

    @Override
    public Path resolveSibling(String other) {
        return resolveSibling(new CosPath(this.getFileSystem(), other));
    }

    @Override
    public Path relativize(Path other) {
        Preconditions.checkArgument(other instanceof CosPath,
                "other must be an instance of %s", CosPath.class.getName());
        CosPath cosPath = (CosPath) other;

        if (this.equals(other)) {
            return new CosPath(this.getFileSystem(), "");
        }

        Preconditions.checkArgument(isAbsolute(),
                "Path is already relative: %s", this);
        Preconditions.checkArgument(cosPath.isAbsolute(),
                "Cannot relativize against a relative path: %s", cosPath);
        Preconditions.checkArgument(bucket.equals(cosPath.getBucket()),
                "Cannot relativize paths with different buckets: '%s', '%s'",
                this, other);
        Preconditions.checkArgument(parts.size() <= cosPath.parts.size(),
                "Cannot relativize against a parent path: '%s', '%s'",
                this, other);

        int startPart = 0;
        for (int i = 0; i < this.parts.size(); i++) {
            if (this.parts.get(i).equals(cosPath.parts.get(i))) {
                startPart++;
            }
        }

        List<String> resultParts = new ArrayList<>();
        for (int i = startPart; i < cosPath.parts.size(); i++) {
            resultParts.add(cosPath.parts.get(i));
        }

        return new CosPath(fileSystem, null, resultParts);
    }

    @Override
    public URI toUri() {
        StringBuilder builder = new StringBuilder();
        builder.append("cos://");
        if (bucket != null) {
            builder.append(bucket);
            builder.append(PATH_SEPARATOR);
        }
        builder.append(Joiner.on(PATH_SEPARATOR).join(parts));
        return URI.create(builder.toString());
    }

    @Override
    public Path toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        }

        // cos path cannot convert from relative to absolute
        throw new IllegalStateException(format(
                "Relative path cannot be made absolute: %s", this));
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        // cos don't support toRealPath()
        throw new UnsupportedOperationException();
    }

    @Override
    public File toFile() {
        // cos don't support toFile()
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events,
            WatchEvent.Modifier... modifiers) throws IOException {
        // cos don't support register()
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events)
            throws IOException {
        // cos don't support register()
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Path> iterator() {
        ImmutableList.Builder<Path> builder = ImmutableList.builder();

        for (String part : parts) {
            builder.add(new CosPath(fileSystem, null, ImmutableList.of(part)));
        }

        return builder.build().iterator();
    }

    @Override
    public int compareTo(Path other) {
        // compare string equal
        return toString().compareTo(other.toString());
    }

    @Override
    public String toString() {
        // currently cannot separate absolute path(with bucket)
        // or relative path(without bucket) if relative first path is equal to bucket
        StringBuilder builder = new StringBuilder();

        if (isAbsolute()) {
            builder.append(PATH_SEPARATOR);
            builder.append(bucket);
            builder.append(PATH_SEPARATOR);
        }

        builder.append(Joiner.on(PATH_SEPARATOR).join(parts));

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CosPath paths = (CosPath) o;

        if (!Objects.equals(bucket, paths.bucket)) {
            return false;
        }
        return parts.equals(paths.parts);
    }

    @Override
    public int hashCode() {
        int result = bucket != null ? bucket.hashCode() : 0;
        result = 31 * result + parts.hashCode();
        return result;
    }

    public COSObjectSummary fetchObjectSummary() {
        COSObjectSummary result = objectSummary;
        objectSummary = null;
        return result;
    }

    void setObjectSummary(COSObjectSummary objectSummary) {
        this.objectSummary = objectSummary;
    }

    @Override
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }

//    @Override
//    public void setStorageClass(String storageClass) {
//        this.storageClass = storageClass;
//    }

    public List<Tag> getTagsList() {
        if (tags == null) {
            return Collections.emptyList();
        }
        List<Tag> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            result.add(new Tag(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    public String getContentType() {
        return contentType;
    }

    public String getStorageClass() {
        return storageClass;
    }

    private static Function<String, String> strip(final String... strs) {
        return input -> {
            String res = input;
            for (String str : strs) {
                res = res.replace(str, "");
            }
            return res;
        };
    }

    private static ImmutableList<String> parseKeyPart(List<String> parts) {
        return ImmutableList.copyOf(filter(transform(parts, strip("/")), notEmpty()));
    }

    private static ImmutableList<String> parseKeyPart(Iterable<String> parts) {
        return ImmutableList.copyOf(filter(transform(parts, strip("/")), notEmpty()));
    }

    private static Predicate<String> notEmpty() {
        return input -> input != null && !input.isEmpty();
    }

    public static String bucketName(URI uri) {
        // hostname is bucket name
        if (uri.getHost() != null) {
            return uri.getHost();
        }

        // first part of the path is the bucket name
        final String path = uri.getPath();
        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid Cos path: " + uri);
        }
        final String[] parts = path.split("/");
        return parts.length > 1 ? parts[1] : null;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        if (parts.isEmpty()) {
            return "";
        }

        ImmutableList.Builder<String> builder = ImmutableList
                .<String>builder().addAll(parts);

        return Joiner.on(PATH_SEPARATOR).join(builder.build());
    }

    public COSObjectId toCosObjectId() {
        return new COSObjectId(bucket, getKey());
    }
}
