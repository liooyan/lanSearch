package cn.lioyan.util;

import cn.lioyan.search.DocIdSetIterator;

import java.util.Arrays;


/**
 * {@link FixedBitSet}
 * 一个固定长度的 {@link BitSet}
 * @author com.lioyan
 * @date 2023/5/30  13:52
 */
public class FixedBitSet  extends BitSet implements Bits, Accountable {
    private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(FixedBitSet.class);

    private final long[] bits; // Array of longs holding the bits
    private final int numBits; // The number of bits in use
    private final int numWords; // The exact number of longs needed to hold numBits (<= bits.length)

    public FixedBitSet(int numBits) {
        this.numBits = numBits;
        bits = new long[bits2words(numBits)];
        numWords = bits.length;
    }

    /**
     * Creates a new LongBitSet using the provided long[] array as backing store.
     * The storedBits array must be large enough to accommodate the numBits specified, but may be larger.
     * In that case the 'extra' or 'ghost' bits must be clear (or they may provoke spurious side-effects)
     * @param storedBits the array to use as backing store
     * @param numBits the number of bits actually needed
     */
    public FixedBitSet(long[] storedBits, int numBits) {
        this.numWords = bits2words(numBits);
        if (numWords > storedBits.length) {
            throw new IllegalArgumentException("The given long array is too small  to hold " + numBits + " bits");
        }
        this.numBits = numBits;
        this.bits = storedBits;

        assert verifyGhostBitsClear();
    }
    @Override
    public int cardinality() {
        // Depends on the ghost bits being clear!
        return (int) pop_array(bits, 0, numWords);
    }

    public static long pop_array(long[] arr, int wordOffset, int numWords) {
        long popCount = 0;
        for (int i = wordOffset, end = wordOffset + numWords; i < end; ++i) {
            popCount += Long.bitCount(arr[i]);
        }
        return popCount;
    }


    public static int bits2words(int numBits) {
        return ((numBits - 1) >> 6) + 1; // I.e.: get the word-offset of the last bit and add one (make sure to use >> so 0 returns 0!)
    }

    private boolean verifyGhostBitsClear() {
        for (int i = numWords; i < bits.length; i++) {
            if (bits[i] != 0) return false;
        }

        if ((numBits & 0x3f) == 0) return true;

        long mask = -1L << numBits;

        return (bits[numWords - 1] & mask) == 0;
    }

    @Override
    public long ramBytesUsed() {
        return BASE_RAM_BYTES_USED + RamUsageEstimator.sizeOf(bits);
    }


    public static FixedBitSet ensureCapacity(FixedBitSet bits, int numBits) {
        if (numBits < bits.numBits) {
            return bits;
        } else {
            // Depends on the ghost bits being clear!
            // (Otherwise, they may become visible in the new instance)
            int numWords = bits2words(numBits);
            long[] arr = bits.getBits();
            if (numWords >= arr.length) {
                arr = ArrayUtil.grow(arr, numWords + 1);
            }
            return new FixedBitSet(arr, arr.length << 6);
        }
    }

    public long[] getBits() {
        return bits;
    }
    @Override
    public void set(int index) {
        assert index >= 0 && index < numBits: "index=" + index + ", numBits=" + numBits;
        int wordNum = index >> 6;      // div 64
        long bitmask = 1L << index;
        bits[wordNum] |= bitmask;
    }
    public void set(int startIndex, int endIndex) {
        assert startIndex >= 0 && startIndex < numBits : "startIndex=" + startIndex + ", numBits=" + numBits;
        assert endIndex >= 0 && endIndex <= numBits : "endIndex=" + endIndex + ", numBits=" + numBits;
        if (endIndex <= startIndex) {
            return;
        }

        int startWord = startIndex >> 6;
        int endWord = (endIndex-1) >> 6;

        long startmask = -1L << startIndex;
        long endmask = -1L >>> -endIndex;  // 64-(endIndex&0x3f) is the same as -endIndex since only the lowest 6 bits are used

        if (startWord == endWord) {
            bits[startWord] |= (startmask & endmask);
            return;
        }

        bits[startWord] |= startmask;
        Arrays.fill(bits, startWord+1, endWord, -1L);
        bits[endWord] |= endmask;
    }

    @Override
    public void clear(int index) {
        assert index >= 0 && index < numBits: "index=" + index + ", numBits=" + numBits;
        int wordNum = index >> 6;
        long bitmask = 1L << index;
        bits[wordNum] &= ~bitmask;
    }


    @Override
    public int prevSetBit(int index) {
        assert index >= 0 && index < numBits: "index=" + index + " numBits=" + numBits;
        int i = index >> 6;
        final int subIndex = index & 0x3f;  // index within the word
        long word = (bits[i] << (63-subIndex));  // skip all the bits to the left of index

        if (word != 0) {
            return (i << 6) + subIndex - Long.numberOfLeadingZeros(word); // See LUCENE-3197
        }

        while (--i >= 0) {
            word = bits[i];
            if (word !=0 ) {
                return (i << 6) + 63 - Long.numberOfLeadingZeros(word);
            }
        }

        return -1;
    }

    @Override
    public int nextSetBit(int index) {
        // Depends on the ghost bits being clear!
        assert index >= 0 && index < numBits : "index=" + index + ", numBits=" + numBits;
        int i = index >> 6;
        long word = bits[i] >> index;  // skip all the bits to the right of index

        if (word!=0) {
            return index + Long.numberOfTrailingZeros(word);
        }

        while(++i < numWords) {
            word = bits[i];
            if (word != 0) {
                return (i<<6) + Long.numberOfTrailingZeros(word);
            }
        }

        return DocIdSetIterator.NO_MORE_DOCS;
    }

    @Override
    public boolean get(int index) {
        assert index >= 0 && index < numBits: "index=" + index + ", numBits=" + numBits;
        int i = index >> 6;               // div 64
        // signed shift will keep a negative index and force an
        // array-index-out-of-bounds-exception, removing the need for an explicit check.
        long bitmask = 1L << index;
        return (bits[i] & bitmask) != 0;
    }
    @Override
    public int length() {
        return numBits;
    }
}
