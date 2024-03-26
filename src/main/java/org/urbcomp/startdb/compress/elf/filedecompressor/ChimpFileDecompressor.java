package org.urbcomp.startdb.compress.elf.filedecompressor;

import org.urbcomp.startdb.compress.elf.decompressor.ChimpDecompressor;
import org.urbcomp.startdb.compress.elf.decompressor.ElfDecompressor;
import org.urbcomp.startdb.compress.elf.decompressor.IDecompressor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ChimpFileDecompressor extends AbstractFileDecompressor{
    @Override
    public void decompress() throws IOException {

        List<byte[]> data = readBytesFromFile(this.getFilePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.getOutputFilePath(), true))) {
            StringBuilder stringBuilder = new StringBuilder();

            for (byte[] block : data) {
                IDecompressor decompressor = new ChimpDecompressor(block);
                List<Double> blockValues = decompressor.decompress();

                for (double element : blockValues) {
                    stringBuilder.append(String.valueOf(element)).append(System.lineSeparator());
                }

                writer.write(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
    }
}
