package org.urbcomp.startdb.serf.filecompressor;


import org.urbcomp.startdb.compress.elf.filecompressor.AbstractFileCompressor;
import org.urbcomp.startdb.compress.elf.utils.ByteInt;
import org.urbcomp.startdb.compress.elf.utils.FileReader;
import org.urbcomp.startdb.serf.compressor.ICompressor;
import org.urbcomp.startdb.serf.compressor.SerfQtCompressor;

import java.io.*;

public class SerfQtFileCompressor extends AbstractFileCompressor {
    public double precison;

    public SerfQtFileCompressor(double precison){
        this.precison = precison;
    }

    @Override
    public void compress() throws IOException {
        FileReader fileReader;
        try(FileOutputStream fos = new FileOutputStream(this.getOutputFilePath())){
            fos.write(ByteInt.intToBytes(8));
            fileReader = new FileReader(this.getFilePath());
            double[] vs;
            while ((vs = fileReader.nextBlock()) != null) {
                ICompressor compressor = new SerfQtCompressor(this.precison);
                for (double v : vs) {
                    compressor.addValue(v);
                }
                compressor.close();
                byte[] result = compressor.getBytes();
                int sizeofcompressor = (int) (compressor.getCompressedSizeInBits() / 8);
                byte[] sizeofblock = ByteInt.intToBytes(sizeofcompressor);
                fos.write(sizeofblock);
                fos.write(result, 0, sizeofcompressor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
