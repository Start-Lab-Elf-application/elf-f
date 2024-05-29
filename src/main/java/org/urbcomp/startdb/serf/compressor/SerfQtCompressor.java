package org.urbcomp.startdb.serf.compressor;

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
 * |Double Value+---+
 * +------------+   |
 *                  |               +---------+----------------+-------------------+
 *                  |               |SignalBit|LeadingZeroCount|      LeftBits     |
 *                  +-------------->+---------+----------------+-------------------+
 *                   exception      |    1    |      5bits     |64-leadingZeroCount|
 *                                  +---------+----------------+-------------------+
 */

public class SerfQtCompressor implements ICompressor {
    private static final int BLOCK_SIZE = 1000;
    private final double maxDiff;
    private final OutputBitStream out = new OutputBitStream(new byte[2 * BLOCK_SIZE * Double.BYTES]);
    private double preValue = 2;
    private long compressedBits = 0;
    private long storedCompressedBits;

    public SerfQtCompressor(double maxDiff) {
        this.maxDiff = maxDiff;
    }

    @Override
    public void addValue(double v) {
        long q = Math.round((v - preValue) / (2 * maxDiff));
        double recoverValue = preValue + 2 * maxDiff * q;
        if (!(Math.abs(recoverValue - v) <= maxDiff)) {
            // small cases
            compressedBits += out.writeBit(true);
            long xorResult = Double.doubleToLongBits(v) ^ Double.doubleToLongBits(preValue);
            int leadingZeroCount = Math.min(Long.numberOfLeadingZeros(xorResult), 31);
            compressedBits += out.writeInt(leadingZeroCount, 5);
            compressedBits += out.writeLong(xorResult, Double.SIZE - leadingZeroCount);
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
        addValue(Double.NaN);
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
