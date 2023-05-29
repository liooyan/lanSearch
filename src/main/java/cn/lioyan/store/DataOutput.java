package cn.lioyan.store;

import cn.lioyan.util.BitUtil;

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


    public void writeLong(long i) throws IOException {
        writeInt((int) (i >> 32));
        writeInt((int) i);
    }


}
