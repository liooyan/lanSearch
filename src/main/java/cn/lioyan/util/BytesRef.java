package cn.lioyan.util;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

@Slf4j
@Setter
@Getter
public class BytesRef implements Comparable<BytesRef>, Cloneable, Iterable<Byte> {


    public static final byte[] EMPTY_BYTES = new byte[0];


    private byte[] bytes;

    private int offset;

    private int length;


    public BytesRef() {
        this(EMPTY_BYTES);
    }

    public BytesRef(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public BytesRef(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int compareTo(BytesRef other) {
        return Arrays.compareUnsigned(this.bytes, this.offset, this.offset + this.length,
                other.bytes, other.offset, other.offset + other.length);
    }

    public boolean bytesEquals(BytesRef other) {
        return Arrays.equals(this.bytes, this.offset, this.offset + this.length,
                other.bytes, other.offset, other.offset + other.length);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof BytesRef) {
            return this.bytesEquals((BytesRef) other);
        }
        return false;
    }

    public String utf8ToString() {
        final char[] ref = new char[length];
        final int len = UnicodeUtil.UTF8toUTF16(bytes, offset, length, ref);
        return new String(ref, 0, len);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            if (i > offset) {
                sb.append(' ');
            }
            sb.append("0x").append(Integer.toHexString(bytes[i] & 0xff));
        }
        sb.append(']');
        return sb.toString();
    }


    public void validThrowException() {
        if (bytes == null) {
            throw new IllegalStateException("bytes is null");
        }
        if (length < 0) {
            throw new IllegalStateException("length is negative: " + length);
        }
        if (length > bytes.length) {
            throw new IllegalStateException("length is out of bounds: " + length + ",bytes.length=" + bytes.length);
        }
        if (offset < 0) {
            throw new IllegalStateException("offset is negative: " + offset);
        }
        if (offset > bytes.length) {
            throw new IllegalStateException("offset out of bounds: " + offset + ",bytes.length=" + bytes.length);
        }
        if (offset + length < 0) {
            throw new IllegalStateException("offset+length is negative: offset=" + offset + ",length=" + length);
        }
        if (offset + length > bytes.length) {
            throw new IllegalStateException("offset+length out of bounds: offset=" + offset + ",length=" + length + ",bytes.length=" + bytes.length);
        }
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index  < length;
            }

            @Override
            public Byte next() {
                Byte data = bytes[index + offset];
                index++;
                return data;
            }
        };
    }
}
