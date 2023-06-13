package cn.lioyan.codecs.compressing;

import cn.lioyan.codecs.CodecUtil;
import cn.lioyan.codecs.StoredFieldsWriter;
import cn.lioyan.index.*;
import cn.lioyan.store.*;
import cn.lioyan.util.ArrayUtil;
import cn.lioyan.util.BitUtil;
import cn.lioyan.util.BytesRef;
import cn.lioyan.util.IOUtils;
import cn.lioyan.util.packed.PackedInts;

import java.io.IOException;

import static cn.lioyan.codecs.compressing.FieldsIndexWriter.VERSION_CURRENT;

/**
 * {@link CompressingStoredFieldsWriter}
 * 用于保存原始数据，包括： fdt、fdx、fdm
 *
 * @author com.lioyan
 * @date 2023/6/6  9:44
 */
public  class CompressingStoredFieldsWriter extends StoredFieldsWriter {

    public static final String FIELDS_EXTENSION = "fdt";
    /**
     * Extension of stored fields index
     */
    public static final String INDEX_EXTENSION = "fdx";
    /**
     * Extension of stored fields meta
     */
    public static final String META_EXTENSION = "fdm";
    /**
     * Codec name for the index.
     */
    public static final String INDEX_CODEC_NAME = "Lucene85FieldsIndex";



    static final int         STRING = 0x00;
    static final int       BYTE_ARR = 0x01;
    static final int    NUMERIC_INT = 0x02;
    static final int  NUMERIC_FLOAT = 0x03;
    static final int   NUMERIC_LONG = 0x04;
    static final int NUMERIC_DOUBLE = 0x05;

    //算出 NUMERIC_DOUBLE 占 几个bit
    static final int TYPE_BITS = PackedInts.bitsRequired(NUMERIC_DOUBLE);

    private long numDirtyChunks; // number of incomplete compressed blocks written
    private long numDirtyDocs; // cumulative number of missing docs in incomplete chunks
    /**
     * 当前段的名称
     */
    private final String segment;
    private final CompressionMode compressionMode;
    private final int chunkSize;
    private final int maxDocsPerChunk;
    //记录内存文档对应的字段个数
    private  int[] numStoredFields;
    // 记录，每个文档，结束时在bufferedDocs 的位置
    private  int[] endOffsets;
    private  int numBufferedDocs;
    private   IndexOutput metaStream = null;
    private IndexOutput fieldsStream = null;
    private FieldsIndexWriter indexWriter = null;

    //分块 的 数量
    private long numChunks;
    private final ByteBuffersDataOutput bufferedDocs;
    public static final int VERSION_MONOTONIC_WITHOUT_ZIGZAG = 2;
    public final static int VERSION_CURRENT = VERSION_MONOTONIC_WITHOUT_ZIGZAG;
    private int docBase;

    public CompressingStoredFieldsWriter(Directory directory, SegmentInfo si, String segmentSuffix, IOContext context,
                                         String formatName, CompressionMode compressionMode, int chunkSize, int maxDocsPerChunk, int blockShift) throws IOException {
        this.segment = si.name;
        this.compressionMode = compressionMode;
//        this.compressor = compressionMode.newCompressor();
        this.chunkSize = chunkSize;
        this.maxDocsPerChunk = maxDocsPerChunk;
        this.docBase = 0;
        this.bufferedDocs = ByteBuffersDataOutput.newResettableInstance();
        this.numStoredFields = new int[16];
        this.endOffsets = new int[16];
        this.numBufferedDocs = 0;


        boolean success = false;


        try {
            metaStream = directory.createOutput(IndexFileNames.segmentFileName(segment, segmentSuffix, META_EXTENSION), context);
            CodecUtil.writeIndexHeader(metaStream, INDEX_CODEC_NAME + "Meta", VERSION_CURRENT, si.getId(), segmentSuffix);

            fieldsStream = directory.createOutput(IndexFileNames.segmentFileName(segment, segmentSuffix, FIELDS_EXTENSION), context);
            CodecUtil.writeIndexHeader(fieldsStream, formatName, VERSION_CURRENT, si.getId(), segmentSuffix);

            //fdx 文件
            indexWriter = new FieldsIndexWriter(directory, segment, segmentSuffix, INDEX_EXTENSION, INDEX_CODEC_NAME, si.getId(), blockShift, context);

            metaStream.writeVInt(chunkSize);
            metaStream.writeVInt(VERSION_CURRENT);

            success = true;
        } finally {
            if (!success) {
                IOUtils.closeWhileHandlingException(metaStream, fieldsStream, indexWriter);
            }
        }

    }


