package cn.lioyan.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public  class FSDirectory extends BaseDirectory{


    protected final Path directory;


    protected FSDirectory(Path path, LockFactory lockFactory) throws IOException {
        super(lockFactory);
        // If only read access is permitted, createDirectories fails even if the directory already exists.
        if (!Files.isDirectory(path)) {
            Files.createDirectories(path);  // create directory, if it doesn't exist
        }
        directory = path.toRealPath();
    }

    @Override
    public String[] listAll() throws IOException {
        return new String[0];
    }

    @Override
    public void deleteFile(String name) throws IOException {

    }

    @Override
    public long fileLength(String name) throws IOException {
        return 0;
    }

    @Override
    public DataOutput createOutput(String name, IOContext context) throws IOException {
        return null;
    }

    @Override
    public DataOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
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
    public void close() throws IOException {

    }

    @Override
    public void copyFrom(Directory from, String src, String dest, IOContext context) throws IOException {

    }
}
