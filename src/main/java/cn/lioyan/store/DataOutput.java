package cn.lioyan.store;

import cn.lioyan.util.BitUtil;

import java.io.IOException;

public abstract class DataOutput {


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

    public final void writeZInt(int i) throws IOException {
        writeVInt(BitUtil.zigZagEncode(i));
    }

}
