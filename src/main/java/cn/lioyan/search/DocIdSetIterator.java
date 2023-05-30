package cn.lioyan.search;

import java.io.IOException;

/**
 * {@link DocIdSetIterator}
 *  文档id 迭代器
 * @author com.lioyan
 * @date 2023/5/30  11:01
 */
public abstract  class DocIdSetIterator {

    /**
     * 表示未查询到
     */
    public static final int NO_MORE_DOCS = Integer.MAX_VALUE;
    /**
     * 返回下一个文档id，与nextDoc 配合使用
     * @return
     */
    public abstract int docID();

    /**
     * 判断当前迭代器，使用还有下一个元素
     *
     *
     * @return
     * @throws IOException
     */
    public abstract int nextDoc() throws IOException;


    /**
     * 将迭代器直接推进到指定节点，返回的结果 >= target
     *
     * @param target
     * @return
     * @throws IOException
     */
    public abstract int advance(int target) throws IOException;

    /**
     * 通过 nextDoc 实现的 advance 方法
     * @param target
     * @return
     * @throws IOException
     */
    protected final int slowAdvance(int target) throws IOException {
        assert docID() < target;
        int doc;
        do {
            doc = nextDoc();
        } while (doc < target);
        return doc;
    }

    /**
     * 返回当前集合的最大文档id
     * @return
     */
    public abstract long cost();


    /**
     *
     * 一个空的迭代器
     * @return
     */
    public static final DocIdSetIterator empty() {
        return new DocIdSetIterator() {
            boolean exhausted = false;

            @Override
            public int advance(int target) {
                assert !exhausted;
                assert target >= 0;
                exhausted = true;
                return NO_MORE_DOCS;
            }

            @Override
            public int docID() {
                return exhausted ? NO_MORE_DOCS : -1;
            }
            @Override
            public int nextDoc() {
                assert !exhausted;
                exhausted = true;
                return NO_MORE_DOCS;
            }

            @Override
            public long cost() {
                return 0;
            }
        };
    }

    /**
     * 一个保护 0 到 maxDoc 所有元素的 迭代器
     * @param maxDoc
     * @return
     */
    public static final DocIdSetIterator all(int maxDoc) {
        return new DocIdSetIterator() {
            int doc = -1;

            @Override
            public int docID() {
                return doc;
            }

            @Override
            public int nextDoc() throws IOException {
                return advance(doc + 1);
            }

            @Override
            public int advance(int target) throws IOException {
                doc = target;
                if (doc >= maxDoc) {
                    doc = NO_MORE_DOCS;
                }
                return doc;
            }

            @Override
            public long cost() {
                return maxDoc;
            }
        };
    }

    /**
     * 一个包含 minDoc ---  maxDoc 连续数据的迭代器
     * @param minDoc
     * @param maxDoc
     * @return
     */
    public static final DocIdSetIterator range(int minDoc, int maxDoc) {
        if (minDoc >= maxDoc) {
            throw new IllegalArgumentException("minDoc must be < maxDoc but got minDoc=" + minDoc + " maxDoc=" + maxDoc);
        }
        if (minDoc < 0) {
            throw new IllegalArgumentException("minDoc must be >= 0 but got minDoc=" + minDoc);
        }
        return new DocIdSetIterator() {
            private int doc = -1;

            @Override
            public int docID() {
                return doc;
            }

            @Override
            public int nextDoc() throws IOException {
                return advance(doc + 1);
            }

            @Override
            public int advance(int target) throws IOException {
                if (target < minDoc) {
                    doc = minDoc;
                } else if (target >= maxDoc) {
                    doc = NO_MORE_DOCS;
                } else {
                    doc = target;
                }
                return doc;
            }

            @Override
            public long cost() {
                return maxDoc - minDoc;
            }
        };
    }
}
