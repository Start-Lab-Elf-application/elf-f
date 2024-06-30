package org.urbcomp.startdb.compress.elf.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileReader {
    public static final int DEFAULT_BLOCK_SIZE = 100;
    BufferedReader bufferedReader;
    private final int blockSize;

    public FileReader(String filePath, int blockSize) throws FileNotFoundException {
        java.io.FileReader fr = new java.io.FileReader(filePath);
        this.bufferedReader = new BufferedReader(fr);
        this.blockSize = blockSize;
    }

    public FileReader(String filePath) throws FileNotFoundException {
        this(filePath, DEFAULT_BLOCK_SIZE);
    }

    public double[] nextBlock() {
        List<Double> valuesList = new ArrayList<>();
        String line;
        try {
            int counter = 0;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    valuesList.add(Double.parseDouble(line));
                    counter++;
                    if (counter == blockSize) {
                        return valuesList.stream().mapToDouble(Double::doubleValue).toArray();
                    }
                } catch (NumberFormatException ignored) {
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
