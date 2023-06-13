package cn.lioyan.codecs.compressing;

import cn.lioyan.codecs.StoredFieldsFormat;
import cn.lioyan.codecs.StoredFieldsWriter;
import cn.lioyan.index.SegmentInfo;
import cn.lioyan.store.Directory;
import cn.lioyan.store.IOContext;
import cn.lioyan.util.packed.DirectMonotonicWriter;

import java.io.IOException;

/**
 * {@link CompressingStoredFieldsFormat}
 *
 * @author com.lioyan
 * @date 2023/6/13  10:48
 */
public class CompressingStoredFieldsFormat extends StoredFieldsFormat {
    private final String formatName;
    private final String segmentSuffix;
    private final CompressionMode compressionMode;
    private final int chunkSize;
    private final int maxDocsPerChunk;
    private final int blockShift;


    public CompressingStoredFieldsFormat(String formatName, String segmentSuffix,
                                         CompressionMode compressionMode, int chunkSize, int maxDocsPerChunk, int blockShift) {
        this.formatName = formatName;
        this.segmentSuffix = segmentSuffix;
        this.compressionMode = compressionMode;
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize must be >= 1");
        }
        this.chunkSize = chunkSize;
        if (maxDocsPerChunk < 1) {
            throw new IllegalArgumentException("maxDocsPerChunk must be >= 1");
        }
        this.maxDocsPerChunk = maxDocsPerChunk;
        if (blockShift < DirectMonotonicWriter.MIN_BLOCK_SHIFT || blockShift > DirectMonotonicWriter.MAX_BLOCK_SHIFT) {
            throw new IllegalArgumentException("blockSize must be in " + DirectMonotonicWriter.MIN_BLOCK_SHIFT + "-" +
                    DirectMonotonicWriter.MAX_BLOCK_SHIFT + ", got " + blockShift);
        }
        this.blockShift = blockShift;
    }


    @Override
    public StoredFieldsWriter fieldsWriter(Directory directory, SegmentInfo si,
                                           IOContext context) throws IOException {
        return new CompressingStoredFieldsWriter(directory, si, segmentSuffix, context,
                formatName, compressionMode, chunkSize, maxDocsPerChunk, blockShift);
    }
}
