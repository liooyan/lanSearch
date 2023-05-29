package cn.lioyan.store;

import java.io.IOException;

/**
 * 文件目录锁
 */
public abstract class Lock {
    /**
     * 释放锁
     * @throws IOException
     */
    public abstract void close() throws IOException;

    /**
     * 验证锁
     * @throws IOException
     */
    public abstract void ensureValid() throws IOException;
}
