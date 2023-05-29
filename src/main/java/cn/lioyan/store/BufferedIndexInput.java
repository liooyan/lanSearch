package cn.lioyan.store;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 使用 {@link ByteBuffer}的 输入流
 */
public abstract class BufferedIndexInput  extends IndexInput implements RandomAccessInput{
    private static final ByteBuffer EMPTY_BYTEBUFFER = ByteBuffer.allocate(0);

    public static final int BUFFER_SIZE = 1024;

    public static final int MIN_BUFFER_SIZE = 8;


    private long bufferStart = 0;
    private int bufferSize = BUFFER_SIZE;
    private ByteBuffer buffer = EMPTY_BYTEBUFFER;

    protected BufferedIndexInput(String resourceDescription) {
        super(resourceDescription);
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


    /**
     * 将当前的读取指针，重置到pos位置
     *
     * @param pos
     * @throws IOException
     */
    protected abstract void seekInternal(long pos) throws IOException;

    /**
     *
     * 继续加载，下一部分数据至 b中
     * @param b
     * @throws IOException
     */
    protected abstract void readInternal(ByteBuffer b) throws IOException;
}
