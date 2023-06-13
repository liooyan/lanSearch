package cn.lioyan.index;

import cn.lioyan.codecs.Codec;
import cn.lioyan.codecs.StoredFieldsWriter;
import cn.lioyan.codecs.compressing.CompressingStoredFieldsFormat;
import cn.lioyan.store.Directory;
import cn.lioyan.store.IOContext;
import cn.lioyan.util.Accountable;
import cn.lioyan.util.IOUtils;

import java.io.IOException;

/**
 * {@link StoredFieldsConsumer}
 *  写文件原始数据的代理类，其中方法实现都是通过{@link CompressingStoredFieldsFormat}。并且其创建通过{@link Codec}
 * @author com.lioyan
 * @date 2023/6/13  10:44
 */
public class StoredFieldsConsumer {
    final Codec codec;
    final Directory directory;
    final SegmentInfo info;
    StoredFieldsWriter writer;
    // this accountable either holds the writer or one that returns null.
    // it's cleaner than checking if the writer is null all over the place
    Accountable accountable = Accountable.NULL_ACCOUNTABLE;
    private int lastDoc;

    StoredFieldsConsumer(Codec codec, Directory directory, SegmentInfo info) {
        this.codec = codec;
        this.directory = directory;
        this.info = info;
        this.lastDoc = -1;
    }

    protected void initStoredFieldsWriter() throws IOException {
        if (writer == null) { // TODO can we allocate this in the ctor? we call start document for every doc anyway
            this.writer = codec.storedFieldsFormat().fieldsWriter(directory, info, IOContext.DEFAULT);
            accountable = writer;
        }
    }

    void startDocument(int docID) throws IOException {
        assert lastDoc < docID;
        initStoredFieldsWriter();
        while (++lastDoc < docID) {
            writer.startDocument();
            writer.finishDocument();
        }
        writer.startDocument();
    }

    void writeField(FieldInfo info, IndexableField field) throws IOException {
        writer.writeField(info, field);
    }

    void finishDocument() throws IOException {
        writer.finishDocument();
    }

    void finish(int maxDoc) throws IOException {
        while (lastDoc < maxDoc-1) {
            startDocument(lastDoc);
            finishDocument();
            ++lastDoc;
        }
    }

    void flush(SegmentWriteState state, Sorter.DocMap sortMap) throws IOException {
        try {
            writer.finish(state.fieldInfos, state.segmentInfo.maxDoc());
        } finally {
            IOUtils.close(writer);
        }
    }

    void abort() {
        IOUtils.closeWhileHandlingException(writer);
    }
}
