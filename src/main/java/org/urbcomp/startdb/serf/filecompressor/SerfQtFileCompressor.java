package org.urbcomp.startdb.serf.filecompressor;

import org.urbcomp.startdb.compress.elf.filecompressor.AbstractFileCompressor;
import org.urbcomp.startdb.compress.elf.utils.FileReader;
import org.urbcomp.startdb.compress.elf.utils.WriteByteToCSV;
import org.urbcomp.startdb.serf.compressor.ICompressor;
import org.urbcomp.startdb.serf.compressor.SerfQtCompressor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.urbcomp.startdb.compress.elf.utils.OperationBetweenIntAndByte.intToTwoBytes;

public class SerfQtFileCompressor extends AbstractFileCompressor {
    public double precison;

    public SerfQtFileCompressor(double precison){
        this.precison = precison;
    }

    @Override
    public void compress() throws IOException {
        FileReader fileReader;
        FileOutputStream fos = new FileOutputStream(this.getOutputFilePath());
        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(this.getOutputFilePath())));

        String filePath = this.getOutputFilePath();

        try {
            fileReader = new FileReader(this.getFilePath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        double[] vs;
        ArrayList<Byte> sizeList = new ArrayList<>();
        sizeList.add((byte) 0x00);
        sizeList.add((byte) 0x00);
        long stime = System.currentTimeMillis();
        while ((vs = fileReader.nextBlock()) != null) {
//            ICompressor compressor = new SerfQtCompressor(1.0E-1);
            ICompressor compressor = new SerfQtCompressor(this.precison);
            for (double v : vs) {
                compressor.addValue(v);
            }
            compressor.close();
            byte[] result = compressor.getBytes();


            int sizeofcompressor = (int) (compressor.getCompressedSizeInBits() / 8); // todo:+12?
//            int sizeofcompressor = 12;
            byte[] sizeOfBlock = intToTwoBytes(sizeofcompressor);
            //CountOfBlock 记录有多少个块
            if (sizeList.get(1) != (byte) 0xff) {
                sizeList.set(1, (byte) (sizeList.get(1) + 1));
            } else {
                sizeList.set(0, (byte) (sizeList.get(0) + 1));
                sizeList.set(1, (byte) 0x00);
            }
            //每两个元素记录一个块所含数据的位数
            sizeList.add(sizeOfBlock[0]);
            sizeList.add(sizeOfBlock[1]);

            // todo
            bos.write(result, 0, sizeofcompressor);
        }//写压缩内容
        bos.flush();
        bos.close();
//        System.out.println("压缩时间："+ (System.nanoTime()-stime));
        byte[] blocks=new byte[sizeList.size()];
        for (int i = 0; i < sizeList.size(); i++) {
            blocks[i] = sizeList.get(i);
        }
        File file = new File(filePath);
        byte[] fileContent = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileContent);
        }
        byte[] newContent = new byte[fileContent.length + blocks.length];
        System.arraycopy(blocks, 0, newContent, 0, blocks.length);//写压缩时，总块数和每块的大小序列//
        System.arraycopy(fileContent, 0, newContent, blocks.length, fileContent.length);
        try (FileOutputStream fos1 = new FileOutputStream(filePath)) {
            fos1.write(newContent);
        }
        System.out.println("压缩时间："+ (System.currentTimeMillis()-stime) + "ms");
//        for (Byte b : sizeList) {
//            //向blocks中添加sizeList里的b
//
//
//            //写压缩时，总块数和每块的大小序列
//        }

        // todo：改名  1? fos写num+content
        WriteByteToCSV.writeByteToCSV(filePath,8);

        fos.close();

        // numberList
//        bos.flush();
//        bos.close();
    }
}
