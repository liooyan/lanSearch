package cn.lioyan.index;

import cn.lioyan.store.DataInput;
import cn.lioyan.store.DataOutput;

import java.io.IOException;
import java.util.Objects;

/**
 * {@link CorruptIndexException}
 *
 * @author com.lioyan
 * @date 2023/6/5  18:14
 */
public class CorruptIndexException extends IOException {

    private final String message;
    private final String resourceDescription;

    /** Create exception with a message only */
    public CorruptIndexException(String message, DataInput input) {
        this(message, input, null);
    }

    /** Create exception with a message only */
    public CorruptIndexException(String message, DataOutput output) {
        this(message, output, null);
    }

    /** Create exception with message and root cause. */
    public CorruptIndexException(String message, DataInput input, Throwable cause) {
        this(message, Objects.toString(input), cause);
    }

    /** Create exception with message and root cause. */
    public CorruptIndexException(String message, DataOutput output, Throwable cause) {
        this(message, Objects.toString(output), cause);
    }

    /** Create exception with a message only */
    public CorruptIndexException(String message, String resourceDescription) {
        this(message, resourceDescription, null);
    }

    /** Create exception with message and root cause. */
    public CorruptIndexException(String message, String resourceDescription, Throwable cause) {
        super(Objects.toString(message) + " (resource=" + resourceDescription + ")", cause);
        this.resourceDescription = resourceDescription;
        this.message = message;
    }

    /**
     * Returns a description of the file that was corrupted
     */
    public String getResourceDescription() {
        return resourceDescription;
    }

    /**
     * Returns the original exception message without the corrupted file description.
     */
    public String getOriginalMessage() {
        return message;
    }
}