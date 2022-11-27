package org.urbcomp.startdb.compress.elf.compressor32;

import gr.aueb.delorean.chimp.ChimpN;
import gr.aueb.delorean.chimp.ChimpN32;
import org.urbcomp.startdb.compress.elf.compressor.ICompressor;

public class ChimpNCompressor32 implements ICompressor32 {

    private final ChimpN32 chimpN32;
    private final int previousValues;

    public ChimpNCompressor32(int previousValues) {
        chimpN32 = new ChimpN32(previousValues);
        this.previousValues = previousValues;
    }

    @Override
    public void addValue(float v) {
        chimpN32.addValue(v);
    }

    @Override
    public int getSize() {
        return chimpN32.getSize();
    }

    @Override
    public byte[] getBytes() {
        return chimpN32.getOut();
    }

    @Override
    public void close() {
        chimpN32.close();
    }
}
