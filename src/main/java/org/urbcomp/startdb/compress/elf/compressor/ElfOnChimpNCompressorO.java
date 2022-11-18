package org.urbcomp.startdb.compress.elf.compressor;

import gr.aueb.delorean.chimp.ChimpNO;
import gr.aueb.delorean.chimp.OutputBitStream;

public class ElfOnChimpNCompressorO extends AbstractElfCompressor {
    private final ChimpNO chimpN;
    public ElfOnChimpNCompressorO(int previousValues) {
        chimpN = new ChimpNO(previousValues);
    }
    @Override protected int writeInt(int n, int len) {
        OutputBitStream os = chimpN.getOutputStream();
        os.writeInt(n, len);
        return len;
    }

    @Override protected int writeBit(boolean bit) {
        OutputBitStream os = chimpN.getOutputStream();
        os.writeBit(bit);
        return 1;
    }

    @Override protected int xorCompress(long vPrimeLong) {
        return chimpN.addValue(vPrimeLong);
    }

    @Override public byte[] getBytes() {
        return chimpN.getOut();
    }

    @Override public void close() {
        // we write one more bit here, for marking an end of the stream.
        writeBit(false);
        chimpN.close();
    }
}