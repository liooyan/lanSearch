package cn.lioyan.codecs;

import cn.lioyan.index.BinaryDocValues;
import cn.lioyan.index.FieldInfo;
import cn.lioyan.index.NumericDocValues;

import java.io.IOException;

/**
 * {@link EmptyDocValuesProducer}
 *
 * @author com.lioyan
 * @date 2023/6/15  11:17
 */
public class EmptyDocValuesProducer extends DocValuesProducer{
    @Override
    public NumericDocValues getNumeric(FieldInfo field) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BinaryDocValues getBinary(FieldInfo field) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkIntegrity() throws IOException {

    }
}
