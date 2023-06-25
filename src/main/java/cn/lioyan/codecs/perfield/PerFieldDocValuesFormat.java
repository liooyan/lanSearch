package cn.lioyan.codecs.perfield;

import cn.lioyan.codecs.DocValuesConsumer;
import cn.lioyan.codecs.DocValuesFormat;
import cn.lioyan.codecs.DocValuesProducer;
import cn.lioyan.index.FieldInfo;
import cn.lioyan.index.SegmentWriteState;
import cn.lioyan.util.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * {@link PerFieldDocValuesFormat}
 *
 * @author com.lioyan
 * @date 2023/6/25  11:27
 */
public class PerFieldDocValuesFormat extends DocValuesFormat {
    @Override
    public final DocValuesConsumer fieldsConsumer(SegmentWriteState state) throws IOException {
        return new FieldsWriter(state);
    }

    private class FieldsWriter extends DocValuesConsumer {

        private final Map<DocValuesFormat,ConsumerAndSuffix> formats = new HashMap<>();
        private final Map<String,Integer> suffixes = new HashMap<>();

        private final SegmentWriteState segmentWriteState;

        public FieldsWriter(SegmentWriteState state) {
            segmentWriteState = state;
        }

        @Override
        public void addNumericField(FieldInfo field, DocValuesProducer valuesProducer) throws IOException {
            getInstance(field).addNumericField(field, valuesProducer);
        }

        @Override
        public void addBinaryField(FieldInfo field, DocValuesProducer valuesProducer) throws IOException {
            getInstance(field).addBinaryField(field, valuesProducer);
        }

        @Override
        public void addSortedField(FieldInfo field, DocValuesProducer valuesProducer) throws IOException {
            getInstance(field).addSortedField(field, valuesProducer);
        }

        @Override
        public void addSortedNumericField(FieldInfo field, DocValuesProducer valuesProducer) throws IOException {
            getInstance(field).addSortedNumericField(field, valuesProducer);
        }

        @Override
        public void addSortedSetField(FieldInfo field, DocValuesProducer valuesProducer) throws IOException {
            getInstance(field).addSortedSetField(field, valuesProducer);
        }


        private DocValuesConsumer getInstance(FieldInfo field) throws IOException {
            return getInstance(field, false);
        }

        /**
         * DocValuesConsumer for the given field.
         * @param field - FieldInfo object.
         * @param ignoreCurrentFormat - ignore the existing format attributes.
         * @return DocValuesConsumer for the field.
         * @throws IOException if there is a low-level IO error
         */
        // 调用的是 Lucene80DocValuesFormat 对象的 fieldsConsumer 方法
        private DocValuesConsumer getInstance(FieldInfo field, boolean ignoreCurrentFormat) throws IOException {

            // TODO: we should only provide the "slice" of FIS
            // that this DVF actually sees ...
            return null;
        }

    }


    static class ConsumerAndSuffix implements Closeable {
        DocValuesConsumer consumer;
        int suffix;

        @Override
        public void close() throws IOException {
           // consumer.close();
        }
    }

}
