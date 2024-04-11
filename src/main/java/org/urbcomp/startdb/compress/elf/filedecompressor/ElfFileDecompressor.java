package org.urbcomp.startdb.compress.elf.filedecompressor;

import org.urbcomp.startdb.compress.elf.decompressor.ElfDecompressor;
import org.urbcomp.startdb.compress.elf.decompressor.IDecompressor;

import java.io.*;
import java.util.List;


public class ElfFileDecompressor extends AbstractFileDecompressor{

    @Override
    public void decompress() throws IOException {

        List<byte[]> data = readBytesFromFile(this.getFilePath());
        System.out.println("read success");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.getOutputFilePath(), true))) {
            StringBuilder stringBuilder = new StringBuilder();

            for (byte[] block : data) {
                IDecompressor decompressor = new ElfDecompressor(block);
                List<Double> blockValues = decompressor.decompress();
                System.out.println("decompress success");
                for (double element : blockValues) {
                    stringBuilder.append(String.valueOf(element)).append(System.lineSeparator());
                }

                writer.write(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
    }
}
