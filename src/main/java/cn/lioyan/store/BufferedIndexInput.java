package cn.lioyan.store;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 使用 {@link ByteBuffer}的 输入流,
 * 当 ByteBuffer 数据读取完后，重新加载
 * 并且支持 pos 指定读取文件位置， 通过seek方法实现，
 */
public abstract class BufferedIndexInput  extends IndexInput implements RandomAccessInput{
    private static final ByteBuffer EMPTY_BYTEBUFFER = ByteBuffer.allocate(0);

    public static final int BUFFER_SIZE = 1024;

    public static final int MERGE_BUFFER_SIZE = 4096;

    public static final int MIN_BUFFER_SIZE = 8;


    private long bufferStart = 0;
    private int bufferSize = BUFFER_SIZE;
    private ByteBuffer buffer = EMPTY_BYTEBUFFER;

    public BufferedIndexInput(String resourceDesc) {
        this(resourceDesc, BUFFER_SIZE);
    }

    public BufferedIndexInput(String resourceDesc, IOContext context) {
        this(resourceDesc, bufferSize(context));
    }

    /** Inits BufferedIndexInput with a specific bufferSize */
    public BufferedIndexInput(String resourceDesc, int bufferSize) {
        super(resourceDesc);
        checkBufferSize(bufferSize);
        this.bufferSize = bufferSize;
    }

    @Override
    public final byte readByte() throws IOException {
        if (buffer.hasRemaining() == false) {
            refill();
        }
        return buffer.get();
    }

    public static int bufferSize(IOContext context) {
        switch (context.context) {
            case MERGE:
                return MERGE_BUFFER_SIZE;
            default:
                return BUFFER_SIZE;
        }
    }

    @Override
    public final void readBytes(byte[] b, int offset, int len) throws IOException {
        readBytes(b, offset, len, true);
    }

    @Override
    public final void readBytes(byte[] b, int offset, int len, boolean useBuffer) throws IOException {
        int available = buffer.remaining();
        if(len <= available){
            // the buffer contains enough data to satisfy this request
            if(len>0) // to allow b to be null if len is 0...
                buffer.get(b, offset, len);
        } else {
            // the buffer does not have enough data. First serve all we've got.
            if(available > 0){
                buffer.get(b, offset, available);
                offset += available;
                len -= available;
            }
            // and now, read the remaining 'len' bytes:
            if (useBuffer && len<bufferSize){
                // If the amount left to read is small enough, and
                // we are allowed to use our buffer, do it in the usual
                // buffered way: fill the buffer and copy from it:
                refill();
                if(buffer.remaining()<len){
                    // Throw an exception when refill() could not read len bytes:
                    buffer.get(b, offset, buffer.remaining());
                    throw new EOFException("read past EOF: " + this);
                } else {
                    buffer.get(b, offset, len);
                }
            } else {
                // The amount left to read is larger than the buffer
                // or we've been asked to not use our buffer -
                // there's no performance reason not to read it all
                // at once. Note that unlike the previous code of
                // this function, there is no need to do a seek
                // here, because there's no need to reread what we
                // had in the buffer.
                long after = bufferStart+buffer.position()+len;
                if(after > length())
                    throw new EOFException("read past EOF: " + this);
                readInternal(ByteBuffer.wrap(b, offset, len));
                bufferStart = after;
                buffer.limit(0);  // trigger refill() on read
            }
        }
    }
    @Override
    public final short readShort() throws IOException {
        if (Short.BYTES <= buffer.remaining()) {
            return buffer.getShort();
        } else {
            return super.readShort();
        }
    }

    @Override
    public final int readInt() throws IOException {
        if (Integer.BYTES <= buffer.remaining()) {
            return buffer.getInt();
        } else {
            return super.readInt();
        }
    }

    @Override
    public final long readLong() throws IOException {
        if (Long.BYTES <= buffer.remaining()) {
            return buffer.getLong();
        } else {
            return super.readLong();
        }
    }

    @Override
    public final int readVInt() throws IOException {
        if (5 <= buffer.remaining()) {
            byte b = buffer.get();
            if (b >= 0) return b;
            int i = b & 0x7F;
            b = buffer.get();
            i |= (b & 0x7F) << 7;
            if (b >= 0) return i;
            b = buffer.get();
            i |= (b & 0x7F) << 14;
            if (b >= 0) return i;
            b = buffer.get();
            i |= (b & 0x7F) << 21;
            if (b >= 0) return i;
            b = buffer.get();
            // Warning: the next ands use 0x0F / 0xF0 - beware copy/paste errors:
            i |= (b & 0x0F) << 28;
            if ((b & 0xF0) == 0) return i;
            throw new IOException("Invalid vInt detected (too many bits)");
        } else {
            return super.readVInt();
        }
    }

