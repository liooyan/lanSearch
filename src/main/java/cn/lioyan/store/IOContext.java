package cn.lioyan.store;

public class IOContext {
    public enum Context {
        MERGE, READ, FLUSH, DEFAULT
    };


    public final Context context;

    public IOContext(Context context) {
        this.context = context;
    }
}
