package cn.lioyan.codecs;

import cn.lioyan.index.CorruptIndexException;
import cn.lioyan.index.IndexFormatTooNewException;
import cn.lioyan.index.IndexFormatTooOldException;
import cn.lioyan.store.DataInput;
import cn.lioyan.store.DataOutput;
import cn.lioyan.store.IndexOutput;
import cn.lioyan.util.BytesRef;

import java.io.IOException;
import java.math.BigInteger;

/**
 * {@link CodecUtil}
 *
 * @author com.lioyan
 * @date 2023/6/5  16:59
 */
public class CodecUtil {

    public final static int CODEC_MAGIC = 0x3fd76c17;

    public final static int FOOTER_MAGIC = ~CODEC_MAGIC;

    public static final int ID_LENGTH = 16;

    public static void writeIndexHeader(DataOutput out, String codec, int version, byte[] id, String suffix) throws IOException {
        if (id.length != ID_LENGTH) {
            throw new IllegalArgumentException("Invalid id: " + idToString(id));
        }
        writeHeader(out, codec, version);
        out.writeBytes(id, 0, id.length);
        BytesRef suffixBytes = new BytesRef(suffix);
        if (suffixBytes.length != suffix.length() || suffixBytes.length >= 256) {
            throw new IllegalArgumentException("suffix must be simple ASCII, less than 256 characters in length [got " + suffix + "]");
        }
        out.writeByte((byte) suffixBytes.length);
        out.writeBytes(suffixBytes.bytes, suffixBytes.offset, suffixBytes.length);
    }


    public static void writeHeader(DataOutput out, String codec, int version) throws IOException {
        BytesRef bytes = new BytesRef(codec);
        if (bytes.length != codec.length() || bytes.length >= 128) {
            throw new IllegalArgumentException("codec must be simple ASCII, less than 128 characters in length [got " + codec + "]");
        }
        out.writeInt(CODEC_MAGIC);
        out.writeString(codec);
        out.writeInt(version);
    }

    public static void writeFooter(IndexOutput out) throws IOException {
        out.writeInt(FOOTER_MAGIC);
        out.writeInt(0);
        writeCRC(out);
    }

    public static int checkHeader(DataInput in, String codec, int minVersion, int maxVersion) throws IOException {
        // Safety to guard against reading a bogus string:
        final int actualHeader = in.readInt();
        if (actualHeader != CODEC_MAGIC) {
            throw new CorruptIndexException("codec header mismatch: actual header=" + actualHeader + " vs expected header=" + CODEC_MAGIC, in);
        }
        return checkHeaderNoMagic(in, codec, minVersion, maxVersion);
    }



    public static int checkHeaderNoMagic(DataInput in, String codec, int minVersion, int maxVersion) throws IOException {
        final String actualCodec = in.readString();
        if (!actualCodec.equals(codec)) {
            throw new CorruptIndexException("codec mismatch: actual codec=" + actualCodec + " vs expected codec=" + codec, in);
        }

        final int actualVersion = in.readInt();
        if (actualVersion < minVersion) {
            throw new IndexFormatTooOldException(in, actualVersion, minVersion, maxVersion);
        }
        if (actualVersion > maxVersion) {
            throw new IndexFormatTooNewException(in, actualVersion, minVersion, maxVersion);
        }

        return actualVersion;
    }

    static void writeCRC(IndexOutput output) throws IOException {
        long value = output.getChecksum();
        if ((value & 0xFFFFFFFF00000000L) != 0) {
            throw new IllegalStateException("Illegal CRC-32 checksum: " + value + " (resource=" + output + ")");
        }
        output.writeLong(value);
    }
    public static String idToString(byte id[]) {
        if (id == null) {
            return "(null)";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(new BigInteger(1, id).toString(Character.MAX_RADIX));
            if (id.length != ID_LENGTH) {
                sb.append(" (INVALID FORMAT)");
            }
            return sb.toString();
        }
    }
}
