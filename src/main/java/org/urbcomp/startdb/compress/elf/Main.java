package org.urbcomp.startdb.compress.elf;

import org.urbcomp.startdb.compress.elf.filedecompressor.ElfFileDecompressor;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
        int flag = Integer.parseInt(args[0]);
        String filePath = args[1];
        String outputFilePath = args[2];

        if(flag == 0){
            org.urbcomp.startdb.compress.elf.filecompressor.ElfFileCompressor fileCompressor = new org.urbcomp.startdb.compress.elf.filecompressor.ElfFileCompressor();
            fileCompressor.setFilePath(filePath);
            fileCompressor.setoutputFilePath(outputFilePath);
            fileCompressor.compress();
        }

        else{
            org.urbcomp.startdb.compress.elf.filedecompressor.ElfFileDecompressor fileDecompressor = new org.urbcomp.startdb.compress.elf.filedecompressor.ElfFileDecompressor();
            fileDecompressor.setFilePath(filePath);
            fileDecompressor.setoutputFilePath(outputFilePath);
            fileDecompressor.decompress();
        }
    }
}
