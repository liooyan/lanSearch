package cn.lioyan.store;

/**
 * {@link IndexFileNames}
 *
 * @author com.lioyan
 * @date 2023/5/26  17:29
 */
public class IndexFileNames {

    public static String segmentFileName(String segmentName, String segmentSuffix, String ext) {
        if (ext.length() > 0 || segmentSuffix.length() > 0) {
            assert !ext.startsWith(".");
            StringBuilder sb = new StringBuilder(segmentName.length() + 2 + segmentSuffix.length() + ext.length());
            sb.append(segmentName);
            if (segmentSuffix.length() > 0) {
                sb.append('_').append(segmentSuffix);
            }
            if (ext.length() > 0) {
                sb.append('.').append(ext);
            }
            return sb.toString();
        } else {
            return segmentName;
        }
    }
}
