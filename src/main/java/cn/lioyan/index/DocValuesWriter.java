package cn.lioyan.index;

import cn.lioyan.codecs.DocValuesConsumer;
import cn.lioyan.search.DocIdSetIterator;

import java.io.IOException;

/**
 * {@link DocValuesWriter}
 * 提供获取 DocIdSetIterator 的方法
 * @author com.lioyan
 * @date 2023/6/14  11:04
 */
abstract class DocValuesWriter<T extends DocIdSetIterator> {
    /**
     * 将当前内容，写入到 DocValuesConsumer 中
     * @param state
     * @param sortMap
     * @param consumer
     * @throws IOException
     */
    abstract void flush(SegmentWriteState state, Sorter.DocMap sortMap, DocValuesConsumer consumer) throws IOException;

    /**
     * 获取 {@link DocIdSetIterator} 遍历id
     *
     * @return
     */
    abstract T getDocValues();
}

