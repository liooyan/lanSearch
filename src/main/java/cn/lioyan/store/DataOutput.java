package cn.lioyan.store;

import java.io.IOException;

/**
 * {@link DataOutput}
 *
 * @author com.lioyan
 * @date 2023/5/26  15:24
 */
public interface DataOutput {
    void writeByte(byte b) throws IOException;

    default void writeBytes(byte[] b, int length) throws IOException {
        writeBytes(b, 0, length);
    }

    void writeBytes(byte[] b, int offset, int length) throws IOException;


    default void writeInt(int i) throws IOException {
        writeByte((byte) (i >> 24));
        writeByte((byte) (i >> 16));
        writeByte((byte) (i >> 8));
        writeByte((byte) i);
    }

    default void writeShort(short i) throws IOException {
        writeByte((byte) (i >> 8));
        writeByte((byte) i);
    }

    default void writeVInt(int i) throws IOException {
        while ((i & ~0x7F) != 0) {
            writeByte((byte) ((i & 0x7F) | 0x80));
            i >>>= 7;
        }
        writeByte((byte) i);
    }
}
