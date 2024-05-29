package org.urbcomp.startdb.serf.decompressor;

import org.urbcomp.startdb.serf.utils.EliasDeltaCodec;
import org.urbcomp.startdb.serf.utils.InputBitStream;
import org.urbcomp.startdb.serf.utils.ZigZagCodec;

import java.io.IOException;

public class NetSerfQtDecompressor implements INetDecompressor {
    private final double maxDiff;
    private double preValue = 2;
    private final InputBitStream in = new InputBitStream(new byte[0]);

    public NetSerfQtDecompressor(double maxDiff) {
        this.maxDiff = maxDiff;
    }

    @Override
    public double decompress(byte[] input) {
        try {
            in.setBuffer(input);
            in.readInt(4);
            int exceptionFlag = in.readInt(1);
            if (exceptionFlag == 0) {
                long decodeValue = ZigZagCodec.decode(EliasDeltaCodec.decode(in) - 1);
                preValue = preValue + 2 * maxDiff * decodeValue;
            } else {
                int leadingZeroCount = in.readInt(5);
                long leftBits = in.readLong(Double.SIZE - leadingZeroCount);
                preValue = Double.longBitsToDouble(leftBits ^ Double.doubleToLongBits(preValue));
            }
            return preValue;
        } catch (IOException e) {
            return Double.NaN;
        }
    }
}
