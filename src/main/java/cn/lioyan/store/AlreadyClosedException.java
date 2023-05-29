package cn.lioyan.store;

/**
 * 重复关闭异常
 */
public class AlreadyClosedException  extends IllegalStateException {
    public AlreadyClosedException(String message) {
        super(message);
    }

    public AlreadyClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}