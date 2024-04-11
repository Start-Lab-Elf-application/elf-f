package org.urbcomp.startdb.compress.elf.filedecompressor;

import org.urbcomp.startdb.compress.elf.filecompressor.IFileCompressor;

import java.io.*;
import java.util.ArrayList;
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
        FileInputStream inStream = new FileInputStream(file);

        byte[] length = new byte[2];
        inStream.read(length);

        int intlength = twoBytesToInt(length);
        byte[] sizeOfBlock = new byte[intlength * 2];
        inStream.read(sizeOfBlock);

        List<Integer> sizeOfBlockToInt = new ArrayList<>();

        for (int i = 0; i < intlength; i++) {
            byte[] tempArr = new byte[2];
            tempArr[0] = sizeOfBlock[2 * i];
            tempArr[1] = sizeOfBlock[2 * i + 1];
            sizeOfBlockToInt.add(twoBytesToInt(tempArr));
        }

        List<byte[]> byteTodec = new ArrayList<>();
        for (int i = 0; i < intlength; i++) {
            byte[] byteOfBlock = new byte[sizeOfBlockToInt.get(i)];
            inStream.read(byteOfBlock);
            byteTodec.add(byteOfBlock);
        }
        inStream.close();

        return byteTodec;
    }
}
