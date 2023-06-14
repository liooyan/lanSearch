package cn.lioyan.index;

import cn.lioyan.util.BytesRef;

import java.io.IOException;

/**
 * {@link BinaryDocValues}
 * 值为 {@link BytesRef} 的 文档迭代器
 * @author com.lioyan
 * @date 2023/6/13  11:21
 */
public abstract class BinaryDocValues  extends DocValuesIterator {

    protected BinaryDocValues() {}

    /**
     * Returns the binary value for the current document ID.
     * It is illegal to call this method after {@link #advanceExact(int)}
     * returned {@code false}.
     * @return binary value
     */
    public abstract BytesRef binaryValue() throws IOException;
}
