package org.urbcomp.startdb.compress.elf.filedecompressor;

import org.urbcomp.startdb.compress.elf.filecompressor.IFileCompressor;
import org.urbcomp.startdb.compress.elf.utils.ByteToInt;

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
//        File file = new File(path);
//        FileInputStream inStream = new FileInputStream(file);
//
//        // 这个数组没用，这里是为了将inStream的指针指到第五个字节
//        byte[] algorithm = new byte[4];
//        inStream.read(algorithm);
//
//        byte[] length = new byte[2];
//        inStream.read(length);
//        int intlength = twoBytesToInt(length);
//        byte[] sizeOfBlock = new byte[intlength * 2];
//        inStream.read(sizeOfBlock);
//
//        List<Integer> sizeOfBlockToInt = new ArrayList<>();
//
//        for (int i = 0; i < intlength; i++) {
//            byte[] tempArr = new byte[2];
//            tempArr[0] = sizeOfBlock[2 * i];
//            tempArr[1] = sizeOfBlock[2 * i + 1];
//            sizeOfBlockToInt.add(twoBytesToInt(tempArr));
//        }
//
//        List<byte[]> byteTodec = new ArrayList<>();
//        for (int i = 0; i < intlength; i++) {
//            byte[] byteOfBlock = new byte[sizeOfBlockToInt.get(i)];
//            inStream.read(byteOfBlock);
//            byteTodec.add(byteOfBlock);
//        }
//        inStream.close();
//        return byteTodec;
        File file = new File(path);
        FileInputStream inStream = new FileInputStream(file);

        // 这个数组没用，这里是为了将inStream的指针指到第五个字节
        byte[] algorithm = new byte[4];
        inStream.read(algorithm);

        byte[] length = new byte[4];
        inStream.read(length);
        int intlength = ByteToInt.byteToInt2(length);
        System.out.println(intlength);
        byte[] sizeOfBlock = new byte[intlength * 4];
        inStream.read(sizeOfBlock);

        List<Integer> sizeOfBlockToInt = new ArrayList<>();

        for (int i = 0; i < intlength; i++) {
            byte[] tempArr = new byte[4];
//            tempArr[0] = sizeOfBlock[2 * i];
//            tempArr[1] = sizeOfBlock[2 * i + 1];
            tempArr[0] = sizeOfBlock[4 * i];
            tempArr[1] = sizeOfBlock[4 * i + 1];
            tempArr[2] = sizeOfBlock[4 * i + 2];
            tempArr[3] = sizeOfBlock[4 * i + 3];
//            System.out.println("当前i:  " + i);
//            System.out.println(ByteToInt.byteToInt2(tempArr));
            if (ByteToInt.byteToInt2(tempArr) < 0){
                intlength = i;
                break;
            }
            sizeOfBlockToInt.add(ByteToInt.byteToInt2(tempArr));
//            sizeOfBlockToInt.add(twoBytesToInt(tempArr));
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
