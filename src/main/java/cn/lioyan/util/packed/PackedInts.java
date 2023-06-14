package cn.lioyan.util.packed;

import cn.lioyan.codecs.CodecUtil;
import cn.lioyan.store.DataOutput;
import cn.lioyan.util.Accountable;
import cn.lioyan.util.RamUsageEstimator;

import java.io.IOException;
import java.util.Arrays;


/**
 * {@link PackedInts}
 *
 * @author com.lioyan
 * @date 2023/6/6  16:05
 */
public class PackedInts {

    public final static String CODEC_NAME = "PackedInts";
    public static final int VERSION_MONOTONIC_WITHOUT_ZIGZAG = 2;
    public final static int VERSION_START = VERSION_MONOTONIC_WITHOUT_ZIGZAG;
    public final static int VERSION_CURRENT = VERSION_MONOTONIC_WITHOUT_ZIGZAG;
    public static final int DEFAULT_BUFFER_SIZE = 1024; // 1K
    public static int bitsRequired(long maxValue) {
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be non-negative (got: " + maxValue + ")");
        }
        return unsignedBitsRequired(maxValue);
    }
    public static int unsignedBitsRequired(long bits) {
        return Math.max(1, 64 - Long.numberOfLeadingZeros(bits));
    }


    /**
     * 随机只读的long类型数组
     */
    public static abstract class Reader implements Accountable {

        /** Get the long at the given index. Behavior is undefined for out-of-range indices. */
        public abstract long get(int index);

        /**
         * Bulk get: read at least one and at most <code>len</code> longs starting
         * from <code>index</code> into <code>arr[off:off+len]</code> and return
         * the actual number of values that have been read.
         */
        public int get(int index, long[] arr, int off, int len) {
            assert len > 0 : "len must be > 0 (got " + len + ")";
            assert index >= 0 && index < size();
            assert off + len <= arr.length;

            final int gets = Math.min(size() - index, len);
            for (int i = index, o = off, end = index + gets; i < end; ++i, ++o) {
                arr[o] = get(i);
            }
            return gets;
        }

        /**
         * @return the number of values.
         */
        public abstract int size();
    }

    /**
     * 可设置的 数组
     */
    public static abstract class Mutable extends Reader {

        /**
         * @return the number of bits used to store any given value.
         *         Note: This does not imply that memory usage is
         *         {@code bitsPerValue * #values} as implementations are free to
         *         use non-space-optimal packing of bits.
         */
        public abstract int getBitsPerValue();

        /**
         * Set the value at the given index in the array.
         * @param index where the value should be positioned.
         * @param value a value conforming to the constraints set by the array.
         */
        public abstract void set(int index, long value);

        /**
         * Bulk set: set at least one and at most <code>len</code> longs starting
         * at <code>off</code> in <code>arr</code> into this mutable, starting at
         * <code>index</code>. Returns the actual number of values that have been
         * set.
         */
        public int set(int index, long[] arr, int off, int len) {
            assert len > 0 : "len must be > 0 (got " + len + ")";
            assert index >= 0 && index < size();
            len = Math.min(len, size() - index);
            assert off + len <= arr.length;

            for (int i = index, o = off, end = index + len; i < end; ++i, ++o) {
                set(i, arr[o]);
            }
            return len;
        }

        /**
         * Fill the mutable from <code>fromIndex</code> (inclusive) to
         * <code>toIndex</code> (exclusive) with <code>val</code>.
         */
        public void fill(int fromIndex, int toIndex, long val) {
            assert val <= maxValue(getBitsPerValue());
            assert fromIndex <= toIndex;
            for (int i = fromIndex; i < toIndex; ++i) {
                set(i, val);
            }
        }

        /**
         * Sets all values to 0.
         */
        public void clear() {
            fill(0, size(), 0);
        }

        /**
         * Save this mutable into <code>out</code>. Instantiating a reader from
         * the generated data will return a reader with the same number of bits
         * per value.
         */
        public void save(DataOutput out) throws IOException {
            Writer writer = getWriterNoHeader(out, getFormat(), size(), getBitsPerValue(), DEFAULT_BUFFER_SIZE);
            writer.writeHeader();
            for (int i = 0; i < size(); ++i) {
                writer.add(get(i));
            }
            writer.finish();
        }

        /** The underlying format. */
        Format getFormat() {
            return Format.PACKED;
        }

    }

    static abstract class MutableImpl extends Mutable {

        protected final int valueCount;
        protected final int bitsPerValue;

        protected MutableImpl(int valueCount, int bitsPerValue) {
            this.valueCount = valueCount;
            assert bitsPerValue > 0 && bitsPerValue <= 64 : "bitsPerValue=" + bitsPerValue;
            this.bitsPerValue = bitsPerValue;
        }

        @Override
        public final int getBitsPerValue() {
            return bitsPerValue;
        }

        @Override
        public final int size() {
            return valueCount;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(valueCount=" + valueCount + ",bitsPerValue=" + bitsPerValue + ")";
        }
    }

    public static long maxValue(int bitsPerValue) {
        return bitsPerValue == 64 ? Long.MAX_VALUE : ~(~0L << bitsPerValue);
    }
    public static Writer getWriterNoHeader(
            DataOutput out, Format format, int valueCount, int bitsPerValue, int mem) {
        return new PackedWriter(format, out, valueCount, bitsPerValue, mem);
    }
    public static Mutable getMutable(int valueCount,
                                     int bitsPerValue, float acceptableOverheadRatio) {
//        final FormatAndBits formatAndBits = fastestFormatAndBits(valueCount, bitsPerValue, acceptableOverheadRatio);
//        return getMutable(valueCount, formatAndBits.bitsPerValue, formatAndBits.format);
    return null;
    }
    public static Mutable getMutable(int valueCount,
                                     int bitsPerValue, PackedInts.Format format) {
        assert valueCount >= 0;
        return new Direct8(valueCount);
    }
    static int checkBlockSize(int blockSize, int minBlockSize, int maxBlockSize) {
        if (blockSize < minBlockSize || blockSize > maxBlockSize) {
            throw new IllegalArgumentException("blockSize must be >= " + minBlockSize + " and <= " + maxBlockSize + ", got " + blockSize);
        }
        if ((blockSize & (blockSize - 1)) != 0) {
            throw new IllegalArgumentException("blockSize must be a power of two, got " + blockSize);
        }
        return Integer.numberOfTrailingZeros(blockSize);
    }
    public static final class NullReader extends Reader {

        private final int valueCount;

        /** Sole constructor. */
        public NullReader(int valueCount) {
            this.valueCount = valueCount;
        }

        @Override
        public long get(int index) {
            return 0;
        }

        @Override
        public int get(int index, long[] arr, int off, int len) {
            assert len > 0 : "len must be > 0 (got " + len + ")";
            assert index >= 0 && index < valueCount;
            len = Math.min(len, valueCount - index);
            Arrays.fill(arr, off, off + len, 0);
            return len;
        }

        @Override
        public int size() {
            return valueCount;
        }

        @Override
        public long ramBytesUsed() {
            return RamUsageEstimator.alignObjectSize(RamUsageEstimator.NUM_BYTES_OBJECT_HEADER + Integer.BYTES);
        }
    }
    /**
     * 封装 DataOutput
     */
    public static abstract class Writer {
        protected final DataOutput out;
        protected final int valueCount;
        protected final int bitsPerValue;

        protected Writer(DataOutput out, int valueCount, int bitsPerValue) {
            assert bitsPerValue <= 64;
            assert valueCount >= 0 || valueCount == -1;
            this.out = out;
            this.valueCount = valueCount;
            this.bitsPerValue = bitsPerValue;
        }

        void writeHeader() throws IOException {
            assert valueCount != -1;
            CodecUtil.writeHeader(out, CODEC_NAME, VERSION_CURRENT);
            out.writeVInt(bitsPerValue);
            out.writeVInt(valueCount);
            out.writeVInt(getFormat().getId());
        }

        /** The format used to serialize values. */
        protected abstract PackedInts.Format getFormat();

        /** Add a value to the stream. */
        public abstract void add(long v) throws IOException;

        /** The number of bits per value. */
        public final int bitsPerValue() {
            return bitsPerValue;
        }

        /** Perform end-of-stream operations. */
        public abstract void finish() throws IOException;

        /**
         * Returns the current ord in the stream (number of values that have been
         * written so far minus one).
         */
        public abstract int ord();
    }

    public enum Format {
        /**
         * Compact format, all bits are written contiguously.
         */
        PACKED(0) {

            @Override
            public long byteCount(int packedIntsVersion, int valueCount, int bitsPerValue) {
                return (long) Math.ceil((double) valueCount * bitsPerValue / 8);
            }

        },

        /**
         *
         */
        PACKED_SINGLE_BLOCK(1) {

            @Override
            public int longCount(int packedIntsVersion, int valueCount, int bitsPerValue) {
                final int valuesPerBlock = 64 / bitsPerValue;
                return (int) Math.ceil((double) valueCount / valuesPerBlock);
            }

            @Override
            public boolean isSupported(int bitsPerValue) {
                return Packed64SingleBlock.isSupported(bitsPerValue);
            }

            @Override
            public float overheadPerValue(int bitsPerValue) {
                assert isSupported(bitsPerValue);
                final int valuesPerBlock = 64 / bitsPerValue;
                final int overhead = 64 % bitsPerValue;
                return (float) overhead / valuesPerBlock;
            }

        };

        /**
         * Get a format according to its ID.
         */
        public static Format byId(int id) {
            for (Format format : Format.values()) {
                if (format.getId() == id) {
                    return format;
                }
            }
            throw new IllegalArgumentException("Unknown format id: " + id);
        }

        private Format(int id) {
            this.id = id;
        }

        public int id;

        /**
         * Returns the ID of the format.
         */
        public int getId() {
            return id;
        }

        /**
         * Computes how many byte blocks are needed to store <code>values</code>
         * values of size <code>bitsPerValue</code>.
         */
        public long byteCount(int packedIntsVersion, int valueCount, int bitsPerValue) {
            assert bitsPerValue >= 0 && bitsPerValue <= 64 : bitsPerValue;
            // assume long-aligned
            return 8L * longCount(packedIntsVersion, valueCount, bitsPerValue);
        }

        /**
         * Computes how many long blocks are needed to store <code>values</code>
         * values of size <code>bitsPerValue</code>.
         */
        public int longCount(int packedIntsVersion, int valueCount, int bitsPerValue) {
            assert bitsPerValue >= 0 && bitsPerValue <= 64 : bitsPerValue;
            final long byteCount = byteCount(packedIntsVersion, valueCount, bitsPerValue);
            assert byteCount < 8L * Integer.MAX_VALUE;
            if ((byteCount % 8) == 0) {
                return (int) (byteCount / 8);
            } else {
                return (int) (byteCount / 8 + 1);
            }
        }

        /**
         * Tests whether the provided number of bits per value is supported by the
         * format.
         */
        public boolean isSupported(int bitsPerValue) {
            return bitsPerValue >= 1 && bitsPerValue <= 64;
        }

        /**
         * Returns the overhead per value, in bits.
         */
        public float overheadPerValue(int bitsPerValue) {
            assert isSupported(bitsPerValue);
            return 0f;
        }

        /**
         * Returns the overhead ratio (<code>overhead per value / bits per value</code>).
         */
        public final float overheadRatio(int bitsPerValue) {
            assert isSupported(bitsPerValue);
            return overheadPerValue(bitsPerValue) / bitsPerValue;
        }
    }
}
