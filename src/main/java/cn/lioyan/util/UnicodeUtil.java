package cn.lioyan.util;

/**
 * 编码相关，直接复制
 */
public class UnicodeUtil {
    private static final long UNI_MAX_BMP = 0x0000FFFF;
    private static final long HALF_MASK = 0x3FFL;

    public static int UTF8toUTF16(byte[] utf8, int offset, int length, char[] out) {
        int out_offset = 0;
        final int limit = offset + length;
        while (offset < limit) {
            int b = utf8[offset++]&0xff;
            if (b < 0xc0) {
                assert b < 0x80;
                out[out_offset++] = (char)b;
            } else if (b < 0xe0) {
                out[out_offset++] = (char)(((b&0x1f)<<6) + (utf8[offset++]&0x3f));
            } else if (b < 0xf0) {
                out[out_offset++] = (char)(((b&0xf)<<12) + ((utf8[offset]&0x3f)<<6) + (utf8[offset+1]&0x3f));
                offset += 2;
            } else {
                assert b < 0xf8: "b = 0x" + Integer.toHexString(b);
                int ch = ((b&0x7)<<18) + ((utf8[offset]&0x3f)<<12) + ((utf8[offset+1]&0x3f)<<6) + (utf8[offset+2]&0x3f);
                offset += 3;
                if (ch < UNI_MAX_BMP) {
                    out[out_offset++] = (char)ch;
                } else {
                    int chHalf = ch - 0x0010000;
                    out[out_offset++] = (char) ((chHalf >> 10) + 0xD800);
                    out[out_offset++] = (char) ((chHalf & HALF_MASK) + 0xDC00);
                }
            }
        }
        return out_offset;
    }

}