    @Override
    public void startDocument() throws IOException {

    }

    private int numStoredFieldsInDoc;
    @Override
    public void writeField(FieldInfo info, IndexableField field) throws IOException {
        ++numStoredFieldsInDoc;


        int bits = 0;
        final BytesRef bytes;
        final String string;

        Number number = field.numericValue();

        if (number != null) {
            if (number instanceof Byte || number instanceof Short || number instanceof Integer) {
                bits = NUMERIC_INT;
            } else if (number instanceof Long) {
                bits = NUMERIC_LONG;
            } else if (number instanceof Float) {
                bits = NUMERIC_FLOAT;
            } else if (number instanceof Double) {
                bits = NUMERIC_DOUBLE;
            } else {
                throw new IllegalArgumentException("cannot store numeric type " + number.getClass());
            }
            string = null;
            bytes = null;
        } else {
            bytes = field.binaryValue();
            if (bytes != null) {
                bits = BYTE_ARR;
                string = null;
            } else {
                bits = STRING;
                string = field.stringValue();
                if (string == null) {
                    throw new IllegalArgumentException("field " + field.name() + " is stored but does not have binaryValue, stringValue nor numericValue");
                }
            }
        }
        // 后2位是表示字段类型，前面是编号
        final long infoAndBits = (((long) info.number) << TYPE_BITS) | bits;

        //存在内存中
        bufferedDocs.writeVLong(infoAndBits);

        if (bytes != null) {
            bufferedDocs.writeVInt(bytes.length);
            bufferedDocs.writeBytes(bytes.bytes, bytes.offset, bytes.length);
        } else if (string != null) {
            bufferedDocs.writeString(string);
        } else {
            if (number instanceof Byte || number instanceof Short || number instanceof Integer) {
                bufferedDocs.writeZInt(number.intValue());
            } else if (number instanceof Long) {
                writeTLong(bufferedDocs, number.longValue());
            } else if (number instanceof Float) {
                writeZFloat(bufferedDocs, number.floatValue());
            } else if (number instanceof Double) {
                writeZDouble(bufferedDocs, number.doubleValue());
            } else {
                throw new AssertionError("Cannot get here");
            }
        }

    }

    @Override
    public void finish(FieldInfos fis, int numDocs) throws IOException {
        if (numBufferedDocs > 0) {
            flush(true);
        } else {
            assert bufferedDocs.size() == 0;
        }
        if (docBase != numDocs) {
            throw new RuntimeException("Wrote " + docBase + " docs, finish called with numDocs=" + numDocs);
        }
        indexWriter.finish(numDocs, fieldsStream.getFilePointer(), metaStream);
        metaStream.writeVLong(numChunks);
        metaStream.writeVLong(numDirtyChunks);
        metaStream.writeVLong(numDirtyDocs);
        CodecUtil.writeFooter(metaStream);
        CodecUtil.writeFooter(fieldsStream);
        assert bufferedDocs.size() == 0;
    }

