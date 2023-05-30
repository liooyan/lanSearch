package cn.lioyan.util;

import cn.lioyan.search.DocIdSetIterator;

import java.io.IOException;

/**
 * {@link BitSet}
 * {@link Bits} 的基础实现
 * @author com.lioyan
 * @date 2023/5/30  13:43
 */
public abstract class BitSet implements Bits,Accountable {


    /**
     * 设置某个值
     * @param i
     */
    public abstract void set(int i);

    /**
     * 删除某个值
     * @param i
     */
    public abstract void clear(int i);


    /**
     * 返回指定索引的上一个值
     * @param index
     * @return
     */
    public abstract int prevSetBit(int index);


    /**
     * 返回指定索引的下一个值
     * @param index
     * @return
     */
    public abstract int nextSetBit(int index);
    public abstract int cardinality();

    public int approximateCardinality() {
        return cardinality();
    }

    protected final void checkUnpositioned(DocIdSetIterator iter) {
        if (iter.docID() != -1) {
            throw new IllegalStateException("This operation only works with an unpositioned iterator, got current position = " + iter.docID());
        }
    }

    /**
     * 迭代 DocIdSetIterator 将结果保存到当前集合中
     * @param iter
     * @throws IOException
     */
    public void or(DocIdSetIterator iter) throws IOException {
        checkUnpositioned(iter);
        for (int doc = iter.nextDoc(); doc != DocIdSetIterator.NO_MORE_DOCS; doc = iter.nextDoc()) {
            set(doc);
        }
    }
}
