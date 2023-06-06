package cn.lioyan.util.packed;

/**
 * {@link PackedInts}
 *
 * @author com.lioyan
 * @date 2023/6/6  16:05
 */
public class PackedInts {



    public static int bitsRequired(long maxValue) {
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be non-negative (got: " + maxValue + ")");
        }
        return unsignedBitsRequired(maxValue);
    }
    public static int unsignedBitsRequired(long bits) {
        return Math.max(1, 64 - Long.numberOfLeadingZeros(bits));
    }
}
