package org.urbcomp.startdb.serf.compressor32;

import org.urbcomp.startdb.serf.utils.EliasDeltaCodec;
import org.urbcomp.startdb.serf.utils.OutputBitStream;
import org.urbcomp.startdb.serf.utils.ZigZagCodec;

import java.util.Arrays;

/*
 *                                  +---------+---------------------+
 *                   no exception   |SignalBit|EliasDeltaEncodedBits|
 *                  +-------------->+---------+---------------------+
 *                  |               |    0    |        1bits+       |
 *                  |               +---------+---------------------+
 * +------------+   |
 * |Float Value +---+
 * +------------+   |
 *                  |               +---------+----------------+-------------------+
 *                  |               |SignalBit|LeadingZeroCount|      LeftBits     |
 *                  +-------------->+---------+----------------+-------------------+
 *                   exception      |    1    |      4bits     |32-leadingZeroCount|
 *                                  +---------+----------------+-------------------+
 */

public class SerfQtCompressor32 implements ICompressor32 {
    private static final int BLOCK_SIZE = 1000;
    private final float maxDiff;
    private final OutputBitStream out = new OutputBitStream(new byte[2 * BLOCK_SIZE * Float.BYTES]);
    private float preValue = 2;
    private long compressedBits = 0;
    private long storedCompressedBits;

    public SerfQtCompressor32(float maxDiff) {
        this.maxDiff = maxDiff;
    }

    @Override
    public void addValue(float v) {
        long q = Math.round((v - preValue) / (2 * maxDiff));
        float recoverValue = preValue + 2 * maxDiff * q;
        if (!(Math.abs(recoverValue - v) <= maxDiff)) {
            // small cases
            compressedBits += out.writeBit(true);
            int xorResult = Float.floatToIntBits(v) ^ Float.floatToIntBits(preValue);
            int leadingZeroCount = Math.min(Integer.numberOfLeadingZeros(xorResult), 15);
            compressedBits += out.writeInt(leadingZeroCount, 4);
            compressedBits += out.writeInt(xorResult, Float.SIZE - leadingZeroCount);
            preValue = v;
        } else {
            compressedBits += out.writeBit(false);
            compressedBits += EliasDeltaCodec.encode(ZigZagCodec.encode(q) + 1, out);
            preValue = recoverValue;
        }
    }

    @Override
    public byte[] getBytes() {
        byte[] compressedBytes = Arrays.copyOf(out.getBuffer(), (int) Math.ceil(storedCompressedBits / 8.0));
        out.refresh();
        return compressedBytes;
    }

    @Override
    public void close() {
        addValue(Float.NaN);
        out.flush();
        preValue = 2;
        storedCompressedBits = compressedBits;
        compressedBits = 0;
    }

    @Override
    public long getCompressedSizeInBits() {
        return storedCompressedBits;
    }
}