    @Override
    public final long readVLong() throws IOException {
        if (9 <= buffer.remaining()) {
            byte b = buffer.get();
            if (b >= 0) return b;
            long i = b & 0x7FL;
            b = buffer.get();
            i |= (b & 0x7FL) << 7;
            if (b >= 0) return i;
            b = buffer.get();
            i |= (b & 0x7FL) << 14;
            if (b >= 0) return i;
            b = buffer.get();
            i |= (b & 0x7FL) << 21;
            if (b >= 0) return i;
            b = buffer.get();
            i |= (b & 0x7FL) << 28;
            if (b >= 0) return i;
            b = buffer.get();
            i |= (b & 0x7FL) << 35;
            if (b >= 0) return i;
            b = buffer.get();
            i |= (b & 0x7FL) << 42;
            if (b >= 0) return i;
            b = buffer.get();
            i |= (b & 0x7FL) << 49;
            if (b >= 0) return i;
            b = buffer.get();
            i |= (b & 0x7FL) << 56;
            if (b >= 0) return i;
            throw new IOException("Invalid vLong detected (negative values disallowed)");
        } else {
            return super.readVLong();
        }
    }

    @Override
    public final byte readByte(long pos) throws IOException {
        long index = pos - bufferStart;
        if (index < 0 || index >= buffer.limit()) {
            bufferStart = pos;
            buffer.limit(0);  // trigger refill() on read
            seekInternal(pos);
            refill();
            index = 0;
        }
        return buffer.get((int) index);
    }

    @Override
    public final short readShort(long pos) throws IOException {
        long index = pos - bufferStart;
        if (index < 0 || index >= buffer.limit()-1) {
            bufferStart = pos;
            buffer.limit(0);  // trigger refill() on read
            seekInternal(pos);
            refill();
            index = 0;
        }
        return buffer.getShort((int) index);
    }

    @Override
    public final int readInt(long pos) throws IOException {
        long index = pos - bufferStart;
        if (index < 0 || index >= buffer.limit()-3) {
            bufferStart = pos;
            buffer.limit(0);  // trigger refill() on read
            seekInternal(pos);
            refill();
            index = 0;
        }
        return buffer.getInt((int) index);
    }

    @Override
    public final long readLong(long pos) throws IOException {
        long index = pos - bufferStart;
        if (index < 0 || index >= buffer.limit()-7) {
            bufferStart = pos;
            buffer.limit(0);  // trigger refill() on read
            seekInternal(pos);
            refill();
            index = 0;
        }
        return buffer.getLong((int) index);
    }
    /**
     * 当  buffer 内数据 消费完时，重新获取数据
     * @throws IOException
     */
    private void refill() throws IOException {
        long start = bufferStart + buffer.position();
        long end = start + bufferSize;
        if (end > length())  // don't read past EOF
            end = length();
        int newLength = (int)(end - start);
        if (newLength <= 0)
            throw new EOFException("read past EOF: " + this);

        if (buffer == EMPTY_BYTEBUFFER) {
            buffer = ByteBuffer.allocate(bufferSize);  // allocate buffer lazily
            seekInternal(bufferStart);
        }
        buffer.position(0);
        buffer.limit(newLength);
        bufferStart = start;
        readInternal(buffer);
        // Make sure sub classes don't mess up with the buffer.
        assert buffer.order() == ByteOrder.BIG_ENDIAN : buffer.order();
        assert buffer.remaining() == 0 : "should have thrown EOFException";
        assert buffer.position() == newLength;
        buffer.flip();
    }

    private void checkBufferSize(int bufferSize) {
        if (bufferSize < MIN_BUFFER_SIZE)
            throw new IllegalArgumentException("bufferSize must be at least MIN_BUFFER_SIZE (got " + bufferSize + ")");
    }
    @Override
    public final long getFilePointer() { return bufferStart + buffer.position(); }

    @Override
    public final void seek(long pos) throws IOException {
        if (pos >= bufferStart && pos < (bufferStart + buffer.limit()))
            buffer.position((int)(pos - bufferStart));  // seek within buffer
        else {
            bufferStart = pos;
            buffer.limit(0);  // trigger refill() on read
            seekInternal(pos);
        }
    }

    public final int getBufferSize() {
        return bufferSize;
    }
    /**
     * 将当前的读取指针，重置到pos位置
     *
     * @param pos
     * @throws IOException
     */
    protected abstract void seekInternal(long pos) throws IOException;

    public abstract IndexInput slice(String sliceDescription, long offset, long length) throws IOException;

    /**
     *
     * 继续加载，下一部分数据至 b中
     * @param b
     * @throws IOException
     */
    protected abstract void readInternal(ByteBuffer b) throws IOException;
}
