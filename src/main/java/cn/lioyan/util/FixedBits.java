package cn.lioyan.util;

/**
 * {@link FixedBits}
 * 固定的{@link Bits}集合，通过构造函数中的bits
 * @author com.lioyan
 * @date 2023/5/30  13:57
 */
public class FixedBits implements Bits {
    final long[] bits;
    final int length;

    FixedBits(long[] bits, int length) {
        this.bits = bits;
        this.length = length;
    }


    @Override
    public boolean get(int index) {
        assert index >= 0 && index < length: "index=" + index + ", numBits=" + length;
        int i = index >> 6;               // div 64
        // signed shift will keep a negative index and force an
        // array-index-out-of-bounds-exception, removing the need for an explicit check.
        long bitmask = 1L << index;
        return (bits[i] & bitmask) != 0;
    }

    @Override
    public int length() {
        return length;
    }

}
