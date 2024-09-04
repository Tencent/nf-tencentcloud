package nextflow.tencentcloud.nio;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class CosFileAttributesView implements BasicFileAttributeView {

    private CosFileAttributes fileAttributes;

    CosFileAttributesView(CosFileAttributes fileAttributes) {
        this.fileAttributes = fileAttributes;
    }

    @Override
    public String name() {
        return "basic";
    }

    @Override
    public BasicFileAttributes readAttributes() throws IOException {
        return fileAttributes;
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
        // cos is an object storage, currently not support set times
    }
}
