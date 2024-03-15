package org.urbcomp.startdb.compress.elf.filecompressor;

import org.urbcomp.startdb.compress.elf.compressor.ElfCompressor;
import org.urbcomp.startdb.compress.elf.compressor.ICompressor;
import org.urbcomp.startdb.compress.elf.utils.FileReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.urbcomp.startdb.compress.elf.utils.OperationBetweenIntAndByte.intToTwoBytes;

public class ElfFileCompressor extends AbstractFileCompressor{
    @Override
    public void compress() throws IOException {
        org.urbcomp.startdb.compress.elf.utils.FileReader fileReader;
        FileOutputStream fos = new FileOutputStream(this.getOutputFilePath());
        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(this.getOutputFilePath())));

        try {
            fileReader = new FileReader(this.getFilePath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        double[] vs;
        ArrayList<Byte> sizeList = new ArrayList<>();
        sizeList.add((byte) 0x00);
        sizeList.add((byte) 0x00);

        while ((vs = fileReader.nextBlock()) != null) {

            ICompressor compressor = new ElfCompressor();

            for (double v : vs) {
                compressor.addValue(v);
            }
            compressor.close();

            int sizeofcompressor = compressor.getSize() / 8 + 12;

            byte[] result = compressor.getBytes();
            byte[] sizeOfBlock = intToTwoBytes(sizeofcompressor);

            if (sizeList.get(1) != (byte) 0xff) {
                sizeList.set(1, (byte) (sizeList.get(1) + 1));
            } else {
                sizeList.set(0, (byte) (sizeList.get(0) + 1));
                sizeList.set(1, (byte) 0x00);
            }
            sizeList.add(sizeOfBlock[0]);
            sizeList.add(sizeOfBlock[1]);

            bos.write(result, 0, sizeofcompressor);
        }

        for (Byte b : sizeList) {
            fos.write(b);
        }
        fos.close();

        bos.flush();
        bos.close();
    }
}
