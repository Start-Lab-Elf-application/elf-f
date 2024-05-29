package org.urbcomp.startdb.serf.compressor32;

public interface ICompressor32 {
    void addValue(float v);

    byte[] getBytes();

    void close();

    long getCompressedSizeInBits();

    default String getKey() {
        return getClass().getSimpleName();
    }
}
