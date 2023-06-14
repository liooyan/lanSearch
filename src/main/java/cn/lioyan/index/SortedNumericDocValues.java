package cn.lioyan.index;

import java.io.IOException;

/**
 * {@link SortedNumericDocValues}
 * 多个值的  {@link NumericDocValues}
 * @author com.lioyan
 * @date 2023/6/13  11:40
 */
public abstract class SortedNumericDocValues extends DocValuesIterator {

    /** Sole constructor. (For invocation by subclass
     *  constructors, typically implicit.) */
    protected SortedNumericDocValues() {}

    /**
     * 获取下一个值
     */
    public abstract long nextValue() throws IOException;

    /**
     * 当前id 有几个值
     */
    public abstract int docValueCount();
}