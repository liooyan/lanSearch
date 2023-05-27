package cn.lioyan.util;

public class Arrays {
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
}
