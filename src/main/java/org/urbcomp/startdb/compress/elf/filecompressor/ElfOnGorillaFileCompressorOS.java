package org.urbcomp.startdb.compress.elf.filecompressor;

import org.urbcomp.startdb.compress.elf.compressor.ElfCompressor;
import org.urbcomp.startdb.compress.elf.compressor.ElfOnChimpCompressor;
import org.urbcomp.startdb.compress.elf.compressor.ElfOnGorillaCompressorOS;
import org.urbcomp.startdb.compress.elf.compressor.ICompressor;
import org.urbcomp.startdb.compress.elf.utils.ByteInt;
import org.urbcomp.startdb.compress.elf.utils.DeleteBytesFromDF;
import org.urbcomp.startdb.compress.elf.utils.FileReader;
import org.urbcomp.startdb.compress.elf.utils.WriteByteToCSV;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.urbcomp.startdb.compress.elf.utils.OperationBetweenIntAndByte.intToTwoBytes;

public class ElfOnGorillaFileCompressorOS extends AbstractFileCompressor{
    @Override
    public void compress() throws IOException {
        FileReader fileReader;
        try(FileOutputStream fos = new FileOutputStream(this.getOutputFilePath())){
            fos.write(ByteInt.intToBytes(6));
            fileReader = new FileReader(this.getFilePath());
            double[] vs;
            while ((vs = fileReader.nextBlock()) != null) {
                ICompressor compressor = new ElfOnGorillaCompressorOS();
                for (double v : vs) {
                    compressor.addValue(v);
                }
                compressor.close();
                byte[] result = compressor.getBytes();
                int sizeofcompressor = compressor.getSize() / 8 + 12;  //为什么这里+12可以通过???
                byte[] sizeofblock = ByteInt.intToBytes(sizeofcompressor);
                fos.write(sizeofblock);
                fos.write(result, 0, sizeofcompressor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
