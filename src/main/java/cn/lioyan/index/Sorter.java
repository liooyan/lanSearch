package cn.lioyan.index;

/**
 * {@link Sorter}
 * //TODO 未知
 * @author com.lioyan
 * @date 2023/6/13  10:54
 */
public class Sorter {

    static abstract class DocMap {

        /** Given a doc ID from the original index, return its ordinal in the
         *  sorted index. */
        abstract int oldToNew(int docID);

        /** Given the ordinal of a doc ID, return its doc ID in the original index. */
        abstract int newToOld(int docID);

        /** Return the number of documents in this map. This must be equal to the
         *  {@link org.apache.lucene.index.LeafReader#maxDoc() number of documents} of the
         *  {@link org.apache.lucene.index.LeafReader} which is sorted. */
        abstract int size();
    }
}
