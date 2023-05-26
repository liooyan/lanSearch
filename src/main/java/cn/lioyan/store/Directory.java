package cn.lioyan.store;

import java.io.Closeable;
import java.io.IOException;


/**
 * {@link Directory}
 *
 * @author com.lioyan
 * @date 2023/5/26  14:23
 */
public interface Directory extends Closeable {

    long fileLength(String name) throws IOException;

    String[] listAll() throws IOException;



     void deleteFile(String name) throws IOException;


    IndexOutput createOutput(String name, IOContext context) throws IOException;


     IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException;

    void rename(String source, String dest) throws IOException;


    public abstract IndexInput openInput(String name, IOContext context) throws IOException;


    Lock obtainLock(String name) throws IOException;



}
