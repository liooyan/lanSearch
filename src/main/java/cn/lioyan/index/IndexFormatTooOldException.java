package cn.lioyan.index;

import cn.lioyan.store.DataInput;

import java.io.IOException;
import java.util.Objects;

/**
 * {@link IndexFormatTooOldException}
 *
 * @author com.lioyan
 * @date 2023/6/5  18:14
 */
public class IndexFormatTooOldException extends IOException {

    private final String resourceDescription;
    private final String reason;
    private final Integer version;
    private final Integer minVersion;
    private final Integer maxVersion;


    /** Creates an {@code IndexFormatTooOldException}.
     *
     *  @param resourceDescription describes the file that was too old
     *  @param reason the reason for this exception if the version is not available
     *
     * @lucene.internal */
    public IndexFormatTooOldException(String resourceDescription, String reason) {
        super("Format version is not supported (resource " + resourceDescription + "): " +
                reason + ". This version of Lucene only supports indexes created with release 7.0 and later.");
        this.resourceDescription = resourceDescription;
        this.reason = reason;
        this.version = null;
        this.minVersion = null;
        this.maxVersion = null;

    }

    /** Creates an {@code IndexFormatTooOldException}.
     *
     *  @param in the open file that's too old
     *  @param reason the reason for this exception if the version is not available
     *
     * @lucene.internal */
    public IndexFormatTooOldException(DataInput in, String reason) {
        this(Objects.toString(in), reason);
    }

    /** Creates an {@code IndexFormatTooOldException}.
     *
     *  @param resourceDescription describes the file that was too old
     *  @param version the version of the file that was too old
     *  @param minVersion the minimum version accepted
     *  @param maxVersion the maximum version accepted
     *
     * @lucene.internal */
    public IndexFormatTooOldException(String resourceDescription, int version, int minVersion, int maxVersion) {
        super("Format version is not supported (resource " + resourceDescription + "): " +
                version + " (needs to be between " + minVersion + " and " + maxVersion +
                "). This version of Lucene only supports indexes created with release 6.0 and later.");
        this.resourceDescription = resourceDescription;
        this.version = version;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.reason = null;
    }

    /** Creates an {@code IndexFormatTooOldException}.
     *
     *  @param in the open file that's too old
     *  @param version the version of the file that was too old
     *  @param minVersion the minimum version accepted
     *  @param maxVersion the maximum version accepted
     *
     * @lucene.internal */
    public IndexFormatTooOldException(DataInput in, int version, int minVersion, int maxVersion) {
        this(Objects.toString(in), version, minVersion, maxVersion);
    }

    /**
     * Returns a description of the file that was too old
     */
    public String getResourceDescription() {
        return resourceDescription;
    }

    /**
     * Returns an optional reason for this exception if the version information was not available. Otherwise <code>null</code>
     */
    public String getReason() {
        return reason;
    }

    /**
     * Returns the version of the file that was too old.
     * This method will return <code>null</code> if an alternative {@link #getReason()}
     * is provided.
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Returns the maximum version accepted.
     * This method will return <code>null</code> if an alternative {@link #getReason()}
     * is provided.
     */
    public Integer getMaxVersion() {
        return maxVersion;
    }

    /**
     * Returns the minimum version accepted
     * This method will return <code>null</code> if an alternative {@link #getReason()}
     * is provided.
     */
    public Integer getMinVersion() {
        return minVersion;
    }
}
