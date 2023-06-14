package cn.lioyan.util.packed;

import cn.lioyan.store.DataOutput;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

/**
 * {@link PackedWriter}
 * // TODO 不完整的
 * @author com.lioyan
 * @date 2023/6/14  10:21
 */
final class PackedWriter extends PackedInts.Writer {

    boolean finished;
    final PackedInts.Format format;
  //  final BulkOperation encoder;
    final byte[] nextBlocks = new byte[0];
    final long[] nextValues= new long[0];
    final int iterations= 0;
    int off;
    int written;

    PackedWriter(PackedInts.Format format, DataOutput out, int valueCount, int bitsPerValue, int mem) {
        super(out, valueCount, bitsPerValue);
        this.format = format;
    //    encoder = BulkOperation.of(format, bitsPerValue);
    //    iterations = encoder.computeIterations(valueCount, mem);
   //     nextBlocks = new byte[iterations * encoder.byteBlockCount()];
   //     nextValues = new long[iterations * encoder.byteValueCount()];
        off = 0;
        written = 0;
        finished = false;
    }

    @Override
    protected PackedInts.Format getFormat() {
        return format;
    }

    @Override
    public void add(long v) throws IOException {
        assert PackedInts.unsignedBitsRequired(v) <= bitsPerValue;
        assert !finished;
        if (valueCount != -1 && written >= valueCount) {
            throw new EOFException("Writing past end of stream");
        }
        nextValues[off++] = v;
        if (off == nextValues.length) {
            flush();
        }
        ++written;
    }

    @Override
    public void finish() throws IOException {
        assert !finished;
        if (valueCount != -1) {
            while (written < valueCount) {
                add(0L);
            }
        }
        flush();
        finished = true;
    }

    private void flush() throws IOException {
      //  encoder.encode(nextValues, 0, nextBlocks, 0, iterations);
        final int blockCount = (int) format.byteCount(PackedInts.VERSION_CURRENT, off, bitsPerValue);
        out.writeBytes(nextBlocks, blockCount);
        Arrays.fill(nextValues, 0L);
        off = 0;
    }

    @Override
    public int ord() {
        return written - 1;
    }
}
