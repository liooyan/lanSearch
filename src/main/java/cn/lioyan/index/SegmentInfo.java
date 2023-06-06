package cn.lioyan.index;

/**
 * {@link SegmentInfo}
 *  索引分段信息
 * @author com.lioyan
 * @date 2023/6/5  16:46
 */
public class SegmentInfo {

    /**
     * 文档名称，也就是 _1  _2  等
     */
    public final  String name;

    /**
     *
     * 16位的唯一id
     */
    private final byte[] id;

    public SegmentInfo(String name, byte[] id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public byte[] getId() {
        return id;
    }
}
