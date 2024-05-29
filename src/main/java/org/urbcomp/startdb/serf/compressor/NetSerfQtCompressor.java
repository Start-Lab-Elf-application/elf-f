package org.urbcomp.startdb.serf.compressor;

import org.urbcomp.startdb.serf.utils.EliasDeltaCodec;
import org.urbcomp.startdb.serf.utils.OutputBitStream;
import org.urbcomp.startdb.serf.utils.ZigZagCodec;

import java.util.Arrays;

public class NetSerfQtCompressor implements INetCompressor {
    private double preValue = 2;
    private final double maxDiff;
    private final OutputBitStream out = new OutputBitStream(new byte[5 * Double.BYTES]);

    public NetSerfQtCompressor(double errorBound) {
        this.maxDiff = errorBound;
    }

    @Override
    public byte[] compress(double v) {
        int writtenBitsCount = out.writeInt(0, 4);
        long q = Math.round((v - preValue) / (2 * maxDiff));
        double recoverValue = preValue + 2 * maxDiff * q;
        if (!(Math.abs(recoverValue - v) <= maxDiff)) {
            writtenBitsCount += out.writeBit(true);
            long xorResult = Double.doubleToLongBits(v) ^ Double.doubleToLongBits(preValue);
            int leadingZeroCount = Math.min(Long.numberOfLeadingZeros(xorResult), 31);
            writtenBitsCount += out.writeInt(leadingZeroCount, 5);
            writtenBitsCount += out.writeLong(xorResult, Double.SIZE - leadingZeroCount);
            preValue = v;
        } else {
            writtenBitsCount += out.writeBit(false);
            writtenBitsCount += EliasDeltaCodec.encode(ZigZagCodec.encode(q) + 1, out);
            preValue = recoverValue;
        }
        out.flush();
        byte[] result = Arrays.copyOf(out.getBuffer(), (int) Math.ceil(writtenBitsCount / 8.0));
        out.refresh();
        return result;
    }
}
