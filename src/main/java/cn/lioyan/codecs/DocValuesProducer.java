package cn.lioyan.codecs;

import cn.lioyan.index.*;

import java.io.IOException;

/**
 * {@link DocValuesProducer}
 * 包含不同数据类型的 {@link DocValuesIterator}
 * @author com.lioyan
 * @date 2023/6/13  11:20
 */
public abstract class DocValuesProducer {

    protected DocValuesProducer() {}
    public abstract NumericDocValues getNumeric(FieldInfo field) throws IOException;

    public abstract BinaryDocValues getBinary(FieldInfo field) throws IOException;



    public abstract void checkIntegrity() throws IOException;

    /**
     * Returns an instance optimized for merging. This instance may only be
     * consumed in the thread that called {@link #getMergeInstance()}.
     * <p>
     * The default implementation returns {@code this} */
    public DocValuesProducer getMergeInstance() {
        return this;
    }
}
