package org.urbcomp.startdb.compress.elf.filedecompressor;

import org.urbcomp.startdb.compress.elf.decompressor32.ElfOnChimpDecompressor32;
import org.urbcomp.startdb.compress.elf.decompressor32.ElfOnChimpNDecompressor32;
import org.urbcomp.startdb.compress.elf.decompressor32.IDecompressor32;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ElfOnChimpNFileDecompressor32 extends AbstractFileDecompressor{
    @Override
    public void decompress() throws IOException {

        List<byte[]> data = readBytesFromFile(this.getFilePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.getOutputFilePath(), true))) {
            StringBuilder stringBuilder = new StringBuilder();

            for (byte[] block : data) {
                IDecompressor32 decompressor = new ElfOnChimpNDecompressor32(block, 128);
                List<Float> blockValues = decompressor.decompress();

                for (double element : blockValues) {
                    stringBuilder.append(String.valueOf(element)).append(System.lineSeparator());
                }

                writer.write(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
    }
}
