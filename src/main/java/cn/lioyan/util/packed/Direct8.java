package cn.lioyan.util.packed;

import cn.lioyan.store.DataInput;
import cn.lioyan.util.RamUsageEstimator;

import java.io.IOException;
import java.util.Arrays;

/**
 * {@link Direct8}
 *
 * @author com.lioyan
 * @date 2023/6/14  10:30
 */
final class Direct8 extends PackedInts.MutableImpl {
    final byte[] values;

    Direct8(int valueCount) {
        super(valueCount, 8);
        values = new byte[valueCount];
    }

    Direct8(int packedIntsVersion, DataInput in, int valueCount) throws IOException {
        this(valueCount);
        in.readBytes(values, 0, valueCount);
        // because packed ints have not always been byte-aligned
        final int remaining = (int) (PackedInts.Format.PACKED.byteCount(packedIntsVersion, valueCount, 8) - 1L * valueCount);
        for (int i = 0; i < remaining; ++i) {
            in.readByte();
        }
    }

    @Override
    public long get(final int index) {
        return values[index] & 0xFFL;
    }

    @Override
    public void set(final int index, final long value) {
        values[index] = (byte) (value);
    }

    @Override
    public long ramBytesUsed() {
        return RamUsageEstimator.alignObjectSize(
                RamUsageEstimator.NUM_BYTES_OBJECT_HEADER
                        + 2 * Integer.BYTES                       // valueCount,bitsPerValue
                        + RamUsageEstimator.NUM_BYTES_OBJECT_REF) // values ref
                + RamUsageEstimator.sizeOf(values);
    }

    @Override
    public void clear() {
        Arrays.fill(values, (byte) 0L);
    }

    @Override
    public int get(int index, long[] arr, int off, int len) {
        assert len > 0 : "len must be > 0 (got " + len + ")";
        assert index >= 0 && index < valueCount;
        assert off + len <= arr.length;

        final int gets = Math.min(valueCount - index, len);
        for (int i = index, o = off, end = index + gets; i < end; ++i, ++o) {
            arr[o] = values[i] & 0xFFL;
        }
        return gets;
    }

    @Override
    public int set(int index, long[] arr, int off, int len) {
        assert len > 0 : "len must be > 0 (got " + len + ")";
        assert index >= 0 && index < valueCount;
        assert off + len <= arr.length;

        final int sets = Math.min(valueCount - index, len);
        for (int i = index, o = off, end = index + sets; i < end; ++i, ++o) {
            values[i] = (byte) arr[o];
        }
        return sets;
    }

    @Override
    public void fill(int fromIndex, int toIndex, long val) {
        assert val == (val & 0xFFL);
        Arrays.fill(values, fromIndex, toIndex, (byte) val);
    }
}
