package org.urbcomp.startdb.compress.elf.filedecompressor;

import com.sun.tools.internal.xjc.reader.xmlschema.BindYellow;
import org.urbcomp.startdb.compress.elf.utils.ByteInt;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.urbcomp.startdb.compress.elf.utils.OperationBetweenIntAndByte.twoBytesToInt;

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
        File file = new File(path);
        try(FileInputStream inStream = new FileInputStream(file)){
            // 这个数组没用，这里是为了将inStream的指针指到第五个字节
            byte[] algorithm = new byte[4];
            inStream.read(algorithm);
            List<byte[]> byteTodec = new ArrayList<>();
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
