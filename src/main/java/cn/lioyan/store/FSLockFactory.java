package cn.lioyan.store;

import java.io.IOException;

/**
 * 文件锁
 */
public abstract  class FSLockFactory  extends  LockFactory{
    public static final FSLockFactory getDefault() {
        return NativeFSLockFactory.INSTANCE;
    }

    @Override
    public final Lock obtainLock(Directory dir, String lockName) throws IOException {
        if (!(dir instanceof FSDirectory)) {
            throw new UnsupportedOperationException(getClass().getSimpleName() + " can only be used with FSDirectory subclasses, got: " + dir);
        }
        return obtainFSLock((FSDirectory) dir, lockName);
    }

    /**
     * Implement this method to obtain a lock for a FSDirectory instance.
     * @throws IOException if the lock could not be obtained.
     */
    protected abstract Lock obtainFSLock(FSDirectory dir, String lockName) throws IOException;


}
