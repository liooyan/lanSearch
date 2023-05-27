package cn.lioyan.test.util;

import cn.lioyan.util.BytesRef;
import org.junit.Test;

public class BytesRefTest {


    @Test
    public void  newBytes(){
        String text = "cat is running";
        BytesRef bytes = new BytesRef(text.getBytes(),3,7);

        System.out.println(bytes.utf8ToString());

    }
    @Test
    public void  BytesFor(){
        String text = "cat is running";
        BytesRef bytes = new BytesRef(text.getBytes(),3,7);

        for (Byte data : bytes) {
            System.out.println(data);
        }

    }


}
