package cn.lioyan.index;

import java.io.IOException;

/**
 * {@link SingletonSortedNumericDocValues}
 * 单值对多值的适配
 * @author com.lioyan
 * @date 2023/6/13  11:41
 */
public class SingletonSortedNumericDocValues  extends SortedNumericDocValues {
    private final NumericDocValues in;

    public SingletonSortedNumericDocValues(NumericDocValues in) {
        if (in.docID() != -1) {
            throw new IllegalStateException("iterator has already been used: docID=" + in.docID());
        }
        this.in = in;
    }

    /** Return the wrapped {@link NumericDocValues} */
    public NumericDocValues getNumericDocValues() {
        if (in.docID() != -1) {
            throw new IllegalStateException("iterator has already been used: docID=" + in.docID());
        }
        return in;
    }

    @Override
    public int docID() {
        return in.docID();
    }

    @Override
    public int nextDoc() throws IOException {
        return in.nextDoc();
    }

    @Override
    public int advance(int target) throws IOException {
        return in.advance(target);
    }

    @Override
    public boolean advanceExact(int target) throws IOException {
        return in.advanceExact(target);
    }

    @Override
    public long cost() {
        return in.cost();
    }

    @Override
    public long nextValue() throws IOException {
        return in.longValue();
    }

    @Override
    public int docValueCount() {
        return 1;
    }
}
