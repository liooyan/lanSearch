package cn.lioyan.util.packed;

import cn.lioyan.store.IndexOutput;
import cn.lioyan.util.ArrayUtil;

import java.io.IOException;

/**
 * {@link DirectMonotonicWriter}
 * 对于分块存储的单调递增，数据， 计算每个块之间的平均斜率、最小值。
 *
 * @author com.lioyan
 * @date 2023/6/5  18:18
 */
public class DirectMonotonicWriter {

    public static final int MIN_BLOCK_SHIFT = 2;
    public static final int MAX_BLOCK_SHIFT = 22;

    final IndexOutput meta;
    final IndexOutput data;
    final long numValues;
    final long baseDataPointer;
    final long[] buffer;
    int bufferSize;
    long count;
    boolean finished;



    DirectMonotonicWriter(IndexOutput metaOut, IndexOutput dataOut, long numValues, int blockShift) {
        if (blockShift < MIN_BLOCK_SHIFT || blockShift > MAX_BLOCK_SHIFT) {
            throw new IllegalArgumentException("blockShift must be in [" + MIN_BLOCK_SHIFT + "-" + MAX_BLOCK_SHIFT + "], got " + blockShift);
        }
        if (numValues < 0) {
            throw new IllegalArgumentException("numValues can't be negative, got " + numValues);
        }
        final long numBlocks = numValues == 0 ? 0 : ((numValues - 1) >>> blockShift) + 1;
        if (numBlocks > ArrayUtil.MAX_ARRAY_LENGTH) {
            throw new IllegalArgumentException("blockShift is too low for the provided number of values: blockShift=" + blockShift +
                    ", numValues=" + numValues + ", MAX_ARRAY_LENGTH=" + ArrayUtil.MAX_ARRAY_LENGTH);
        }
        this.meta = metaOut;
        this.data = dataOut;
        this.numValues = numValues;
        final int blockSize = 1 << blockShift;
        this.buffer = new long[(int) Math.min(numValues, blockSize)];
        this.bufferSize = 0;
        this.baseDataPointer = dataOut.getFilePointer();
    }
    public static DirectMonotonicWriter getInstance(IndexOutput metaOut, IndexOutput dataOut, long numValues, int blockShift) {
        return new DirectMonotonicWriter(metaOut, dataOut, numValues, blockShift);
    }
    long previous = Long.MIN_VALUE;

    /**
     * 当前序列的值
     * @param v
     * @throws IOException
     */
    public void add(long v) throws IOException {
        if (v < previous) {
            throw new IllegalArgumentException("Values do not come in order: " + previous + ", " + v);
        }
        if (bufferSize == buffer.length) {
            flush();
        }
        buffer[bufferSize++] = v;
        previous = v;
        count++;
    }
    private void flush() throws IOException {


        // 计算斜率
        final float avgInc = (float) ((double) (buffer[bufferSize-1] - buffer[0]) / Math.max(1, bufferSize - 1));

        //根据斜率，算出 每个元素，与斜率的偏差
        for (int i = 0; i < bufferSize; ++i) {
            final long expected = (long) (avgInc * (long) i);
            buffer[i] -= expected;
        }

        //最小值
        long min = buffer[0];
        for (int i = 1; i < bufferSize; ++i) {
            min = Math.min(buffer[i], min);
        }

        // 减去最小值，并且算出，占用多少bit位
        long maxDelta = 0;
        for (int i = 0; i < bufferSize; ++i) {
            buffer[i] -= min;
            // use | will change nothing when it comes to computing required bits
            // but has the benefit of working fine with negative values too
            // (in case of overflow)
            maxDelta |= buffer[i];
        }


        //基本信息，写入meta中
        meta.writeLong(min);
        meta.writeInt(Float.floatToIntBits(avgInc));
        meta.writeLong(data.getFilePointer() - baseDataPointer);

        //写入data
        if (maxDelta == 0) {
            meta.writeByte((byte) 0);
        } else {
            for (int i = 0; i < bufferSize; ++i) {
                data.writeVLong(buffer[i]);
            }
        }

    }

    public void finish() throws IOException {
        if (count != numValues) {
            throw new IllegalStateException("Wrong number of values added, expected: " + numValues + ", got: " + count);
        }
        if (finished) {
            throw new IllegalStateException("#finish has been called already");
        }
        if (bufferSize > 0) {
            flush();
        }
        finished = true;
    }
}
