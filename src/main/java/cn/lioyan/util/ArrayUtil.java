package cn.lioyan.util;

public class ArrayUtil {

    public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - RamUsageEstimator.NUM_BYTES_ARRAY_HEADER;

    public static boolean equals(byte[] a,int aFrom,int aTo,byte[] b,int bFrom, int bTo) {
        checkFromToIndex(aFrom, aTo, a.length);
        checkFromToIndex(bFrom, bTo, b.length);
        int aLen = aTo - aFrom;
        int bLen = bTo - bFrom;
        int len = Math.min(aLen, bLen);
        // lengths differ: cannot be equal
        if (aLen != bLen) {
            return false;
        }
        for (int i = 0; i < aLen; i++) {
            if (a[i+aFrom] != b[i+bFrom]) {
                return false;
            }
        }
        return true;
    }
    public static int compareUnsigned(byte[] a, int aFromIndex, int aToIndex, byte[] b, int bFromIndex, int bToIndex) {
        checkFromToIndex(aFromIndex, aToIndex, a.length);
        checkFromToIndex(bFromIndex, bToIndex, b.length);
        int aLen = aToIndex - aFromIndex;
        int bLen = bToIndex - bFromIndex;
        int len = Math.min(aLen, bLen);
        for (int i = 0; i < len; i++) {
            int aByte = a[i+aFromIndex] & 0xFF;
            int bByte = b[i+bFromIndex] & 0xFF;
            int diff = aByte - bByte;
            if (diff != 0) {
                return diff;
            }
        }

        // One is a prefix of the other, or, they are equal:
        return aLen - bLen;
    }


    // so this method works just like checkFromToIndex, but with that stupidity added.
    private static void checkFromToIndex(int fromIndex, int toIndex, int length) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex " + fromIndex + " > toIndex " + toIndex);
        }
        if (fromIndex < 0 || toIndex > length) {
            throw new IndexOutOfBoundsException("Range [" + fromIndex + ", " + toIndex + ") out-of-bounds for length " + length);
        }
    }
    public static long[] grow(long[] array, int minSize) {
        assert minSize >= 0: "size must be positive (got " + minSize + "): likely integer overflow?";
        if (array.length < minSize) {
            return growExact(array, oversize(minSize, Long.BYTES));
        } else
            return array;
    }
    public static int oversize(int minTargetSize, int bytesPerElement) {

        if (minTargetSize < 0) {
            // catch usage that accidentally overflows int
            throw new IllegalArgumentException("invalid array size " + minTargetSize);
        }

        if (minTargetSize == 0) {
            // wait until at least one element is requested
            return 0;
        }

        if (minTargetSize > MAX_ARRAY_LENGTH) {
            throw new IllegalArgumentException("requested array size " + minTargetSize + " exceeds maximum array in java (" + MAX_ARRAY_LENGTH + ")");
        }

        // asymptotic exponential growth by 1/8th, favors
        // spending a bit more CPU to not tie up too much wasted
        // RAM:
        int extra = minTargetSize >> 3;

        if (extra < 3) {
            // for very small arrays, where constant overhead of
            // realloc is presumably relatively high, we grow
            // faster
            extra = 3;
        }

        int newSize = minTargetSize + extra;

        // add 7 to allow for worst case byte alignment addition below:
        if (newSize+7 < 0 || newSize+7 > MAX_ARRAY_LENGTH) {
            // int overflowed, or we exceeded the maximum array length
            return MAX_ARRAY_LENGTH;
        }

            // round up to 8 byte alignment in 64bit env
            switch(bytesPerElement) {
                case 4:
                    // round up to multiple of 2
                    return (newSize + 1) & 0x7ffffffe;
                case 2:
                    // round up to multiple of 4
                    return (newSize + 3) & 0x7ffffffc;
                case 1:
                    // round up to multiple of 8
                    return (newSize + 7) & 0x7ffffff8;
                case 8:
                    // no rounding
                default:
                    // odd (invalid?) size
                    return newSize;
            }
    }

    public static long[] growExact(long[] array, int newLength) {
        long[] copy = new long[newLength];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }
    public static int[] growExact(int[] array, int newLength) {
        int[] copy = new int[newLength];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

}
