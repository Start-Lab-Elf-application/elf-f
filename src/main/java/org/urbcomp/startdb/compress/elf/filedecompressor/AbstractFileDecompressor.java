package org.urbcomp.startdb.compress.elf.filedecompressor;

//import com.sun.tools.internal.xjc.reader.xmlschema.BindYellow;
import org.urbcomp.startdb.compress.elf.utils.ByteInt;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFileDecompressor implements IFileDecompressor {
    private String filePath = "";
    private String outputFilePath = "";

    @Override
    public void setFilePath(String path) {
        this.filePath = path;
    }

    @Override
    public void setoutputFilePath(String path) {
        this.outputFilePath = path;
    }

    @Override
    public String getFilePath(){
        return this.filePath;
    }

    @Override
    public String getOutputFilePath(){
        return this.outputFilePath;
    }

    public abstract void decompress() throws IOException;

    public List<byte[]> readBytesFromFile(String path) throws IOException {
        //压缩文件是二进制的，这个方法的作用是将其进行拆分，并只保留原始数据的二进制,以及最开始的一个字节是原数据文件的列数
        File file = new File(path);
        try(FileInputStream inStream = new FileInputStream(file)){
            // 读取4字节的算法标识，再读取4字节的列数，使指针指向每块数据的起始位置
            byte[] otherAndCol = new byte[4];
            inStream.read(otherAndCol);
            inStream.read(otherAndCol);
            List<byte[]> byteTodec = new ArrayList<>();
            byteTodec.add(otherAndCol);
            while (true){
                byte[] length = new byte[4];
                int lengths = inStream.read(length);
                if (lengths == -1){
                    break;
                }
                int blockSize = ByteInt.bytesToInt(length);
                byte[] data = new byte[blockSize];
                inStream.read(data);
                byteTodec.add(data);
            }
            return byteTodec;
        } catch (IOException e){
            return null;
        }
    }
}
