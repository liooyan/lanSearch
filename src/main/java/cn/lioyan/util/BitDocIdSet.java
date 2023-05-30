package cn.lioyan.util;

import cn.lioyan.search.DocIdSet;
import cn.lioyan.search.DocIdSetIterator;

import java.io.IOException;

/**
 * {@link BitDocIdSet}
 * 使用 {@link  Bits} 存储 {@link DocIdSet}的 实现
 * @author com.lioyan
 * @date 2023/5/30  13:42
 */
public class BitDocIdSet extends DocIdSet {
    @Override
    public DocIdSetIterator iterator() throws IOException {
        return null;
    }

    @Override
    public long ramBytesUsed() {
        return 0;
    }
}
