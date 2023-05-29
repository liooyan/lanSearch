package cn.lioyan.store;


import java.io.Closeable;
import java.io.IOException;

/**
 * 基于文件的  DataInput 抽象实现，
 * 可以通过 {@link Directory} 创建
 *
 * 同时实现随机读取文件位置的方法
 *
 *
 */
public abstract class IndexInput extends DataInput implements Cloneable, Closeable {
    private final String resourceDescription;

    protected IndexInput(String resourceDescription) {
        if (resourceDescription == null) {
            throw new IllegalArgumentException("resourceDescription must not be null");
        }
        this.resourceDescription = resourceDescription;
    }
    public abstract void seek(long pos) throws IOException;

    public abstract long length();

    protected String getFullSliceDescription(String sliceDescription) {
        if (sliceDescription == null) {
            // Clones pass null sliceDescription:
            return toString();
        } else {
            return toString() + " [slice=" + sliceDescription + "]";
        }
    }
}
