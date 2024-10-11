package org.urbcomp.startdb.serf.decompressor;

import org.urbcomp.startdb.serf.utils.EliasDeltaCodec;
import org.urbcomp.startdb.serf.utils.InputBitStream;
import org.urbcomp.startdb.serf.utils.ZigZagCodec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SerfQtDecompressor implements IDecompressor {
    private final double maxDiff;
    private final InputBitStream in = new InputBitStream(new byte[0]);
    private double preValue;

    public SerfQtDecompressor(double maxDiff) {
        this.maxDiff = maxDiff;
    }

    @Override
    public List<Double> decompress(byte[] bs) {
        preValue = 2;
        in.setBuffer(bs);
        List<Double> decompressedValueList = new ArrayList<>(1024);
        Double value;
        while ((value = nextValue()) != null) {
            decompressedValueList.add(value);
        }
        return decompressedValueList;
    }

    private Double nextValue() {
        try {
            double returnValue;
            int exceptionFlag = in.readInt(1);
            if (exceptionFlag == 0) {
                long decodeValue = ZigZagCodec.decode(EliasDeltaCodec.decode(in) - 1);
                double recoverValue = preValue + 2 * maxDiff * decodeValue;
                preValue = recoverValue;
                returnValue = recoverValue;
            } else {
                int leadingZeroCount = in.readInt(5);
                long leftBits = in.readLong(Double.SIZE - leadingZeroCount);
                double recoverValue = Double.longBitsToDouble(leftBits ^ Double.doubleToLongBits(preValue));
                preValue = recoverValue;
                returnValue = recoverValue;
            }
            return Double.isNaN(returnValue) ? null : returnValue;
        } catch (IOException e) {
            return null;
        }
    }
}
