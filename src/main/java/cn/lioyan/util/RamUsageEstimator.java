package cn.lioyan.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * {@link RamUsageEstimator}
 * 关于内存相关计算
 *
 * @author com.lioyan
 * @date 2023/5/30  13:59
 */
public class RamUsageEstimator {

    /**
     * One kilobyte bytes.
     */
    public static final long ONE_KB = 1024;

    /**
     * One megabyte bytes.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * One gigabyte bytes.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    /**
     * No instantiation.
     */
    private RamUsageEstimator() {
    }

    /**
     * True, iff compressed references (oops) are enabled by this JVM
     */
    public final static boolean COMPRESSED_REFS_ENABLED;

    /**
     * Number of bytes this JVM uses to represent an object reference.
     */
    public final static int NUM_BYTES_OBJECT_REF;

    /**
     * Number of bytes to represent an object header (no fields, no alignments).
     */
    public final static int NUM_BYTES_OBJECT_HEADER;

    /**
     * Number of bytes to represent an array header (no content, but with alignments).
     */
    public final static int NUM_BYTES_ARRAY_HEADER;

    /**
     * A constant specifying the object alignment boundary inside the JVM. Objects will
     * always take a full multiple of this constant, possibly wasting some space.
     */
    public final static int NUM_BYTES_OBJECT_ALIGNMENT;

    /**
     * Approximate memory usage that we assign to all unknown queries -
     * this maps roughly to a BooleanQuery with a couple term clauses.
     */
    public static final int QUERY_DEFAULT_RAM_BYTES_USED = 1024;

    /**
     * Approximate memory usage that we assign to all unknown objects -
     * this maps roughly to a few primitive fields and a couple short String-s.
     */
    public static final int UNKNOWN_DEFAULT_RAM_BYTES_USED = 256;

    /**
     * Sizes of primitive classes.
     */
    public static final Map<Class<?>, Integer> primitiveSizes;

    static {
        Map<Class<?>, Integer> primitiveSizesMap = new IdentityHashMap<>();
        primitiveSizesMap.put(boolean.class, 1);
        primitiveSizesMap.put(byte.class, 1);
        primitiveSizesMap.put(char.class, Integer.valueOf(Character.BYTES));
        primitiveSizesMap.put(short.class, Integer.valueOf(Short.BYTES));
        primitiveSizesMap.put(int.class, Integer.valueOf(Integer.BYTES));
        primitiveSizesMap.put(float.class, Integer.valueOf(Float.BYTES));
        primitiveSizesMap.put(double.class, Integer.valueOf(Double.BYTES));
        primitiveSizesMap.put(long.class, Integer.valueOf(Long.BYTES));

        primitiveSizes = Collections.unmodifiableMap(primitiveSizesMap);
    }

    /**
     * JVMs typically cache small longs. This tries to find out what the range is.
     */
    static final long LONG_CACHE_MIN_VALUE, LONG_CACHE_MAX_VALUE;
    static final int LONG_SIZE, STRING_SIZE;

    /**
     * For testing only
     */
    static final boolean JVM_IS_HOTSPOT_64BIT;

    static final String MANAGEMENT_FACTORY_CLASS = "java.lang.management.ManagementFactory";
    static final String HOTSPOT_BEAN_CLASS = "com.sun.management.HotSpotDiagnosticMXBean";

