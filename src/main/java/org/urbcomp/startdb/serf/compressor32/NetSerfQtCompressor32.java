package org.urbcomp.startdb.serf.compressor32;

import org.urbcomp.startdb.serf.utils.EliasDeltaCodec;
import org.urbcomp.startdb.serf.utils.OutputBitStream;
import org.urbcomp.startdb.serf.utils.ZigZagCodec;

import java.util.Arrays;

public class NetSerfQtCompressor32 implements INetCompressor32 {
    private float preValue = 2;
    private final float maxDiff;
    private final OutputBitStream out = new OutputBitStream(new byte[5 * Float.BYTES]);

    public NetSerfQtCompressor32(float errorBound) {
        this.maxDiff = errorBound;
    }

    @Override
    public byte[] compress(float v) {
        int writtenBitsCount = out.writeInt(0, 4);
        long q = Math.round((v - preValue) / (2 * maxDiff));
        float recoverValue = preValue + 2 * maxDiff * q;
        if (Math.abs(recoverValue - v) > maxDiff || Float.isNaN(v)) {
            writtenBitsCount += out.writeBit(true);
            int xorResult = Float.floatToIntBits(v) ^ Float.floatToIntBits(preValue);
            int leadingZeroCount = Math.min(Integer.numberOfLeadingZeros(xorResult), 15);
            writtenBitsCount += out.writeInt(leadingZeroCount, 4);
            writtenBitsCount += out.writeInt(xorResult, Float.SIZE - leadingZeroCount);
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
