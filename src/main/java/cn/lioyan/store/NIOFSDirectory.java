package cn.lioyan.store;

import java.io.IOException;
import java.nio.file.Path;

/**
 * {@link NIOFSDirectory}
 *
 * @author com.lioyan
 * @date 2023/5/26  18:04
 */
public class NIOFSDirectory extends FSDirectory {
    public NIOFSDirectory(Path directory) throws IOException {
        super(directory);
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        return null;
    }

    @Override
    public void rename(String source, String dest) throws IOException {

    }

    @Override
    public IndexInput openInput(String name, IOContext context) throws IOException {
        return null;
    }

    @Override
    public Lock obtainLock(String name) throws IOException {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
