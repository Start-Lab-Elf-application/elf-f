package org.urbcomp.startdb.compress.elf.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class DeleteBytesFromDF {
    private static final int head_length = 4;
    public static void writeByteToDF(String filePath){
        try {
            // 读取原始压缩文件内容
            File file = new File(filePath);
            byte[] fileContent = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileContent);
            }
            //Head   压缩文件内容（每块长度2个字节   块内容..）
            // 删除头部4个字节
            byte[] newContent = new byte[fileContent.length - head_length];
            System.arraycopy(fileContent, head_length, newContent, 0, fileContent.length - head_length);
            // 将新内容写回压缩文件
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(newContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
