package cn.lioyan.test.store;

import cn.lioyan.store.FSDirectory;
import cn.lioyan.store.IOContext;
import cn.lioyan.store.IndexInput;
import cn.lioyan.store.IndexOutput;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@link FSDirectoryTest}
 *
 * @author com.lioyan
 * @date 2023/5/30  10:34
 */
public class FSDirectoryTest {


    @Test
    public  void IndexOutputTest() throws IOException {
        IOContext ioContext = new IOContext(IOContext.Context.MERGE);
        FSDirectory fsDirectory = FSDirectory.open(Paths.get("C:\\work\\idea_work\\lanSearch\\target\\test"));
        IndexOutput output = fsDirectory.createOutput("11.txt", ioContext);


        output.writeInt(1);
        output.writeInt(1);
        output.writeInt(1);
        output.writeInt(1);
        output.close();

        IndexInput indexInput = fsDirectory.openInput("11.txt", ioContext);

        System.out.println(indexInput.readInt());
    }
}
