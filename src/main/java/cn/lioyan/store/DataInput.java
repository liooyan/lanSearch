package cn.lioyan.store;

import cn.lioyan.util.BitUtil;

import java.io.IOException;

/**
 * 输入流的父类
 */
public abstract class DataInput {


    public abstract byte readByte() throws IOException;

    public short readShort() throws IOException {
        return (short) (((readByte() & 0xFF) <<  8) |  (readByte() & 0xFF));
    }

    public int readInt() throws IOException {
        return ((readByte() & 0xFF) << 24) | ((readByte() & 0xFF) << 16)
                | ((readByte() & 0xFF) <<  8) |  (readByte() & 0xFF);
    }

    public long readLong() throws IOException {
        return (((long)readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
    }

    public final int readVInt() throws IOException {
        int data = 0;
        for (int i = 0; i < 4; i++) {
            byte b = readByte();
            data += (data<< (i*7)) | (b & 0x7F);
            if( (b & 0x80) != 0){
                break;
            }
        }
        return data;
    }

    public final int readZInt() throws IOException {
        return BitUtil.zigZagDecode(readVInt());
    }

}
