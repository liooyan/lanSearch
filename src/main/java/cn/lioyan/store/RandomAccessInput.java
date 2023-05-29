package cn.lioyan.store;

import java.io.IOException;

public interface RandomAccessInput {


    public byte readByte(long pos) throws IOException;
    /**
     * Reads a short at the given position in the file
     * @see DataInput#readShort
     */
    public short readShort(long pos) throws IOException;
    /**
     * Reads an integer at the given position in the file
     * @see DataInput#readInt
     */
    public int readInt(long pos) throws IOException;
    /**
     * Reads a long at the given position in the file
     * @see DataInput#readLong
     */
    public long readLong(long pos) throws IOException;
}
