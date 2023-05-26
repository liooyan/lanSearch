package cn.lioyan.store;

/**
 * {@link IOContext}
 *
 * @author com.lioyan
 * @date 2023/5/26  15:20
 */
public class IOContext {
    public IOContext(Context context) {
        this.context = context;
    }

    public enum Context {
        MERGE, READ, FLUSH, DEFAULT
    };

    public final Context context;




}
