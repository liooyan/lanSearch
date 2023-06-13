package cn.lioyan.store;

public class IOContext {
    public enum Context {
        MERGE, READ, FLUSH, DEFAULT
    };

    public static final IOContext READONCE = new IOContext(true);
    public static final IOContext DEFAULT = new IOContext(Context.DEFAULT);

    private IOContext(boolean readOnce) {
        this.context = Context.READ;
    }


    public final Context context;

    public IOContext(Context context) {
        this.context = context;
    }
}
