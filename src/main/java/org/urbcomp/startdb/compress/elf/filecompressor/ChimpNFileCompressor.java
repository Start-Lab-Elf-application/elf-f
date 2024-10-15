package org.urbcomp.startdb.compress.elf.filecompressor;

import org.urbcomp.startdb.compress.elf.compressor.ChimpNCompressor;
import org.urbcomp.startdb.compress.elf.compressor.ICompressor;
import org.urbcomp.startdb.compress.elf.utils.ByteInt;
import org.urbcomp.startdb.compress.elf.utils.ExcelReader;
import org.urbcomp.startdb.compress.elf.utils.FileReader;

import java.io.*;
import java.util.Arrays;

public class ChimpNFileCompressor extends AbstractFileCompressor{
    private int col_num;
    @Override
    public void compress() throws IOException {
        String filePath = this.getFilePath();
        String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        double[] vs;
        int debug_count =0;
        try(FileOutputStream fos = new FileOutputStream(this.getOutputFilePath())){
            //向二进制文件中写入int转字节后的共4字节算法信息
            fos.write(ByteInt.intToBytes(3));
            if ("csv".equals(fileExtension) || "txt".equals(fileExtension)){
                FileReader fileReader = new FileReader(filePath);
                //向二进制文件中写入int转字节后的共四字节列数信息
                col_num=FileReader.getNumberOfColumns(filePath);
                System.out.println("列数为"+col_num);
                if(col_num!=0){
                    fos.write(ByteInt.intToBytes(col_num));
                }
                //向二进制文件中写入压缩后数据
                while ((vs = fileReader.nextBlock()) != null) {
                    ICompressor compressor = new ChimpNCompressor(128);
                    for (double v : vs) {
                        compressor.addValue(v);
                    }
                    compressor.close();
                    byte[] result = compressor.getBytes();
                    int sizeofcompressor = compressor.getSize() / 8 + 12;  //为什么这里+12可以通过???
                    byte[] sizeofblock = ByteInt.intToBytes(sizeofcompressor);
                    //每块数据，先写其大小，再写其结果
                    fos.write(sizeofblock);
                    fos.write(result, 0, sizeofcompressor);
                    System.out.println("第"+(++debug_count)+"块写入完成");
                }
            } else if ("xls".equals(fileExtension) || "xlsx".equals(fileExtension)){
                ExcelReader excelFileReader = new ExcelReader(filePath);
                col_num=excelFileReader.getColCount();
                System.out.println("这是EXCEL，列数为"+col_num);
                while ((vs = excelFileReader.nextBlock()) != null) {
                    System.out.println("第"+(++debug_count)+"块读取完成,数据共有"+ Arrays.stream(vs).count()+"个");
                }
            }else {
                throw new IOException("Unsupported file type: " + fileExtension);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        FileReader fileReader;
//        try(FileOutputStream fos = new FileOutputStream(this.getOutputFilePath())){
//            fos.write(ByteInt.intToBytes(3));
//            fileReader = new FileReader(this.getFilePath());
//            double[] vs;
//            while ((vs = fileReader.nextBlock()) != null) {
//                ICompressor compressor = new ChimpNCompressor(128);
//                for (double v : vs) {
//                    compressor.addValue(v);
//                }
//                compressor.close();
//                byte[] result = compressor.getBytes();
//                int sizeofcompressor = compressor.getSize() / 8 + 12;  //为什么这里+12可以通过???
//                byte[] sizeofblock = ByteInt.intToBytes(sizeofcompressor);
//                fos.write(sizeofblock);
//                fos.write(result, 0, sizeofcompressor);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}

