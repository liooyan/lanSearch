package cn.lioyan.store;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

/**
 * 带有文件校验的 文件  IndexOutput
 *
 */
public class OutputStreamIndexOutput extends  IndexOutput {
    private final CRC32 crc = new CRC32();
    private final BufferedOutputStream os;
    private boolean flushedOnClose = false;

    private long bytesWritten = 0L;

    public OutputStreamIndexOutput(String s, String name, OutputStream filterOutputStream, int chunkSize) {
        super(s, name);
        this.os = new BufferedOutputStream(new CheckedOutputStream(filterOutputStream, crc), chunkSize);
    }

    @Override
    public void writeByte(byte b) throws IOException {
        os.write(b);
        bytesWritten++;
    }
    @Override
    public final void writeBytes(byte[] b, int offset, int length) throws IOException {
        os.write(b, offset, length);
        bytesWritten += length;
    }

    @Override
    public void close() throws IOException {
        try (final OutputStream o = os) {
            if (!flushedOnClose) {
                flushedOnClose = true; // set this BEFORE calling flush!
                o.flush();
            }
        }
    }

    @Override
    public final long getFilePointer() {
        return bytesWritten;
    }

    @Override
    public long getChecksum() throws IOException {
        os.flush();
        return crc.getValue();
    }
}
