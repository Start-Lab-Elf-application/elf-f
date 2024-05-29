package org.urbcomp.startdb.serf.compressor;

public interface INetCompressor {
    byte[] compress(double v);

    default String getKey() {
        return getClass().getSimpleName();
    }
}
