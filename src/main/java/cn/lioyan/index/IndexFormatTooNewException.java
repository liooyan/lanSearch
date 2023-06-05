package cn.lioyan.index;

import cn.lioyan.store.DataInput;

import java.io.IOException;
import java.util.Objects;

/**
 * {@link IndexFormatTooNewException}
 *
 * @author com.lioyan
 * @date 2023/6/5  18:15
 */
public class IndexFormatTooNewException extends IOException {

    private final String resourceDescription;
    private final int version;
    private final int minVersion;
    private final int maxVersion;

    /** Creates an {@code IndexFormatTooNewException}
     *
     *  @param resourceDescription describes the file that was too new
     *  @param version the version of the file that was too new
     *  @param minVersion the minimum version accepted
     *  @param maxVersion the maximum version accepted
     *
     * @lucene.internal */
    public IndexFormatTooNewException(String resourceDescription, int version, int minVersion, int maxVersion) {
        super("Format version is not supported (resource " + resourceDescription + "): "
                + version + " (needs to be between " + minVersion + " and " + maxVersion + ")");
        this.resourceDescription = resourceDescription;
        this.version = version;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    /** Creates an {@code IndexFormatTooNewException}
     *
     *  @param in the open file that's too new
     *  @param version the version of the file that was too new
     *  @param minVersion the minimum version accepted
     *  @param maxVersion the maximum version accepted
     *
     * @lucene.internal */
    public IndexFormatTooNewException(DataInput in, int version, int minVersion, int maxVersion) {
        this(Objects.toString(in), version, minVersion, maxVersion);
    }

    /**
     * Returns a description of the file that was too new
     */
    public String getResourceDescription() {
        return resourceDescription;
    }

    /**
     * Returns the version of the file that was too new
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the maximum version accepted
     */
    public int getMaxVersion() {
        return maxVersion;
    }

    /**
     * Returns the minimum version accepted
     */
    public int getMinVersion() {
        return minVersion;
    }
}
