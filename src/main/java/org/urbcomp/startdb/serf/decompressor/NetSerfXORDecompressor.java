package org.urbcomp.startdb.serf.decompressor;

import org.urbcomp.startdb.serf.utils.InputBitStream;
import org.urbcomp.startdb.serf.utils.PostOfficeSolver;

import java.io.IOException;

public class NetSerfXORDecompressor implements INetDecompressor {
    private static final int BLOCK_SIZE = 1000;
    private long storedVal = Double.doubleToLongBits(2);
    private int storedLeadingZeros = Integer.MAX_VALUE;
    private int storedTrailingZeros = Integer.MAX_VALUE;
    private final InputBitStream in = new InputBitStream(new byte[0]);
    private int[] leadingRepresentation = {0, 8, 12, 16, 18, 20, 22, 24};
    private int[] trailingRepresentation = {0, 22, 28, 32, 36, 40, 42, 46};
    private int leadingBitsPerValue = 3;
    private int trailingBitsPerValue = 3;
    private int numberOfValues = 0;
    private boolean equalWin = false;
    private final long adjustD;

    public NetSerfXORDecompressor(long adjustD) {
        this.adjustD = adjustD;
    }

    @Override
    public double decompress(byte[] input) {
        in.setBuffer(input);
        return Double.longBitsToDouble(readValue()) - adjustD;
    }

    private long readValue() {
        try {
            // empty read 4 bits for getting rid of transmit header
            in.readInt(4);
            if (numberOfValues >= BLOCK_SIZE) {
                updateFlagAndPositionsIfNeeded();
            }
            nextValue();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        ++numberOfValues;
        return storedVal;
    }

    private void nextValue() throws IOException {
        long value;
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
                storedVal = value;
            }
        } else {
            if (in.readInt(1) == 1) {
                // case 1
                centerBits = 64 - storedLeadingZeros - storedTrailingZeros;

                value = in.readLong(centerBits) << storedTrailingZeros;
                value = storedVal ^ value;
                storedVal = value;
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
                storedVal = value;
            }
        }
    }

    private void updateFlagAndPositionsIfNeeded() throws IOException {
        equalWin = in.readBit() == 1;
        if (in.readBit() == 1) {
            updateLeadingRepresentation();
            updateTrailingRepresentation();
        }
        numberOfValues = 0;
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
