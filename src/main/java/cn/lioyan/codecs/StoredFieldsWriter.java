package cn.lioyan.codecs;

import cn.lioyan.index.FieldInfo;
import cn.lioyan.index.FieldInfos;
import cn.lioyan.index.IndexableField;
import cn.lioyan.util.Accountable;

import java.io.Closeable;
import java.io.IOException;

/**
 * {@link StoredFieldsWriter}
 * 原始字段存储 抽象类
 * @author com.lioyan
 * @date 2023/6/6  9:37
 */
public abstract class StoredFieldsWriter implements Closeable, Accountable {

    protected StoredFieldsWriter() {
    }


    /**
     * 开始存储文档
     * @throws IOException
     */
    public abstract void startDocument() throws IOException;


    /**
     * 文档存储结束
     * @throws IOException
     */
    public void finishDocument() throws IOException {}


    /**
     * 写入当前字段
     * @param info
     * @param field
     * @throws IOException
     */
    public abstract void writeField(FieldInfo info, IndexableField field) throws IOException;


    /**
     * 刷新，落盘
     * @param fis
     * @param numDocs
     * @throws IOException
     */
    public abstract void finish(FieldInfos fis, int numDocs) throws IOException;
}
