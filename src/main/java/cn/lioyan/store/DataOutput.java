package cn.lioyan.store;

import cn.lioyan.util.BitUtil;
import cn.lioyan.util.BytesRef;

import java.io.IOException;

/**
 * 输出流的父类
 */
public abstract class DataOutput {


    /**
     * 写入一个byte 字符
     * @param b 写入的内容
     * @throws IOException
     */
    public abstract void writeByte(byte b) throws IOException;
    /**
     * vint 类型，大端存储
     * 每个 byte只使用 7位进行存储，最高位用于表示是否下一个byte是否有值
     * @param i
     */
    public final void writeVInt(int i) throws IOException {
        while ((i & ~0x7F) != 0){
            writeByte((byte)((i & 0x7F) | 0x80));
            i >>>= 7;
        }
        writeByte((byte)i);

    }
    public abstract void writeBytes(byte[] b, int offset, int length) throws IOException;

    public final void writeZInt(int i) throws IOException {
        writeVInt(BitUtil.zigZagEncode(i));
    }


    public void writeInt(int i) throws IOException {
        writeByte((byte)(i >> 24));
        writeByte((byte)(i >> 16));
        writeByte((byte)(i >>  8));
        writeByte((byte) i);
    }

    /** Writes a short as two bytes.
     * @see DataInput#readShort()
     */
    public void writeShort(short i) throws IOException {
        writeByte((byte)(i >>  8));
        writeByte((byte) i);
    }
    public final void writeVLong(long i) throws IOException {
        if (i < 0) {
            throw new IllegalArgumentException("cannot write negative vLong (got: " + i + ")");
        }
        writeSignedVLong(i);
    }

    // write a potentially negative vLong
    private void writeSignedVLong(long i) throws IOException {
        while ((i & ~0x7FL) != 0L) {
            writeByte((byte)((i & 0x7FL) | 0x80L));
            i >>>= 7;
        }
        writeByte((byte)i);
    }

    /**
     * Write a {@link BitUtil#zigZagEncode(long) zig-zag}-encoded
     * {@link #writeVLong(long) variable-length} long. Writes between one and ten
     * bytes. This is typically useful to write small signed ints.
     */
    public final void writeZLong(long i) throws IOException {
        writeSignedVLong(BitUtil.zigZagEncode(i));
    }

    public void writeLong(long i) throws IOException {
        writeInt((int) (i >> 32));
        writeInt((int) i);
    }
    public void writeString(String s) throws IOException {
        final BytesRef utf8Result = new BytesRef(s);
        writeVInt(utf8Result.getLength());
        writeBytes(utf8Result.getBytes(), utf8Result.getOffset(), utf8Result.getLength());
    }


}
