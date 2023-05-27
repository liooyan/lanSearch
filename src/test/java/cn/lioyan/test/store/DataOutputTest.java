package cn.lioyan.test.store;

import cn.lioyan.store.DataOutput;
import org.junit.Test;

import java.io.IOException;

public class DataOutputTest {

    @Test
    public void dataOutput() throws IOException {
        DataOutput dataOutput = new DataOutput() {
            @Override
            public void writeByte(byte b) throws IOException {
                System.out.println(b);
            }
        };

        dataOutput.writeVInt(0xff47);

    }

}
