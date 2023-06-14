package cn.lioyan.index;

import java.io.IOException;

/**
 * {@link DocValues}
 * 不同类型数据 的 适配
 * @author com.lioyan
 * @date 2023/6/13  11:31
 */
public class DocValues {


//    ----------------------------NumericDocValues---------------------------

    /**
     * 空 Numeric
     * @return
     */
    public static final NumericDocValues emptyNumeric() {
        return new NumericDocValues() {
            private int doc = -1;

            @Override
            public int advance(int target) {
                return doc = NO_MORE_DOCS;
            }

            @Override
            public boolean advanceExact(int target) throws IOException {
                doc = target;
                return false;
            }

            @Override
            public int docID() {
                return doc;
            }

            @Override
            public int nextDoc() {
                return doc = NO_MORE_DOCS;
            }

            @Override
            public long cost() {
                return 0;
            }

            @Override
            public long longValue() {
                assert false;
                return 0;
            }
        };
    }



    public static NumericDocValues unwrapSingleton(SortedNumericDocValues dv) {
        if (dv instanceof SingletonSortedNumericDocValues) {
            return ((SingletonSortedNumericDocValues)dv).getNumericDocValues();
        } else {
            return null;
        }
    }

    public static SortedNumericDocValues singleton(NumericDocValues dv) {
        return new SingletonSortedNumericDocValues(dv);
    }

}
