package nextflow.tencentcloud.nio;

import static java.lang.String.format;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * CosFileAttributes
 * basic file attributes for cos file system
 */
public class CosFileAttributes implements BasicFileAttributes {

    /**
     * lastModifiedTime, lastAccessTime, creationTime equal, for cos is a object storage system
     */
    private final FileTime lastModifiedTime;
    /**
     * cos only can be a file or a directory, so isRegularFile means is a file
     */
    private final boolean isRegularFile;
    /**
     * check if is a directory
     */
    private final boolean isDirectory;
    /**
     * file size
     */
    private final long size;
    /**
     * file key
     */
    private final String key;

    public CosFileAttributes(String key, FileTime lastModifiedTime, long size,
            boolean isDirectory, boolean isRegularFile) {
        this.key = key;
        this.lastModifiedTime = lastModifiedTime;
        this.size = size;
        this.isDirectory = isDirectory;
        this.isRegularFile = isRegularFile;
    }

    @Override
    public FileTime lastModifiedTime() {
        return lastModifiedTime;
    }

    @Override
    public FileTime lastAccessTime() {
        return this.lastModifiedTime();
    }

    @Override
    public FileTime creationTime() {
        return this.lastModifiedTime();
    }

    @Override
    public boolean isRegularFile() {
        return isRegularFile;
    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * cos cannot have a symbolic link
     */
    @Override
    public boolean isSymbolicLink() {
        // directly return false
        return false;
    }

    /**
     * cos cannot be other
     */
    @Override
    public boolean isOther() {
        // directly return false
        return false;
    }

    @Override
    public long size() {
        // return file size
        return size;
    }

    @Override
    public Object fileKey() {
        // return file key
        return key;
    }

    @Override
    public String toString() {
        // return cosFileAttributes format string
        return format(
                "[cosFileAttributes: key:%s: lastModified=%s, size=%s, isDirectory=%s, isRegularFile=%s]",
                key, lastModifiedTime, size, isDirectory, isRegularFile);
    }
}
