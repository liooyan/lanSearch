package cn.lioyan.util;

/**
 * {@link LongValues}
 * long 类型数据集合， 可以根据index 获取数据
 * @author com.lioyan
 * @date 2023/6/13  16:21
 */
public abstract  class LongValues {

    public static final LongValues IDENTITY = new LongValues() {

        @Override
        public long get(long index) {
            return index;
        }

    };

    public static final LongValues ZEROES = new LongValues() {

        @Override
        public long get(long index) {
            return 0;
        }

    };

    public abstract long get(long index);
}
