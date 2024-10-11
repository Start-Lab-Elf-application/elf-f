package org.urbcomp.startdb.serf.compressor;

public interface ICompressor {
    void addValue(double v);

    byte[] getBytes();

    void close();

    long getCompressedSizeInBits();

    default String getKey() {
        return getClass().getSimpleName();
    }
}
