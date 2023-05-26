package cn.lioyan.test.store;

import cn.lioyan.store.IndexOutput;
import cn.lioyan.store.NIOFSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * {@link FSDirectory}
 *
 * @author com.lioyan
 * @date 2023/5/26  18:02
 */
public class FSDirectoryTest {


    public static void main(String[] args) throws IOException {
        NIOFSDirectory niofsDirectory = new NIOFSDirectory(Paths.get("C:\\work\\idea_work\\lanSearch\\target\\test"));

        IndexOutput tempOutput = niofsDirectory.createTempOutput("asd", "asd", null);
        tempOutput.writeString("asdadafffa");
        tempOutput.close();
    }
}
