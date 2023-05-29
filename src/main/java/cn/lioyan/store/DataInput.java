package cn.lioyan.store;

import cn.lioyan.util.BitUtil;

import java.io.IOException;

/**
 * 输入流的父类
 */
public abstract class DataInput {


    public abstract byte readByte() throws IOException;


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
