package cn.lioyan.util;

import java.util.Collection;
import java.util.Collections;

/**
 * {@link Accountable}
 * 记录当前对象占用内存情况
 * @author com.lioyan
 * @date 2023/5/30  10:55
 */
public interface Accountable {
    /**
     * 占用内存情况，单位 byte
     *
     * @return
     */
    long ramBytesUsed();

    /**
     * Returns nested resources of this class.
     * The result should be a point-in-time snapshot (to avoid race conditions).
     */
    default Collection<Accountable> getChildResources() {
        return Collections.emptyList();
    }

    /**
     * An accountable that always returns 0
     */
    Accountable NULL_ACCOUNTABLE = () -> 0;
}
