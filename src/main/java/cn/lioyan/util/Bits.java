package cn.lioyan.util;

/**
 * {@link Bits}
 * 以bit 位表示 的集合
 * @author com.lioyan
 * @date 2023/5/30  11:10
 */
public interface Bits {

    /**
     * 当前 index 下是否存储数据
     * @param index
     * @return
     */
    boolean get(int index);

    /**
     * Returns the number of bits in this set
     */
    int length();


    Bits[] EMPTY_ARRAY = new Bits[0];

    /**
     * Bits impl of the specified length with all bits set.
     */
    class MatchAllBits implements Bits {
        final int len;

        public MatchAllBits(int len) {
            this.len = len;
        }

        @Override
        public boolean get(int index) {
            return true;
        }

        @Override
        public int length() {
            return len;
        }
    }

    /**
     * Bits impl of the specified length with no bits set.
     */
    class MatchNoBits implements Bits {
        final int len;

        public MatchNoBits(int len) {
            this.len = len;
        }

        @Override
        public boolean get(int index) {
            return false;
        }

        @Override
        public int length() {
            return len;
        }
    }

}
