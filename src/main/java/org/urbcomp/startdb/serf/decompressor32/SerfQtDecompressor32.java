package org.urbcomp.startdb.serf.decompressor32;

import org.urbcomp.startdb.serf.utils.EliasDeltaCodec;
import org.urbcomp.startdb.serf.utils.InputBitStream;
import org.urbcomp.startdb.serf.utils.ZigZagCodec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SerfQtDecompressor32 implements IDecompressor32 {
    private final float maxDiff;
    private final InputBitStream in = new InputBitStream(new byte[0]);
    private float preValue;

    public SerfQtDecompressor32(float maxDiff) {
        this.maxDiff = maxDiff;
    }

    @Override
    public List<Float> decompress(byte[] bs) {
        preValue = 2;
        in.setBuffer(bs);
        List<Float> decompressedValueList = new ArrayList<>(1024);
        Float value;
        while ((value = nextValue()) != null) {
            decompressedValueList.add(value);
        }
        return decompressedValueList;
    }

    private Float nextValue() {
        try {
            float returnValue;
            int exceptionFlag = in.readInt(1);
            if (exceptionFlag == 0) {
                long decodeValue = ZigZagCodec.decode(EliasDeltaCodec.decode(in) - 1);
                float recoverValue = preValue + 2 * maxDiff * decodeValue;
                preValue = recoverValue;
                returnValue = recoverValue;
            } else {
                int leadingZeroCount = in.readInt(4);
                int leftBits = in.readInt(Float.SIZE - leadingZeroCount);
                float recoverValue = Float.intBitsToFloat(leftBits ^ Float.floatToIntBits(preValue));
                preValue = recoverValue;
                returnValue = recoverValue;
            }
            return Float.isNaN(returnValue) ? null : returnValue;
        } catch (IOException e) {
            return null;
        }
    }
}
