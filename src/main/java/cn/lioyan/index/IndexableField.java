package cn.lioyan.index;

import cn.lioyan.util.BytesRef;

/**
 * {@link IndexableField}
 *
 * @author com.lioyan
 * @date 2023/6/6  9:39
 */
public interface  IndexableField {


    /** 字段名称
     *
     * @return
     */
    public String name();

    public String stringValue();

    public BytesRef binaryValue();

    /**
     * 返回 Number 的值
     * @return
     */
    public Number numericValue();
}
