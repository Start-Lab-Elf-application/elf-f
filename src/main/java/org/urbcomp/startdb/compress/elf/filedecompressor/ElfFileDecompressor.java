package org.urbcomp.startdb.compress.elf.filedecompressor;

import org.urbcomp.startdb.compress.elf.decompressor.ElfDecompressor;
import org.urbcomp.startdb.compress.elf.decompressor.IDecompressor;
import org.urbcomp.startdb.compress.elf.utils.ByteInt;
import org.urbcomp.startdb.compress.elf.utils.FileReader;

import java.io.*;
import java.util.List;

public class ElfFileDecompressor extends AbstractFileDecompressor{

    @Override
    public void decompress() throws IOException {
        //压缩文件是二进制的，这个方法的作用是将其进行拆分，并只保留原始数据压缩后数据的二进制,以及最开始的一个字节是原数据文件的列数
        List<byte[]> data = readBytesFromFile(this.getFilePath());
        System.out.println("read success");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.getOutputFilePath()))) {
            //获取其列数
            int col= ByteInt.bytesToInt(data.get(0));
            System.out.println("原文件列数为"+col);
            //将每个block的数据写入,跳过第一块，从第二块开始
            StringBuilder stringBuilder = new StringBuilder();
            int col_cnt=0;
            for (int i=1;i<data.size();i++) {
                byte[]block=data.get(i);
                //解压压缩后的二进制数据，还原为原始数据
                IDecompressor decompressor = new ElfDecompressor(block);
                List<Double> blockValues = decompressor.decompress();
//                System.out.println("decompress success");
                for (double element : blockValues) {
                    stringBuilder.append(element).append(" ");
                    if(col_cnt==col-1){
                        stringBuilder.append(System.lineSeparator());
                    }
                    col_cnt=(col_cnt+1)%col;
                }
                writer.write(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
    }
}
