package cn.lioyan.codecs.compressing;

import cn.lioyan.codecs.CodecUtil;
import cn.lioyan.index.CorruptIndexException;
import cn.lioyan.index.IndexFileNames;
import cn.lioyan.store.ChecksumIndexInput;
import cn.lioyan.store.Directory;
import cn.lioyan.store.IOContext;
import cn.lioyan.store.IndexOutput;
import cn.lioyan.util.IOUtils;
import cn.lioyan.util.packed.DirectMonotonicWriter;

import java.io.Closeable;
import java.io.IOException;

/**
 * {@link FieldsIndexWriter}
 *
 * @author com.lioyan
 * @date 2023/5/30  16:38
 */
public class FieldsIndexWriter implements Closeable {


    static final int VERSION_CURRENT = 0;
    private final Directory dir;
    private final String name;
    private final String suffix;
    private final String extension;
    private final String codecName;
    private final byte[] id;
    private final int blockShift;
    private final IOContext ioContext;
    private IndexOutput docsOut;
    private IndexOutput filePointersOut;

    private long previousFP;

    private int totalChunks;

    private int totalDocs;

    /**
     * @param dir
     * @param name       当前段的编号，如 _8
     * @param suffix     前缀
     * @param extension  文件结尾 fdx
     * @param codecName  Lucene85FieldsIndex
     * @param id         文件的id
     * @param blockShift 一块数据大小的基数， 2^blockShift
     * @param ioContext
     * @throws IOException
     */
    public FieldsIndexWriter(Directory dir, String name, String suffix, String extension,
                             String codecName, byte[] id, int blockShift, IOContext ioContext) throws IOException {

        this.dir = dir;
        this.name = name;
        this.suffix = suffix;
        this.extension = extension;
        this.codecName = codecName;
        this.id = id;
        this.blockShift = blockShift;
        this.ioContext = ioContext;

        boolean success = false;
        this.docsOut = dir.createTempOutput(name, codecName + "-doc_ids", ioContext);

        CodecUtil.writeHeader(docsOut, codecName + "Docs", VERSION_CURRENT);

        filePointersOut = dir.createTempOutput(name, codecName + "file_pointers", ioContext);


        CodecUtil.writeHeader(filePointersOut, codecName + "FilePointers", VERSION_CURRENT);

        success = true;
    }

    /**
     * @param numDocs      当前块的文档数量
     * @param startPointer 当前文档块位置
     * @throws IOException
     */
    public void writeIndex(int numDocs, long startPointer) throws IOException {
        assert startPointer >= previousFP;
        docsOut.writeVInt(numDocs); //用于保存的值，该值为与前一个的差值
        filePointersOut.writeVLong(startPointer - previousFP);//当前值在原文档中的位置
        previousFP = startPointer;
        totalDocs += numDocs;  //记录当前总文档数量
        totalChunks++; //当前块数量
    }


    /**
     * @param numDocs    保存的文件数量
     * @param maxPointer 最大偏移量
     * @param metaOut    输出的meta 值
     * @throws IOException
     */
    void finish(int numDocs, long maxPointer, IndexOutput metaOut) throws IOException {
        if (numDocs != totalDocs) {
            throw new IllegalStateException("Expected " + numDocs + " docs, but got " + totalDocs);
        }

        CodecUtil.writeFooter(docsOut);
        CodecUtil.writeFooter(filePointersOut);
        IOUtils.close(docsOut, filePointersOut);
        try (IndexOutput dataOut = dir.createOutput(IndexFileNames.segmentFileName(name, suffix, extension), ioContext)) {
            CodecUtil.writeIndexHeader(dataOut, codecName + "Idx", VERSION_CURRENT, id, suffix);
            metaOut.writeInt(numDocs);
            metaOut.writeInt(blockShift);
            metaOut.writeInt(totalChunks + 1);
            metaOut.writeLong(dataOut.getFilePointer());
            Throwable priorE = null;
            try (ChecksumIndexInput docsIn = dir.openChecksumInput(docsOut.getName(), IOContext.READONCE)) {
                CodecUtil.checkHeader(docsIn, codecName + "Docs", VERSION_CURRENT, VERSION_CURRENT);
                final DirectMonotonicWriter docs = DirectMonotonicWriter.getInstance(metaOut, dataOut, totalChunks + 1, blockShift);
                long doc = 0;
                docs.add(doc);
                for (int i = 0; i < totalChunks; ++i) {
                    doc += docsIn.readVInt();
                    docs.add(doc);
                }
                docs.finish();
                if (doc != totalDocs) {
                    throw new CorruptIndexException("Docs don't add up", docsIn);
                }


            } catch (Throwable e) {
                priorE = e;
            } finally {
              //  CodecUtil.checkFooter(docsIn, priorE);
            }



            dir.deleteFile(docsOut.getName());
            docsOut = null;


            metaOut.writeLong(dataOut.getFilePointer());
            try (ChecksumIndexInput filePointersIn = dir.openChecksumInput(filePointersOut.getName(), IOContext.READONCE)) {
                CodecUtil.checkHeader(filePointersIn, codecName + "FilePointers", VERSION_CURRENT, VERSION_CURRENT);
                try {
                    final DirectMonotonicWriter filePointers = DirectMonotonicWriter.getInstance(metaOut, dataOut, totalChunks + 1, blockShift);
                    long fp = 0;
                    for (int i = 0; i < totalChunks; ++i) {
                        fp += filePointersIn.readVLong();
                        filePointers.add(fp);
                    }
                    if (maxPointer < fp) {
                        throw new CorruptIndexException("File pointers don't add up", filePointersIn);
                    }
                    filePointers.add(maxPointer);
                    filePointers.finish();
                } catch (Throwable e) {
                    priorE = e;
                } finally {
                    // CodecUtil.checkFooter(filePointersIn, priorE);
                }
            }
            dir.deleteFile(filePointersOut.getName());
            filePointersOut = null;

            metaOut.writeLong(dataOut.getFilePointer());
            metaOut.writeLong(maxPointer);

            CodecUtil.writeFooter(dataOut);



        }


    }


    @Override
    public void close() throws IOException {
        docsOut.close();
        filePointersOut.close();
    }
}
