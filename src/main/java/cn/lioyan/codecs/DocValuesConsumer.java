package cn.lioyan.codecs;

import cn.lioyan.index.FieldInfo;

import java.io.IOException;

/**
 * {@link DocValuesConsumer}
 *
 * @author com.lioyan
 * @date 2023/6/14  11:04
 */
public abstract class DocValuesConsumer {

    /**
     * Writes numeric docvalues for a field.
     * @param field field information
     * @param valuesProducer Numeric values to write.
     * @throws IOException if an I/O error occurred.
     */
    public abstract void addNumericField(FieldInfo field, DocValuesProducer valuesProducer) throws IOException;

    /**
     * Writes binary docvalues for a field.
     * @param field field information
     * @param valuesProducer Binary values to write.
     * @throws IOException if an I/O error occurred.
     */
    public abstract void addBinaryField(FieldInfo field, DocValuesProducer valuesProducer) throws IOException;

    /**
     * Writes pre-sorted binary docvalues for a field.
     * @param field field information
     * @param valuesProducer produces the values and ordinals to write
     * @throws IOException if an I/O error occurred.
     */
    public abstract void addSortedField(FieldInfo field, DocValuesProducer valuesProducer) throws IOException;

    /**
     * Writes pre-sorted numeric docvalues for a field
     * @param field field information
     * @param valuesProducer produces the values to write
     * @throws IOException if an I/O error occurred.
     */
    public abstract void addSortedNumericField(FieldInfo field, DocValuesProducer valuesProducer) throws IOException;

    /**
     * Writes pre-sorted set docvalues for a field
     * @param field field information
     * @param valuesProducer produces the values to write
     * @throws IOException if an I/O error occurred.
     */
    public abstract void addSortedSetField(FieldInfo field, DocValuesProducer valuesProducer) throws IOException;

}