    /**
     * 存储完成一个文档时
     * @throws IOException
     */
    @Override
    public void finishDocument() throws IOException {
        if (numBufferedDocs == this.numStoredFields.length) {
            final int newLength = ArrayUtil.oversize(numBufferedDocs + 1, 4);
            this.numStoredFields = ArrayUtil.growExact(this.numStoredFields, newLength);
            endOffsets = ArrayUtil.growExact(endOffsets, newLength);
        }
        this.numStoredFields[numBufferedDocs] = numStoredFieldsInDoc;
        numStoredFieldsInDoc = 0;
        endOffsets[numBufferedDocs] = Math.toIntExact(bufferedDocs.size());
        ++numBufferedDocs;
        //判断是否需要刷新
        if (triggerFlush()) {
            flush(false);
        }
    }

    /**
     * 判断是否需要刷新，依据： 当前内存零时存储大小，文档的数量
     * @return
     */
    private boolean triggerFlush() {
        return bufferedDocs.size() >= chunkSize || // chunks of at least chunkSize bytes
                numBufferedDocs >= maxDocsPerChunk;
    }

    private void flush(boolean force) throws IOException {
        assert triggerFlush() != force;
        numChunks++;

        //通过 force 判断是否是一个完整的快
        if (force) {
            numDirtyChunks++; // incomplete: we had to force this flush
            numDirtyDocs += numBufferedDocs;
        }

        // 记录当前块的结束文档id 与 开始时在原文位置
        indexWriter.writeIndex(numBufferedDocs, fieldsStream.getFilePointer());

        // 将endOffsets 转换为差值
        final int[] lengths = endOffsets;
        for (int i = numBufferedDocs - 1; i > 0; --i) {
            lengths[i] = endOffsets[i] - endOffsets[i - 1];
            assert lengths[i] >= 0;
        }

        final boolean sliced = bufferedDocs.size() >= 2 * chunkSize;
        final boolean dirtyChunk = force;
        // 保存 numStoredFields 与 lengths
        writeHeader(docBase, numBufferedDocs, numStoredFields, lengths, sliced, dirtyChunk);


        byte [] content = bufferedDocs.toArrayCopy();
        bufferedDocs.reset();
        // 将数据写入到文档中
        //TODO  写入
        if (sliced) {
            // big chunk, slice it
            for (int compressed = 0; compressed < content.length; compressed += chunkSize) {
              //  compressor.compress(content, compressed, Math.min(chunkSize, content.length - compressed), fieldsStream);
            }
        } else {
           // compressor.compress(content, 0, content.length, fieldsStream);
        }

        // reset
        docBase += numBufferedDocs;
        numBufferedDocs = 0;
        bufferedDocs.reset();
    }


    private void writeHeader(int docBase, int numBufferedDocs, int[] numStoredFields,
                             int[] lengths, boolean sliced, boolean dirtyChunk) throws IOException {
        final int slicedBit = sliced ? 1 : 0;
        final int dirtyBit = dirtyChunk ? 2 : 0;

        // save docBase and numBufferedDocs
        fieldsStream.writeVInt(docBase);
        fieldsStream.writeVInt((numBufferedDocs << 2) | dirtyBit | slicedBit);

        // save numStoredFields
        saveInts(numStoredFields, numBufferedDocs, fieldsStream);

        // save lengths
        saveInts(lengths, numBufferedDocs, fieldsStream);
    }


    private static void saveInts(int[] values, int length, DataOutput out) throws IOException {
        for (int value : values) {
            out.writeVInt(value);
        }
//        assert length > 0;
//        if (length == 1) {
//            out.writeVInt(values[0]);
//        } else {
//            boolean allEqual = true;
//            for (int i = 1; i < length; ++i) {
//                if (values[i] != values[0]) {
//                    allEqual = false;
//                    break;
//                }
//            }
//            if (allEqual) {
//                out.writeVInt(0);
//                out.writeVInt(values[0]);
//            } else {
//                long max = 0;
//                for (int i = 0; i < length; ++i) {
//                    max |= values[i];
//                }
//                final int bitsRequired = PackedInts.bitsRequired(max);
//                out.writeVInt(bitsRequired);
//                final PackedInts.Writer w = PackedInts.getWriterNoHeader(out, PackedInts.Format.PACKED, length, bitsRequired, 1);
//                for (int i = 0; i < length; ++i) {
//                    w.add(values[i]);
//                }
//                w.finish();
//            }
//        }
    }

