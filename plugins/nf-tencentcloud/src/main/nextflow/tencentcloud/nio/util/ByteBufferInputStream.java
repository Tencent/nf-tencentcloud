package nextflow.tencentcloud.nio.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

    // save buffer locally
    private final ByteBuffer buffer;

    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public int read() throws IOException {
        // no remaining, return -1
        if (!buffer.hasRemaining()) {
            return -1;
        }
        // return buffer
        return buffer.get() & 0xFF;
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
        // no remaining, return -1
        if (!buffer.hasRemaining()) {
            return -1;
        }

        // get the actual size of the buffer
        len = Math.min(len, buffer.remaining());
        buffer.get(bytes, off, len);
        return len;
    }
}
