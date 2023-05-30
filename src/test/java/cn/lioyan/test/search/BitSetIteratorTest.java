package cn.lioyan.test.search;

import cn.lioyan.search.DocIdSetIterator;
import cn.lioyan.store.DataOutput;
import cn.lioyan.util.BitDocIdSet;
import cn.lioyan.util.FixedBitSet;
import org.junit.Test;

import java.io.IOException;

/**
 * {@link BitSetIteratorTest}
 *
 * @author com.lioyan
 * @date 2023/5/30  14:13
 */
public class BitSetIteratorTest {
    @Test
    public void bitSetIteratorTest() throws IOException {

        long[] data = new long[]{7L,7L,7L,7L,7L};

        FixedBitSet fixedBitSet = new FixedBitSet(data,data.length*64);

        BitDocIdSet bitDocIdSet = new BitDocIdSet(fixedBitSet);
        DocIdSetIterator iterator = bitDocIdSet.iterator();

        int advance = iterator.advance(68);
        System.out.println(advance);

    }

}
