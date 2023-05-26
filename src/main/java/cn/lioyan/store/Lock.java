package cn.lioyan.store;

import java.io.Closeable;
import java.io.IOException;

/**
 * {@link Lock}
 *
 * @author com.lioyan
 * @date 2023/5/26  15:45
 */
public abstract class Lock  implements Closeable {
    public abstract void close() throws IOException;

    /**
     * Best effort check that this lock is still valid. Locks
     * could become invalidated externally for a number of reasons,
     * for example if a user deletes the lock file manually or
     * when a network filesystem is in use.
     * @throws IOException if the lock is no longer valid.
     */
    public abstract void ensureValid() throws IOException;
}
