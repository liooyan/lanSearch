package cn.lioyan.index;

/**
 * {@link SegmentWriteState}
 * 在写文件时，常用的参数
 * @author com.lioyan
 * @date 2023/6/13  10:54
 */
public class SegmentWriteState {


    public final FieldInfos fieldInfos;

    public final SegmentInfo segmentInfo;

    public SegmentWriteState(FieldInfos fieldInfos, SegmentInfo segmentInfo) {
        this.fieldInfos = fieldInfos;
        this.segmentInfo = segmentInfo;
    }
}
