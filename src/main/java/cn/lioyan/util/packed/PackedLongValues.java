package cn.lioyan.util.packed;

import cn.lioyan.util.Accountable;
import cn.lioyan.util.ArrayUtil;
import cn.lioyan.util.LongValues;
import cn.lioyan.util.RamUsageEstimator;

import static cn.lioyan.util.packed.PackedInts.checkBlockSize;

/**
 * {@link PackedLongValues}
 * 压缩的 long 集合,可以直接通过索引 获取对于的long类型数据
 * @author com.lioyan
 * @date 2023/6/13  16:22
 */
public class PackedLongValues extends LongValues implements Accountable {

    private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(PackedLongValues.class);
    static final int DEFAULT_PAGE_SIZE = 256;
    static final int MIN_PAGE_SIZE = 64;
    // More than 1M doesn't really makes sense with these appending buffers
    // since their goal is to try to have small numbers of bits per value
    static final int MAX_PAGE_SIZE = 1 << 20;


    public static PackedLongValues.Builder packedBuilder(int pageSize, float acceptableOverheadRatio) {
        return new PackedLongValues.Builder(pageSize, acceptableOverheadRatio);
    }  public static PackedLongValues.Builder deltaPackedBuilder(float acceptableOverheadRatio) {
        return deltaPackedBuilder(DEFAULT_PAGE_SIZE, acceptableOverheadRatio);
    }
    public static PackedLongValues.Builder deltaPackedBuilder(int pageSize, float acceptableOverheadRatio) {
       // return new DeltaPackedLongValues.Builder(pageSize, acceptableOverheadRatio);
    return null;
    }


    final PackedInts.Reader[] values;
    final int pageShift, pageMask;
    private final long size;
    private final long ramBytesUsed;

    PackedLongValues(int pageShift, int pageMask, PackedInts.Reader[] values, long size, long ramBytesUsed) {
        this.pageShift = pageShift;
        this.pageMask = pageMask;
        this.values = values;
        this.size = size;
        this.ramBytesUsed = ramBytesUsed;
    }

    /** Get the number of values in this array. */
    public final long size() {
        return size;
    }

    int decodeBlock(int block, long[] dest) {
        final PackedInts.Reader vals = values[block];
        final int size = vals.size();
        for (int k = 0; k < size; ) {
            k += vals.get(k, dest, k, size - k);
        }
        return size;
    }

    long get(int block, int element) {
        return values[block].get(element);
    }

    @Override
    public final long get(long index) {
        assert index >= 0 && index < size();
        final int block = (int) (index >> pageShift);
        final int element = (int) (index & pageMask);
        return get(block, element);
    }

    @Override
    public long ramBytesUsed() {
        return ramBytesUsed;
    }

    /** Return an iterator over the values of this array. */
    public Iterator iterator() {
        return new Iterator();
    }

    /** An iterator over long values. */
    final public class Iterator {

        final long[] currentValues;
        int vOff, pOff;
        int currentCount; // number of entries of the current page

        Iterator() {
            currentValues = new long[pageMask + 1];
            vOff = pOff = 0;
            fillBlock();
        }

        private void fillBlock() {
            if (vOff == values.length) {
                currentCount = 0;
            } else {
                currentCount = decodeBlock(vOff, currentValues);
                assert currentCount > 0;
            }
        }

        /** Whether or not there are remaining values. */
        public final boolean hasNext() {
            return pOff < currentCount;
        }

        /** Return the next long in the buffer. */
        public final long next() {
            assert hasNext();
            long result = currentValues[pOff++];
            if (pOff == currentCount) {
                vOff += 1;
                pOff = 0;
                fillBlock();
            }
            return result;
        }

    }




    public static class Builder implements Accountable {

        private static final int INITIAL_PAGE_COUNT = 16;
        private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(Builder.class);

        final int pageShift, pageMask;
        final float acceptableOverheadRatio;
        long[] pending;
        long size;

        PackedInts.Reader[] values;
        long ramBytesUsed;
        int valuesOff;
        int pendingOff;

        Builder(int pageSize, float acceptableOverheadRatio) {
            pageShift = checkBlockSize(pageSize, MIN_PAGE_SIZE, MAX_PAGE_SIZE);
            pageMask = pageSize - 1;
            this.acceptableOverheadRatio = acceptableOverheadRatio;
            values = new PackedInts.Reader[INITIAL_PAGE_COUNT];
            pending = new long[pageSize];
            valuesOff = 0;
            pendingOff = 0;
            size = 0;
            ramBytesUsed = baseRamBytesUsed() + RamUsageEstimator.sizeOf(pending) + RamUsageEstimator.shallowSizeOf(values);
        }

        /** Build a {@link PackedLongValues} instance that contains values that
         *  have been added to this builder. This operation is destructive. */
        public PackedLongValues build() {
            finish();
            pending = null;
            final PackedInts.Reader[] values = ArrayUtil.copyOfSubArray(this.values, 0, valuesOff);
            final long ramBytesUsed = PackedLongValues.BASE_RAM_BYTES_USED;// + RamUsageEstimator.sizeOf(values);
            return new PackedLongValues(pageShift, pageMask, values, size, ramBytesUsed);
        }

        long baseRamBytesUsed() {
            return BASE_RAM_BYTES_USED;
        }

        @Override
        public final long ramBytesUsed() {
            return ramBytesUsed;
        }

        /** Return the number of elements that have been added to this builder. */
        public final long size() {
            return size;
        }

        /** Add a new element to this builder. */
        public Builder add(long l) {
            if (pending == null) {
                throw new IllegalStateException("Cannot be reused after build()");
            }
            if (pendingOff == pending.length) {
                // check size
                if (values.length == valuesOff) {
                    final int newLength = ArrayUtil.oversize(valuesOff + 1, 8);
                    grow(newLength);
                }
                pack();
            }
            pending[pendingOff++] = l;
            size += 1;
            return this;
        }

        final void finish() {
            if (pendingOff > 0) {
                if (values.length == valuesOff) {
                    grow(valuesOff + 1);
                }
                pack();
            }
        }

        private void pack() {
            pack(pending, pendingOff, valuesOff, acceptableOverheadRatio);
            ramBytesUsed += values[valuesOff].ramBytesUsed();
            valuesOff += 1;
            // reset pending buffer
            pendingOff = 0;
        }

        void pack(long[] values, int numValues, int block, float acceptableOverheadRatio) {
            assert numValues > 0;
            // compute max delta
            long minValue = values[0];
            long maxValue = values[0];
            for (int i = 1; i < numValues; ++i) {
                minValue = Math.min(minValue, values[i]);
                maxValue = Math.max(maxValue, values[i]);
            }

            // build a new packed reader
            if (minValue == 0 && maxValue == 0) {
                this.values[block] = new PackedInts.NullReader(numValues);
            } else {
                final int bitsRequired = minValue < 0 ? 64 : PackedInts.bitsRequired(maxValue);
                final PackedInts.Mutable mutable = PackedInts.getMutable(numValues, bitsRequired, acceptableOverheadRatio);
                for (int i = 0; i < numValues; ) {
                    i += mutable.set(i, values, i, numValues - i);
                }
                this.values[block] = mutable;
            }
        }

        void grow(int newBlockCount) {
            ramBytesUsed -= RamUsageEstimator.shallowSizeOf(values);
            values = ArrayUtil.growExact(values, newBlockCount);
            ramBytesUsed += RamUsageEstimator.shallowSizeOf(values);
        }

    }
}
