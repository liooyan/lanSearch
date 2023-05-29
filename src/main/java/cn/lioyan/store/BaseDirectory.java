package cn.lioyan.store;

import java.io.IOException;

public abstract class BaseDirectory implements Directory{


    protected final LockFactory lockFactory;

    /** Sole constructor. */
    protected BaseDirectory(LockFactory lockFactory) {
        super();
        if (lockFactory == null) {
            throw new NullPointerException("LockFactory must not be null, use an explicit instance!");
        }
        this.lockFactory = lockFactory;
    }

    @Override
    public final Lock obtainLock(String name) throws IOException {
        return lockFactory.obtainLock(this, name);
    }


    @Override
    public String toString() {
        return super.toString()  + " lockFactory=" + lockFactory;
    }
}
