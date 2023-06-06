package cn.lioyan.index;

/**
 * {@link FieldInfo}
 *
 * @author com.lioyan
 * @date 2023/6/6  9:39
 */
public final class FieldInfo {


    /**
     * 字段名称
     */
    public final String name;
    /** 字段编号 */
    public final int number;

    public FieldInfo(String name, int number) {
        this.name = name;
        this.number = number;
    }
}
