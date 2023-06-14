package cn.lioyan.index;

import cn.lioyan.search.DocIdSetIterator;

import java.io.IOException;

/**
 * {@link DocValuesIterator}
 *
 * @author com.lioyan
 * @date 2023/6/13  11:20
 */
abstract class DocValuesIterator extends DocIdSetIterator {

    /**
     * 将迭代器推进到指定位置，并返回是否有值
     * @param target
     * @return
     * @throws IOException
     */
    public abstract boolean advanceExact(int target) throws IOException;
}