    /**
     * Initialize constants and try to collect information about the JVM internals.
     */
    static {
        // Try to get compressed oops and object alignment (the default seems to be 8 on Hotspot);
        // (this only works on 64 bit, on 32 bits the alignment and reference size is fixed):
        boolean compressedOops = false;
        int objectAlignment = 8;
        boolean isHotspot = false;
        try {
            final Class<?> beanClazz = Class.forName(HOTSPOT_BEAN_CLASS);
            // we use reflection for this, because the management factory is not part
            // of Java 8's compact profile:
            final Object hotSpotBean = Class.forName(MANAGEMENT_FACTORY_CLASS)
                    .getMethod("getPlatformMXBean", Class.class)
                    .invoke(null, beanClazz);
            if (hotSpotBean != null) {
                isHotspot = true;
                final Method getVMOptionMethod = beanClazz.getMethod("getVMOption", String.class);
                try {
                    final Object vmOption = getVMOptionMethod.invoke(hotSpotBean, "UseCompressedOops");
                    compressedOops = Boolean.parseBoolean(
                            vmOption.getClass().getMethod("getValue").invoke(vmOption).toString()
                    );
                } catch (ReflectiveOperationException | RuntimeException e) {
                    isHotspot = false;
                }
                try {
                    final Object vmOption = getVMOptionMethod.invoke(hotSpotBean, "ObjectAlignmentInBytes");
                    objectAlignment = Integer.parseInt(
                            vmOption.getClass().getMethod("getValue").invoke(vmOption).toString()
                    );
                } catch (ReflectiveOperationException | RuntimeException e) {
                    isHotspot = false;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            isHotspot = false;
        }
        JVM_IS_HOTSPOT_64BIT = isHotspot;
        COMPRESSED_REFS_ENABLED = compressedOops;
        NUM_BYTES_OBJECT_ALIGNMENT = objectAlignment;
        // reference size is 4, if we have compressed oops:
        NUM_BYTES_OBJECT_REF = COMPRESSED_REFS_ENABLED ? 4 : 8;
        // "best guess" based on reference size:
        NUM_BYTES_OBJECT_HEADER = 8 + NUM_BYTES_OBJECT_REF;
        // array header is NUM_BYTES_OBJECT_HEADER + NUM_BYTES_INT, but aligned (object alignment):
        NUM_BYTES_ARRAY_HEADER = (int) alignObjectSize(NUM_BYTES_OBJECT_HEADER + Integer.BYTES);

        // get min/max value of cached Long class instances:
        long longCacheMinValue = 0;
        while (longCacheMinValue > Long.MIN_VALUE
                && Long.valueOf(longCacheMinValue - 1) == Long.valueOf(longCacheMinValue - 1)) {
            longCacheMinValue -= 1;
        }
        long longCacheMaxValue = -1;
        while (longCacheMaxValue < Long.MAX_VALUE
                && Long.valueOf(longCacheMaxValue + 1) == Long.valueOf(longCacheMaxValue + 1)) {
            longCacheMaxValue += 1;
        }
        LONG_CACHE_MIN_VALUE = longCacheMinValue;
        LONG_CACHE_MAX_VALUE = longCacheMaxValue;
        LONG_SIZE = (int) shallowSizeOfInstance(Long.class);
        STRING_SIZE = (int) shallowSizeOfInstance(String.class);
    }

    public static long shallowSizeOfInstance(Class<?> clazz) {
        if (clazz.isArray())
            throw new IllegalArgumentException("This method does not work with array classes.");
        if (clazz.isPrimitive())
            return primitiveSizes.get(clazz);

        long size = NUM_BYTES_OBJECT_HEADER;

        // Walk type hierarchy
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            final Class<?> target = clazz;
            final Field[] fields = AccessController.doPrivileged(new PrivilegedAction<Field[]>() {
                @Override
                public Field[] run() {
                    return target.getDeclaredFields();
                }
            });
            for (Field f : fields) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    size = adjustForField(size, f);
                }
            }
        }
        return alignObjectSize(size);
    }

    static long adjustForField(long sizeSoFar, final Field f) {
        final Class<?> type = f.getType();
        final int fsize = type.isPrimitive() ? primitiveSizes.get(type) : NUM_BYTES_OBJECT_REF;
        // TODO: No alignments based on field type/ subclass fields alignments?
        return sizeSoFar + fsize;
    }

    public static long alignObjectSize(long size) {
        size += (long) NUM_BYTES_OBJECT_ALIGNMENT - 1L;
        return size - (size % NUM_BYTES_OBJECT_ALIGNMENT);
    }

    public static long sizeOf(Long value) {
        if (value >= LONG_CACHE_MIN_VALUE && value <= LONG_CACHE_MAX_VALUE) {
            return 0;
        }
        return LONG_SIZE;
    }

    /** Returns the size in bytes of the byte[] object. */
    public static long sizeOf(byte[] arr) {
        return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + arr.length);
    }

    /** Returns the size in bytes of the boolean[] object. */
    public static long sizeOf(boolean[] arr) {
        return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + arr.length);
    }

    /** Returns the size in bytes of the char[] object. */
    public static long sizeOf(char[] arr) {
        return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + (long) Character.BYTES * arr.length);
    }

    /** Returns the size in bytes of the short[] object. */
    public static long sizeOf(short[] arr) {
        return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + (long) Short.BYTES * arr.length);
    }

    /** Returns the size in bytes of the int[] object. */
    public static long sizeOf(int[] arr) {
        return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + (long) Integer.BYTES * arr.length);
    }

    /** Returns the size in bytes of the float[] object. */
    public static long sizeOf(float[] arr) {
        return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + (long) Float.BYTES * arr.length);
    }

    /** Returns the size in bytes of the long[] object. */
    public static long sizeOf(long[] arr) {
        return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + (long) Long.BYTES * arr.length);
    }

    /** Returns the size in bytes of the double[] object. */
    public static long sizeOf(double[] arr) {
        return alignObjectSize((long) NUM_BYTES_ARRAY_HEADER + (long) Double.BYTES * arr.length);
    }

    /** Returns the size in bytes of the String[] object. */
}