    // for compression of timestamps
    static final long SECOND = 1000L;
    static final long HOUR = 60 * 60 * SECOND;
    static final long DAY = 24 * HOUR;
    static final int SECOND_ENCODING = 0x40;
    static final int HOUR_ENCODING = 0x80;
    static final int DAY_ENCODING = 0xC0;



    static void writeZFloat(DataOutput out, float f) throws IOException {
        int intVal = (int) f;
        final int floatBits = Float.floatToIntBits(f);

        if (f == intVal
                && intVal >= -1
                && intVal <= 0x7D
                && floatBits != NEGATIVE_ZERO_FLOAT) {
            // small integer value [-1..125]: single byte
            out.writeByte((byte) (0x80 | (1 + intVal)));
        } else if ((floatBits >>> 31) == 0) {
            // other positive floats: 4 bytes
            out.writeInt(floatBits);
        } else {
            // other negative float: 5 bytes
            out.writeByte((byte) 0xFF);
            out.writeInt(floatBits);
        }
    }

    static void writeTLong(DataOutput out, long l) throws IOException {
        int header;
        if (l % SECOND != 0) {
            header = 0;
        } else if (l % DAY == 0) {
            // timestamp with day precision
            header = DAY_ENCODING;
            l /= DAY;
        } else if (l % HOUR == 0) {
            // timestamp with hour precision, or day precision with a timezone
            header = HOUR_ENCODING;
            l /= HOUR;
        } else {
            // timestamp with second precision
            header = SECOND_ENCODING;
            l /= SECOND;
        }

        final long zigZagL = BitUtil.zigZagEncode(l);
        header |= (zigZagL & 0x1F); // last 5 bits
        final long upperBits = zigZagL >>> 5;
        if (upperBits != 0) {
            header |= 0x20;
        }
        out.writeByte((byte) header);
        if (upperBits != 0) {
            out.writeVLong(upperBits);
        }
    }
    static final int NEGATIVE_ZERO_FLOAT = Float.floatToIntBits(-0f);
    static final long NEGATIVE_ZERO_DOUBLE = Double.doubleToLongBits(-0d);
    static void writeZDouble(DataOutput out, double d) throws IOException {
        int intVal = (int) d;
        final long doubleBits = Double.doubleToLongBits(d);

        if (d == intVal &&
                intVal >= -1 &&
                intVal <= 0x7C &&
                doubleBits != NEGATIVE_ZERO_DOUBLE) {
            // small integer value [-1..124]: single byte
            out.writeByte((byte) (0x80 | (intVal + 1)));
            return;
        } else if (d == (float) d) {
            // d has an accurate float representation: 5 bytes
            out.writeByte((byte) 0xFE);
            out.writeInt(Float.floatToIntBits((float) d));
        } else if ((doubleBits >>> 63) == 0) {
            // other positive doubles: 8 bytes
            out.writeLong(doubleBits);
        } else {
            // other negative doubles: 9 bytes
            out.writeByte((byte) 0xFF);
            out.writeLong(doubleBits);
        }
    }


    @Override
    public long ramBytesUsed() {
        return bufferedDocs.ramBytesUsed() + numStoredFields.length * Integer.BYTES + endOffsets.length * Integer.BYTES;
    }

    @Override
    public void close() throws IOException {
        try {
            IOUtils.close(metaStream, fieldsStream, indexWriter);
        } finally {
            metaStream = null;
            fieldsStream = null;
            indexWriter = null;
//            compressor = null;
        }
    }
}
