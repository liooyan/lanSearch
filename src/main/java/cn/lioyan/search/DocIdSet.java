package cn.lioyan.search;

import cn.lioyan.util.Accountable;
import cn.lioyan.util.Bits;

import java.io.IOException;

/**
 * {@link DocIdSet}
 * 一个用于记录当前文档 id 的 set 集合，
 * 可以通过 iterator 迭代当前集合
 * @author com.lioyan
 * @date 2023/5/30  10:57
 */
public abstract class DocIdSet  implements Accountable {


    public abstract DocIdSetIterator iterator() throws IOException;

    /**
     * 返回 {@link Bits}
     * @return
     * @throws IOException
     */
    public Bits bits() throws IOException {
        return null;
    }


    public static final DocIdSet EMPTY = new DocIdSet() {

        @Override
        public DocIdSetIterator iterator() {
            return DocIdSetIterator.empty();
        }

        // we explicitly provide no random access, as this filter is 100% sparse and iterator exits faster
        @Override
        public Bits bits() {
            return null;
        }

        @Override
        public long ramBytesUsed() {
            return 0L;
        }
    };

}
