package cn.lioyan.store;

import java.io.IOException;

public abstract class ChecksumIndexInput extends IndexInput {
    /** resourceDescription should be a non-null, opaque string
     *  describing this resource; it's returned from
     *  {@link #toString}. */
    protected ChecksumIndexInput(String resourceDescription) {
        super(resourceDescription);
    }

    /** Returns the current checksum value */
    public abstract long getChecksum() throws IOException;

}
