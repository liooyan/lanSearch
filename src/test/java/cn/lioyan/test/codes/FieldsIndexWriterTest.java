package cn.lioyan.test.codes;

import cn.lioyan.codecs.compressing.FieldsIndexWriter;
import cn.lioyan.store.FSDirectory;
import cn.lioyan.store.IOContext;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * {@link FieldsIndexWriterTest}
 *
 * @author com.lioyan
 * @date 2023/6/5  17:22
 */
public class FieldsIndexWriterTest {


    public static void main(String[] args) throws IOException {
        IOContext ioContext = new IOContext(IOContext.Context.MERGE);
        FSDirectory fsDirectory = FSDirectory.open(Paths.get("C:\\work\\idea_work\\lanSearch\\target\\test"));
        FieldsIndexWriter fieldsIndexWriter = new FieldsIndexWriter(fsDirectory,"_9","","fdx","Lucene85FieldsIndex",new byte[]{1,2,3,4,5,6,7,8,9,10},10,ioContext);
        fieldsIndexWriter.close();
    }
}
