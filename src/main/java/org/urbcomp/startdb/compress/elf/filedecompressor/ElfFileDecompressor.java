package org.urbcomp.startdb.compress.elf.filedecompressor;

import org.urbcomp.startdb.compress.elf.decompressor.ElfDecompressor;
import org.urbcomp.startdb.compress.elf.decompressor.IDecompressor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.urbcomp.startdb.compress.elf.utils.OperationBetweenIntAndByte.twoBytesToInt;

public class ElfFileDecompressor extends AbstractFileDecompressor{

    @Override
    public void decompress() throws IOException {

        List<byte[]> data = readBytesFromFile(this.getFilePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.getOutputFilePath(), true))) {
            StringBuilder stringBuilder = new StringBuilder();

            for (byte[] block : data) {
                IDecompressor decompressor = new ElfDecompressor(block);
                List<Double> blockValues = decompressor.decompress();

                for (double element : blockValues) {
                    stringBuilder.append(String.valueOf(element)).append(System.lineSeparator());
                }

                writer.write(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
    }

    private List<byte[]> readBytesFromFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream inStream = new FileInputStream(file);

        byte[] length = new byte[2];
        inStream.read(length);

        int intlength = twoBytesToInt(length);
        byte[] sizeOfBlock = new byte[intlength * 2];
        inStream.read(sizeOfBlock);

        List<Integer> sizeOfBlockToInt = new ArrayList<>();

        for (int i = 0; i < intlength; i++) {
            byte[] tempArr = new byte[2];
            tempArr[0] = sizeOfBlock[2 * i];
            tempArr[1] = sizeOfBlock[2 * i + 1];
            sizeOfBlockToInt.add(twoBytesToInt(tempArr));
        }

        List<byte[]> byteTodec = new ArrayList<>();
        for (int i = 0; i < intlength; i++) {
            byte[] byteOfBlock = new byte[sizeOfBlockToInt.get(i)];
            inStream.read(byteOfBlock);
            byteTodec.add(byteOfBlock);
        }
        inStream.close();

        return byteTodec;
    }
}
