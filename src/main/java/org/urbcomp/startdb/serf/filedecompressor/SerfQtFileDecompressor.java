package org.urbcomp.startdb.serf.filedecompressor;

import org.urbcomp.startdb.compress.elf.filedecompressor.AbstractFileDecompressor;
import org.urbcomp.startdb.serf.decompressor.IDecompressor;
import org.urbcomp.startdb.serf.decompressor.SerfQtDecompressor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SerfQtFileDecompressor extends AbstractFileDecompressor {
    @Override
    public void decompress() throws IOException {
        List<byte[]> data = readBytesFromFile(this.getFilePath());
        System.out.println("read success");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.getOutputFilePath()))) {
            StringBuilder stringBuilder = new StringBuilder();

            for (byte[] block : data) {
                IDecompressor decompressor = new SerfQtDecompressor(1.0E-1);
                List<Double> blockValues = decompressor.decompress(block);
//                System.out.println("decompress success");
                for (double element : blockValues) {
                    stringBuilder.append(element).append(System.lineSeparator());
                }

                writer.write(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
    }
}
