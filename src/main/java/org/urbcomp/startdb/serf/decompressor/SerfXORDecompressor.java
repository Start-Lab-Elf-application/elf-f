package org.urbcomp.startdb.serf.decompressor;

import org.urbcomp.startdb.serf.utils.InputBitStream;
import org.urbcomp.startdb.serf.utils.PostOfficeSolver;
import org.urbcomp.startdb.serf.utils.Serf64Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SerfXORDecompressor implements IDecompressor {
    private long storedVal = Double.doubleToLongBits(2);
    private int storedLeadingZeros = Integer.MAX_VALUE;
    private int storedTrailingZeros = Integer.MAX_VALUE;
    private final InputBitStream in = new InputBitStream(new byte[0]);
    private int[] leadingRepresentation = {0, 8, 12, 16, 18, 20, 22, 24};
    private int[] trailingRepresentation = {0, 22, 28, 32, 36, 40, 42, 46};
    private int leadingBitsPerValue = 3;
    private int trailingBitsPerValue = 3;
    private boolean equalWin = false;
    private final long adjustD;

    public SerfXORDecompressor(long adjustD) {
        this.adjustD = adjustD;
    }

    @Override
    public List<Double> decompress(byte[] bs) {
        in.setBuffer(bs);
        try {
            updateFlagAndPositionsIfNeeded();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        List<Double> values = new ArrayList<>(1024);
        long value;
        while ((value = readValue()) != Serf64Utils.END_SIGN) {
            values.add(Double.longBitsToDouble(value) - this.adjustD);
            storedVal = value;
        }
        return values;
    }

    private long readValue() {
        try {
            long value = storedVal;
            int centerBits;

            if (equalWin) {
                if (in.readInt(1) == 0) {
                    if (in.readInt(1) != 1) {
                        // case 00
                        int leadAndTrail = in.readInt(leadingBitsPerValue + trailingBitsPerValue);
                        int lead = leadAndTrail >>> trailingBitsPerValue;
                        int trail = ~(0xffffffff << trailingBitsPerValue) & leadAndTrail;
                        storedLeadingZeros = leadingRepresentation[lead];
                        storedTrailingZeros = trailingRepresentation[trail];
                    }
                    centerBits = 64 - storedLeadingZeros - storedTrailingZeros;
                    value = in.readLong(centerBits) << storedTrailingZeros;
                    value = storedVal ^ value;
                }
            } else {
                if (in.readInt(1) == 1) {
                    // case 1
                    centerBits = 64 - storedLeadingZeros - storedTrailingZeros;

                    value = in.readLong(centerBits) << storedTrailingZeros;
                    value = storedVal ^ value;
                } else if (in.readInt(1) == 0) {
                    // case 00
                    int leadAndTrail = in.readInt(leadingBitsPerValue + trailingBitsPerValue);
                    int lead = leadAndTrail >>> trailingBitsPerValue;
                    int trail = ~(0xffffffff << trailingBitsPerValue) & leadAndTrail;
                    storedLeadingZeros = leadingRepresentation[lead];
                    storedTrailingZeros = trailingRepresentation[trail];
                    centerBits = 64 - storedLeadingZeros - storedTrailingZeros;

                    value = in.readLong(centerBits) << storedTrailingZeros;
                    value = storedVal ^ value;
                }
            }
            return value;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void updateFlagAndPositionsIfNeeded() throws IOException {
        equalWin = in.readBit() == 1;
        if (in.readBit() == 1) {
            updateLeadingRepresentation();
            updateTrailingRepresentation();
        }
    }

    private void updateLeadingRepresentation() throws IOException {
        int num = in.readInt(5);
        if (num == 0) {
            num = 32;
        }
        leadingBitsPerValue = PostOfficeSolver.positionLength2Bits[num];
        leadingRepresentation = new int[num];
        for (int i = 0; i < num; i++) {
            leadingRepresentation[i] = in.readInt(6);
        }
    }

    private void updateTrailingRepresentation() throws IOException {
        int num = in.readInt(5);
        if (num == 0) {
            num = 32;
        }
        trailingBitsPerValue = PostOfficeSolver.positionLength2Bits[num];
        trailingRepresentation = new int[num];
        for (int i = 0; i < num; i++) {
            trailingRepresentation[i] = in.readInt(6);
        }
    }
}
