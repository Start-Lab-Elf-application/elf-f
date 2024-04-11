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
            byte[] numberBytes = intToBytes(number);

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
//    private static byte[] intToBytes(int value) {
//        return Integer.toString(value).getBytes(StandardCharsets.UTF_8);
//    }

    public static byte[] intToBytes(int a){
        byte[] ans=new byte[4];
        for(int i=0;i<4;i++)
            ans[i]=(byte)(a>>(i*8));//截断 int 的低 8 位为一个字节 byte，并存储起来
        return ans;
    }
}
