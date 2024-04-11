package org.urbcomp.startdb.compress.elf.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DeleteBytesFromCSV {
    public static void writeByteToCSV(String filePath){
        try {
            // 读取原始CSV文件内容
            File file = new File(filePath);
            byte[] fileContent = new byte[(int) file.length()];
            byte[] algorithm = new byte[1];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileContent);
                fis.read(algorithm);
            }


            byte[] newContent = new byte[fileContent.length - algorithm.length];

            System.arraycopy(fileContent, algorithm.length, newContent, 0, fileContent.length - algorithm.length);

            // 将新内容写回CSV文件
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(newContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
