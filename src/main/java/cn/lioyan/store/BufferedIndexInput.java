package cn.lioyan.store;

public abstract class BufferedIndexInput  extends IndexInput implements RandomAccessInput{
    protected BufferedIndexInput(String resourceDescription) {
        super(resourceDescription);
    }
}
