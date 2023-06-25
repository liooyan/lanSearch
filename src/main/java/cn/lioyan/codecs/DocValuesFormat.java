package cn.lioyan.codecs;

import cn.lioyan.index.SegmentWriteState;

import java.io.IOException;

/**
 * {@link DocValuesFormat}
 * 提供 获取 {@link DocValuesConsumer} 方法
 * @author com.lioyan
 * @date 2023/6/25  11:26
 */
public abstract class DocValuesFormat {

    public abstract DocValuesConsumer fieldsConsumer(SegmentWriteState state) throws IOException;

}
