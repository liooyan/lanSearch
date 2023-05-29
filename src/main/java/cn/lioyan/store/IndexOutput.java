package cn.lioyan.store;

import java.io.Closeable;
import java.io.IOException;

public abstract class IndexOutput extends DataOutput implements Closeable {
    /** Full description of this output, e.g. which class such as {@code FSIndexOutput}, and the full path to the file */
    private final String resourceDescription;

    /** Just the name part from {@code resourceDescription} */
    private final String name;

    /** Sole constructor.  resourceDescription should be non-null, opaque string
     *  describing this resource; it's returned from {@link #toString}. */
    protected IndexOutput(String resourceDescription, String name) {
        if (resourceDescription == null) {
            throw new IllegalArgumentException("resourceDescription must not be null");
        }
        this.resourceDescription = resourceDescription;
        this.name = name;
    }

    /** Returns the name used to create this {@code IndexOutput}.  This is especially useful when using
     * {@link Directory#createTempOutput}. */
    // TODO: can we somehow use this as the default resource description or something?
    public String getName() {
        return name;
    }

    /** Closes this stream to further operations. */
    @Override
    public abstract void close() throws IOException;

    /** Returns the current position in this file, where the next write will
     * occur.
     */
    public abstract long getFilePointer();

    /** Returns the current checksum of bytes written so far */
    public abstract long getChecksum() throws IOException;

    @Override
    public String toString() {
        return resourceDescription;
    }

}
