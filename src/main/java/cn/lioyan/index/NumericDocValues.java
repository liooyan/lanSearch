package cn.lioyan.index;

import java.io.IOException;

/**
 * {@link NumericDocValues}
 * 值为 Numeric 的 迭代器
 * @author com.lioyan
 * @date 2023/6/13  11:22
 */
public abstract class NumericDocValues extends DocValuesIterator {

    /** Sole constructor. (For invocation by subclass
     *  constructors, typically implicit.) */
    protected NumericDocValues() {}

    /**
     * Returns the numeric value for the current document ID.
     * It is illegal to call this method after {@link #advanceExact(int)}
     * returned {@code false}.
     * @return numeric value
     */
    public abstract long longValue() throws IOException;

}