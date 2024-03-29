package org.urbcomp.startdb.compress.elf;

import org.urbcomp.startdb.compress.elf.filedecompressor.*;

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
            AbstractFileDecompressor fileDecompressor = null;
            if (filePath.endsWith(".elf")){
                fileDecompressor = new ElfFileDecompressor();
                System.out.println("elf");
            }
            else if (filePath.endsWith(".chimp")){
                fileDecompressor = new ChimpFileDecompressor();
                System.out.println("chimp");
            }
            else if (filePath.endsWith(".chimpN")){
                fileDecompressor = new ChimpNFileDecompressor();
            }
            else if (filePath.endsWith(".elfOnChimp")){
                fileDecompressor = new ElfOnChimpFileDecompressor();
            }
            else if (filePath.endsWith(".elfOnChimpN")){
                fileDecompressor = new ElfOnChimpNFileDecompressor();
            }
            else if (filePath.endsWith(".elfOnGorilla")){
                fileDecompressor = new ElfOnGorillaFileDecompressorOS();
            }
            else if (filePath.endsWith(".gorilla")){
                fileDecompressor = new GorillaFileDecompressorOS();
            }
            else if (filePath.endsWith(".elf32")){
                fileDecompressor = new ElfFileDecompressor32();
            }
            else if (filePath.endsWith(".chimp32")){
                fileDecompressor = new ChimpFileDecompressor32();
            }
            else if (filePath.endsWith(".chimpN32")){
                fileDecompressor = new ChimpNFileDecompressor32();
            }
            else if (filePath.endsWith(".elfOnChimp32")){
                fileDecompressor = new ElfOnChimpNFileDecompressor();
            }
            else if (filePath.endsWith(".elfOnChimpN32")){
                fileDecompressor = new ElfOnChimpNFileDecompressor();
            }
            else if (filePath.endsWith(".elfOnGorilla32")){
                fileDecompressor = new ElfOnGorillaFileDecompressor32OS();
            }
            else if (filePath.endsWith(".gorilla32")){
                fileDecompressor = new GorillaFileDecompressorOS();
            }
            assert fileDecompressor != null;
            fileDecompressor.setFilePath(filePath);
            fileDecompressor.setoutputFilePath(outputFilePath);
            fileDecompressor.decompress();
        }
    }
}
