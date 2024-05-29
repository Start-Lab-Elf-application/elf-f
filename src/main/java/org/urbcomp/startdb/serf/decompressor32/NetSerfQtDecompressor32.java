package org.urbcomp.startdb.serf.decompressor32;

import org.urbcomp.startdb.serf.utils.EliasDeltaCodec;
import org.urbcomp.startdb.serf.utils.InputBitStream;
import org.urbcomp.startdb.serf.utils.ZigZagCodec;

import java.io.IOException;

public class NetSerfQtDecompressor32 implements INetDecompressor32 {
    private final float maxDiff;
    private float preValue = 2;
    private final InputBitStream in = new InputBitStream(new byte[0]);

    public NetSerfQtDecompressor32(float maxDiff) {
        this.maxDiff = maxDiff;
    }

    @Override
    public float decompress(byte[] input) {
        try {
            in.setBuffer(input);
            in.readInt(4);
            int exceptionFlag = in.readInt(1);
            if (exceptionFlag == 0) {
                long decodeValue = ZigZagCodec.decode(EliasDeltaCodec.decode(in) - 1);
                preValue = preValue + 2 * maxDiff * decodeValue;
            } else {
                int leadingZeroCount = in.readInt(4);
                int leftBits = in.readInt(Float.SIZE - leadingZeroCount);
                preValue = Float.intBitsToFloat(leftBits ^ Float.floatToIntBits(preValue));
            }
            return preValue;
        } catch (IOException e) {
            return Float.NaN;
        }
    }
}
