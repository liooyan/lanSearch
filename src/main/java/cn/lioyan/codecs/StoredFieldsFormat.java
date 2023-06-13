package cn.lioyan.codecs;

import cn.lioyan.index.SegmentInfo;
import cn.lioyan.store.Directory;
import cn.lioyan.store.IOContext;

import java.io.IOException;

/**
 * {@link StoredFieldsFormat}
 * 分别创建 {@link StoredFieldsWriter} 和 {@link StoredFieldsReader}
 *
 * 用于写与读取 原始数据
 * @author com.lioyan
 * @date 2023/6/13  10:47
 */
public abstract class StoredFieldsFormat {

    public abstract StoredFieldsWriter fieldsWriter(Directory directory, SegmentInfo si, IOContext context) throws IOException;
}
