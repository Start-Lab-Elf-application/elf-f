package org.urbcomp.startdb.compress.elf;

import org.urbcomp.startdb.compress.elf.filecompressor.*;
import org.urbcomp.startdb.compress.elf.filedecompressor.*;
import org.urbcomp.startdb.compress.elf.utils.ByteToInt;
import org.urbcomp.startdb.serf.filecompressor.SerfQtFileCompressor;
import org.urbcomp.startdb.serf.filedecompressor.SerfQtFileDecompressor;

import java.io.*;
import java.util.Objects;

public class Main {

    public static byte[] result;

    private static final double[] MAX_DIFF = new double[]{1.0E-1, 1.0E-2, 1.0E-3, 1.0E-4, 1.0E-5, 1.0E-6, 1.0E-7, 1.0E-8};

    public static void main(String[] args) throws IOException {
        int flag = Integer.parseInt(args[0]);
        int serf = Integer.parseInt(args[1]);
        String filePath = args[2];
        String outputFilePath = args[3];
        String choice = args[4];
        int precisionFlag = Integer.parseInt(args[5]);   //精度 比如0代表1.0E-1；1代表1.0E-2，以此类推

        if(flag == 0){
            AbstractFileCompressor fileCompressor = null;
            if (serf == 1){
                fileCompressor = new SerfQtFileCompressor(MAX_DIFF[precisionFlag]);
            }
            else{
                if (Objects.equals(choice, "1")){
                    fileCompressor = new ElfFileCompressor();
                }
                else if (Objects.equals(choice, "2")){
                    fileCompressor = new ChimpFileCompressor();
                }
                else if (Objects.equals(choice, "3")){
                    fileCompressor = new ChimpNFileCompressor();
                }
                else if (Objects.equals(choice, "4")){
                    fileCompressor = new ElfOnChimpFileCompressor();
                }
                else if (Objects.equals(choice, "5")){
                    fileCompressor = new ElfOnChimpNFileCompressor();
                }
                else if (Objects.equals(choice, "6")){
                    fileCompressor = new ElfOnGorillaFileCompressorOS();
                }
                else if (Objects.equals(choice, "7")){
                    fileCompressor = new GorillaFileCompressorOS();
                }
                else{
                    System.out.println("没有该选项");
                    return;
                }
            }
            //...继续添加其他算法
            fileCompressor.setFilePath(filePath);
            fileCompressor.setoutputFilePath(outputFilePath);
            fileCompressor.compress();
        }

        else{
// 从给定的文件路径（filePath）中读取前四个字节，将其转换为整数，
            byte[] algorithmByte = new byte[4];
            File file = new File(filePath);
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(algorithmByte);
            }
            int algorithmInt = ByteToInt.byteToInt(algorithmByte);

            AbstractFileDecompressor fileDecompressor = null;
            if (algorithmInt == 1){
                fileDecompressor = new ElfFileDecompressor();
                System.out.println("elf");
            }
            else if (algorithmInt == 2){
                fileDecompressor = new ChimpFileDecompressor();
                System.out.println("chimp");
            }
            else if (algorithmInt == 3){
                fileDecompressor = new ChimpNFileDecompressor();
                System.out.println("chimpN");
            }
            else if (algorithmInt == 4){
                fileDecompressor = new ElfOnChimpFileDecompressor();
                System.out.println("ElfOnChimp");
            }
            else if (algorithmInt == 5){
                fileDecompressor = new ElfOnChimpNFileDecompressor();
                System.out.println("ElfOnChimpN");
            }
            else if (algorithmInt == 6){
                fileDecompressor = new ElfOnGorillaFileDecompressorOS();
                System.out.println("ElfOnGorilla");
            }
            else if (algorithmInt == 7){
                fileDecompressor = new GorillaFileDecompressorOS();
                System.out.println("Gorilla");
            }
            else if (algorithmInt == 8){
                fileDecompressor = new SerfQtFileDecompressor(MAX_DIFF[precisionFlag]);
                System.out.println("SerfQt");
            }
            else{
                System.out.println("没有该选项");
                return;
            }
            fileDecompressor.setFilePath(filePath);
            fileDecompressor.setoutputFilePath(outputFilePath);
            fileDecompressor.decompress();
        }
    }
}
