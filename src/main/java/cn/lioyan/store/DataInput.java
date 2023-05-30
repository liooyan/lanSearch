package cn.lioyan.store;

import cn.lioyan.util.BitUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    public abstract void readBytes(byte[] b, int offset, int len) throws IOException;

    public abstract void readBytes(byte[] b, int offset, int len, boolean useBuffer) throws IOException;

    public int readVInt() throws IOException {
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
    public long readVLong() throws IOException {
        return readVLong(false);
    }

    private long readVLong(boolean allowNegative) throws IOException {
    /* This is the original code of this method,
     * but a Hotspot bug (see LUCENE-2975) corrupts the for-loop if
     * readByte() is inlined. So the loop was unwinded!
    byte b = readByte();
    long i = b & 0x7F;
    for (int shift = 7; (b & 0x80) != 0; shift += 7) {
      b = readByte();
      i |= (b & 0x7FL) << shift;
    }
    return i;
    */
        byte b = readByte();
        if (b >= 0) return b;
        long i = b & 0x7FL;
        b = readByte();
        i |= (b & 0x7FL) << 7;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 14;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 21;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 28;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 35;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 42;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 49;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 56;
        if (b >= 0) return i;
        if (allowNegative) {
            b = readByte();
            i |= (b & 0x7FL) << 63;
            if (b == 0 || b == 1) return i;
            throw new IOException("Invalid vLong detected (more than 64 bits)");
        } else {
            throw new IOException("Invalid vLong detected (negative values disallowed)");
        }
    }

    public String readString() throws IOException {
        int length = readVInt();
        final byte[] bytes = new byte[length];
        readBytes(bytes, 0, length);
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }

}
