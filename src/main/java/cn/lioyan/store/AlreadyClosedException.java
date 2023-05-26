package cn.lioyan.store;

/**
 * {@link AlreadyClosedException}
 *
 * @author com.lioyan
 * @date 2023/5/26  15:40
 */
public class AlreadyClosedException extends IllegalStateException {
    public AlreadyClosedException(String message) {
        super(message);
    }

    public AlreadyClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
