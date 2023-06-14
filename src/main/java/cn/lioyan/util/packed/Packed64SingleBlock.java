package cn.lioyan.util.packed;

import java.util.Arrays;

/**
 * {@link Packed64SingleBlock}
 *
 * @author com.lioyan
 * @date 2023/6/14  10:17
 */
public class Packed64SingleBlock {
    private static final int[] SUPPORTED_BITS_PER_VALUE = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 16, 21, 32};

    public static boolean isSupported(int bitsPerValue) {
        return Arrays.binarySearch(SUPPORTED_BITS_PER_VALUE, bitsPerValue) >= 0;
    }
}
