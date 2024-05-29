package org.urbcomp.startdb.serf.compressor32;

public interface INetCompressor32 {
    byte[] compress(float v);

    default String getKey() {
        return getClass().getSimpleName();
    }
}
