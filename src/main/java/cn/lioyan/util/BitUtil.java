package cn.lioyan.util;

public class BitUtil {
    public static int zigZagEncode(int i) {
        return (i >> 31) ^ (i << 1);
    }

    public static long zigZagEncode(long l) {
        return (l >> 63) ^ (l << 1);
    }

    public static int zigZagDecode(int i) {
        return ((i >>> 1) ^ -(i & 1));
    }

    public static long zigZagDecode(long l) {
        return ((l >>> 1) ^ -(l & 1));
    }


}
