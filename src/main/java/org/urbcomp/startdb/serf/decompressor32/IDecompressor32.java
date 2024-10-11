package org.urbcomp.startdb.serf.decompressor32;

import java.util.List;

public interface IDecompressor32 {

    List<Float> decompress(byte[] bs);
}
