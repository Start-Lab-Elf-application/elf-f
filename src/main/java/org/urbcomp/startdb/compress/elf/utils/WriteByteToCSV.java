package org.urbcomp.startdb.compress.elf.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WriteByteToCSV {

    public static void writeByteToCSV(String filePath, int number){
        try {
            // 读取原始文件内容
            File file = new File(filePath);
            byte[] fileContent = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileContent);
            }

            // 将数字转换为字节
            byte[] numberBytes = ByteInt.intToBytes(number);

            byte[] newContent = new byte[fileContent.length + numberBytes.length];

            System.arraycopy(numberBytes, 0, newContent, 0, numberBytes.length);
            System.arraycopy(fileContent, 0, newContent, numberBytes.length, fileContent.length);

            // 将新内容写回文件
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(newContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
