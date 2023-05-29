package cn.lioyan.store;

import java.io.Closeable;
import java.io.IOException;

/**
 * 文件夹操作方法，需要实现
 * 1、获取当前文件夹内的文件
 * 2、递归获取当前文件夹内文件
 * 3、附带过滤条件的获取
 * 4、删除指定类型的文件
 * 5、获取输入流
 * 6、获取输出流
 * 7、获取后缀增加的tmp文件。
 * 8、添加锁
 *
 */
public interface Directory extends Closeable {
    String[] listAll() throws IOException;

    void deleteFile(String name) throws IOException;

    long fileLength(String name) throws IOException;


    DataOutput createOutput(String name, IOContext context) throws IOException;



    DataOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException;



    void rename(String source, String dest) throws IOException;

    IndexInput openInput(String name, IOContext context) throws IOException;


     Lock obtainLock(String name) throws IOException;

    void close() throws IOException;


    void copyFrom(Directory from, String src, String dest, IOContext context) throws IOException;



}
