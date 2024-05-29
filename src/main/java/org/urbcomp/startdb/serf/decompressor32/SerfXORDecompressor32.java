package org.urbcomp.startdb.serf.decompressor32;

import org.urbcomp.startdb.serf.utils.InputBitStream;
import org.urbcomp.startdb.serf.utils.PostOfficeSolver32;
import org.urbcomp.startdb.serf.utils.Serf32Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SerfXORDecompressor32 implements IDecompressor32 {
    private int storedVal = Float.floatToIntBits(2);
    private int storedLeadingZeros = Integer.MAX_VALUE;
    private int storedTrailingZeros = Integer.MAX_VALUE;
    private final InputBitStream in = new InputBitStream(new byte[0]);
    private int[] leadingRepresentation = {0, 8, 12, 16};
    private int[] trailingRepresentation = {0, 16};
    private int leadingBitsPerValue = 2;
    private int trailingBitsPerValue = 1;
    private boolean equalWin = false;

    public SerfXORDecompressor32() {  }

    @Override
    public List<Float> decompress(byte[] bs) {
        in.setBuffer(bs);
        try {
            updateFlagAndPositionsIfNeeded();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        List<Float> values = new ArrayList<>(1024);
        int value;
        while ((value = readValue()) != Serf32Utils.END_SIGN) {
            values.add(Float.intBitsToFloat(value));
            storedVal = value;
        }
        return values;
    }

    public int readValue() {
        try {
            int value = storedVal;
            int centerBits;

            if (equalWin) {
                if (in.readInt(1) == 0) {
                    if (in.readInt(1) != 1) {
                        // case 00
                        int leadAndTrail = in.readInt(leadingBitsPerValue + trailingBitsPerValue);
                        int lead = leadAndTrail >>> trailingBitsPerValue;
                        int trail = ~(0xffff << trailingBitsPerValue) & leadAndTrail;
                        storedLeadingZeros = leadingRepresentation[lead];
                        storedTrailingZeros = trailingRepresentation[trail];
                    }
                    centerBits = 32 - storedLeadingZeros - storedTrailingZeros;
                    value = in.readInt(centerBits) << storedTrailingZeros;
                    value = storedVal ^ value;
                }
            } else {
                if (in.readInt(1) == 1) {
                    // case 1
                    centerBits = 32 - storedLeadingZeros - storedTrailingZeros;

                    value = in.readInt(centerBits) << storedTrailingZeros;
                    value = storedVal ^ value;
                } else if (in.readInt(1) == 0) {
                    // case 00
                    int leadAndTrail = in.readInt(leadingBitsPerValue + trailingBitsPerValue);
                    int lead = leadAndTrail >>> trailingBitsPerValue;
                    int trail = ~(0xffff << trailingBitsPerValue) & leadAndTrail;
                    storedLeadingZeros = leadingRepresentation[lead];
                    storedTrailingZeros = trailingRepresentation[trail];
                    centerBits = 32 - storedLeadingZeros - storedTrailingZeros;

                    value = in.readInt(centerBits) << storedTrailingZeros;
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
        int num = in.readInt(4);
        if (num == 0) {
            num = 16;
        }
        leadingBitsPerValue = PostOfficeSolver32.positionLength2Bits[num];
        leadingRepresentation = new int[num];
        for (int i = 0; i < num; i++) {
            leadingRepresentation[i] = in.readInt(5);
        }
    }

    private void updateTrailingRepresentation() throws IOException {
        int num = in.readInt(4);
        if (num == 0) {
            num = 16;
        }
        trailingBitsPerValue = PostOfficeSolver32.positionLength2Bits[num];
        trailingRepresentation = new int[num];
        for (int i = 0; i < num; i++) {
            trailingRepresentation[i] = in.readInt(5);
        }
    }
}
