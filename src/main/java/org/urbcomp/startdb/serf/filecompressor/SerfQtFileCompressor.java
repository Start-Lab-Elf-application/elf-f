package org.urbcomp.startdb.serf.filecompressor;


import org.urbcomp.startdb.compress.elf.filecompressor.AbstractFileCompressor;
import org.urbcomp.startdb.compress.elf.utils.ByteInt;
import org.urbcomp.startdb.compress.elf.utils.ExcelReader;
import org.urbcomp.startdb.compress.elf.utils.FileReader;
import org.urbcomp.startdb.serf.compressor.ICompressor;
import org.urbcomp.startdb.serf.compressor.SerfQtCompressor;


import java.io.*;
import java.util.Arrays;

public class SerfQtFileCompressor extends AbstractFileCompressor {
    public double precison;

    public SerfQtFileCompressor(double precison){
        this.precison = precison;
    }

    private int col_num;
    @Override
    public void compress() throws IOException {
        String filePath = this.getFilePath();
        String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        double[] vs;
        int debug_count =0;
        try(FileOutputStream fos = new FileOutputStream(this.getOutputFilePath())){
            //向二进制文件中写入int转字节后的共4字节算法信息
            fos.write(ByteInt.intToBytes(8));
            if ("csv".equals(fileExtension) || "txt".equals(fileExtension)){
                org.urbcomp.startdb.compress.elf.utils.FileReader fileReader = new org.urbcomp.startdb.compress.elf.utils.FileReader(filePath);
                //向二进制文件中写入int转字节后的共四字节列数信息
                col_num= FileReader.getNumberOfColumns(filePath);
                System.out.println("列数为"+col_num);
                if(col_num!=0){
                    fos.write(ByteInt.intToBytes(col_num));
                }
                //向二进制文件中写入压缩后数据
                while ((vs = fileReader.nextBlock()) != null) {
                    ICompressor compressor = new SerfQtCompressor(this.precison);
                    for (double v : vs) {
                        compressor.addValue(v);
                    }
                    compressor.close();
                    byte[] result = compressor.getBytes();
                    int sizeofcompressor = (int) (compressor.getCompressedSizeInBits() / 8);
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
//            fos.write(ByteInt.intToBytes(8));
//            fileReader = new FileReader(this.getFilePath());
//            double[] vs;
//            while ((vs = fileReader.nextBlock()) != null) {
//                ICompressor compressor = new SerfQtCompressor(this.precison);
//                for (double v : vs) {
//                    compressor.addValue(v);
//                }
//                compressor.close();
//                byte[] result = compressor.getBytes();
//                int sizeofcompressor = (int) (compressor.getCompressedSizeInBits() / 8);
//                byte[] sizeofblock = ByteInt.intToBytes(sizeofcompressor);
//                fos.write(sizeofblock);
//                fos.write(result, 0, sizeofcompressor);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}