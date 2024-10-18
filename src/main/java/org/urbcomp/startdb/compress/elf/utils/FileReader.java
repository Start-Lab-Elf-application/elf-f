package org.urbcomp.startdb.compress.elf.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class FileReader {
    public static final int DEFAULT_BLOCK_SIZE = 100;
    BufferedReader bufferedReader;
    private final int blockSize;
    //
    private int currentLine=0;
    private int col_num;

    public static int getNumberOfColumns(String filePath) throws FileNotFoundException {
        java.io.FileReader fr = new java.io.FileReader(filePath);
        try (BufferedReader reader = new BufferedReader(fr)) {
            String firstLine = reader.readLine(); // 读取第一行
            if (firstLine != null) {
                return firstLine.split(",\\s*|\\s+").length; // 假设使用逗号作为分隔符
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0; // 如果文件为空或读取出错，返回0
    }

    public FileReader(String filePath, int blockSize) throws FileNotFoundException {
        java.io.FileReader fr = new java.io.FileReader(filePath);
        this.bufferedReader = new BufferedReader(fr);
        this.blockSize = blockSize;
    }
    public FileReader(String filePath) throws FileNotFoundException {
        this(filePath, DEFAULT_BLOCK_SIZE);
    }
    public int getCol_num() {
        return col_num;
    }
    public double[] nextBlock() {
        List<Double> valuesList = new ArrayList<>();
        String line;
        try {
            int counter = 0;
            int col_counter=0;//在不同的nextBlock时，如何衔接之前的读取位置？
            while ((line = bufferedReader.readLine()) != null) {
                currentLine++;
                // 匹配逗号或任意数量的空格作为分隔符
                String[] parts = line.split(",\\s*|\\s+");

                col_counter=parts.length;
                col_num=max(col_num,col_counter);

                for(String part : parts){
                    // 去除字符串两端的空白字符
                    part = part.trim();
                    if(part.isEmpty()){
                        continue;
                    }
                    try {
                        valuesList.add(Double.parseDouble(part));
                        counter++;
                        if (counter == blockSize) {
                            return valuesList.stream().mapToDouble(Double::doubleValue).toArray();
                        }
                    } catch (NumberFormatException e) {
                        System.out.print("在\033[31m第"+currentLine+" 行 \033[0m 行检测到无法被解析为数字的内容：\033[31m"+part+"\033[0m\n");
                        throw new IllegalArgumentException("Invalid number format: " + part+"at line"+currentLine, e);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!valuesList.isEmpty()) {
            return valuesList.stream().mapToDouble(Double::doubleValue).toArray();
        }

        return null;
    }

}
