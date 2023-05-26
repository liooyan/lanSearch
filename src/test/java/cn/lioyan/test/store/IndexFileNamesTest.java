package cn.lioyan.test.store;

import cn.lioyan.store.IndexFileNames;
import org.junit.Test;

/**
 * {@link IndexFileNamesTest}
 *
 * @author com.lioyan
 * @date 2023/5/26  17:30
 */
public class IndexFileNamesTest {

    @Test
    public  void segmentFileNameTest(){
        String s = IndexFileNames.segmentFileName("run", "324", "tmp");
        System.out.println(s);
    }

}
