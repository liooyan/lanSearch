package cn.lioyan.store;

import java.io.IOException;
import java.nio.file.Path;

public class NIOFSDirectory extends FSDirectory{
    public NIOFSDirectory(Path path, LockFactory lockFactory) throws IOException {
        super(path, lockFactory);
    }
    public NIOFSDirectory(Path path) throws IOException {
        super(path);
    }


    @Override
    public IndexInput openInput(String name, IOContext context) throws IOException {
        return null;
    }
}
