package org.urbcomp.startdb.serf.decompressor;

import java.util.List;

public interface IDecompressor {

    List<Double> decompress(byte[] bs);
}
