package cn.lioyan.index;

import cn.lioyan.search.DocIdSet;
import cn.lioyan.search.DocIdSetIterator;
import cn.lioyan.util.BitSetIterator;
import cn.lioyan.util.FixedBitSet;
import cn.lioyan.util.RamUsageEstimator;

/**
 * {@link DocsWithFieldSet}
 * 具有累加器的 {@link DocIdSet}
 * 当数据时从 0 到 x 连续时，使用 cost 累加器记录
 * 当数据不连续时，使用 {@link FixedBitSet} 记录
 * @author com.lioyan
 * @date 2023/5/30  15:33
 */
public class DocsWithFieldSet  extends DocIdSet {


    private static long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(DocsWithFieldSet.class);

    private FixedBitSet set;
    private int cost = 0;
    private int lastDocId = -1;

    void add(int docID) {
        if (docID <= lastDocId) {
            throw new IllegalArgumentException("Out of order doc ids: last=" + lastDocId + ", next=" + docID);
        }
        if (set != null) {
            set = FixedBitSet.ensureCapacity(set, docID);
            set.set(docID);
        } else if (docID != cost) {
            // migrate to a sparse encoding using a bit set
            set = new FixedBitSet(docID + 1);
            set.set(0, cost);
            set.set(docID);
        }
        lastDocId = docID;
        cost++;
    }

    @Override
    public long ramBytesUsed() {
        return BASE_RAM_BYTES_USED + (set == null ? 0 : set.ramBytesUsed());
    }

    @Override
    public DocIdSetIterator iterator() {
        return set != null ? new BitSetIterator(set, cost) : DocIdSetIterator.all(cost);
    }

}
