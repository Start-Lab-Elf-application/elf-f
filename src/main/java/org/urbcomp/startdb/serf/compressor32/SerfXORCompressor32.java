package org.urbcomp.startdb.serf.compressor32;

import org.urbcomp.startdb.serf.utils.OutputBitStream;
import org.urbcomp.startdb.serf.utils.PostOfficeSolver32;
import org.urbcomp.startdb.serf.utils.Serf32Utils;

import java.util.Arrays;

public class SerfXORCompressor32 implements ICompressor32 {
    private final float maxDiff;
    private int storedVal = Float.floatToIntBits(2);
    private long compressedSizeInBits;
    private long storedCompressedSizeInBits = 0;
    private final OutputBitStream out;
    private byte[] outBuffer;
    private int numberOfValues = 0;
    private double storedCompressionRatio = 0;
    private int equalVote = 0;
    private boolean equalWin = false;

    private final int[] leadingRepresentation = {
        0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 2, 2, 2, 2,
        3, 3, 3, 3, 3, 3, 3, 3,
        3, 3, 3, 3, 3, 3, 3, 3,
    };
    private final int[] leadingRound = {
        0, 0, 0, 0, 0, 0, 0, 0,
        8, 8, 8, 8, 12, 12, 12, 12,
        16, 16, 16, 16, 16, 16, 16, 16,
        16, 16, 16, 16, 16, 16, 16, 16
    };
    private final int[] trailingRepresentation = {
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1
    };
    private final int[] trailingRound = {
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        16, 16, 16, 16, 16, 16, 16, 16,
        16, 16, 16, 16, 16, 16, 16, 16
    };
    private int leadingBitsPerValue = 2;
    private int trailingBitsPerValue = 1;
    private final int[] leadDistribution = new int[32];
    private final int[] trailDistribution = new int[32];
    private int storedLeadingZeros = Integer.MAX_VALUE;
    private int storedTrailingZeros = Integer.MAX_VALUE;

    // for float values, we do not adopt shift technique, since we may lose information with small maxDiff with add and minus operations
    public SerfXORCompressor32(int capacity, float maxDiff) {
        this.maxDiff = maxDiff;
        this.out = new OutputBitStream(new byte[(int) (((capacity + 1) * 4 + capacity / 4 + 1) * 1.2)]);
        this.compressedSizeInBits = out.writeInt(0, 2);
    }

    @Override
    public void addValue(float v) {
        int thisVal;
        // note we cannot let > maxDiff, because NaN - v > maxDiff is always false
        if (!(Math.abs(Float.intBitsToFloat(storedVal) - v) <= maxDiff)) {
            // in our implementation, we do not consider special cases and overflow case
            thisVal = Serf32Utils.findAppInt(v - maxDiff, v + maxDiff, v, storedVal, maxDiff);
        } else {
            // let current value be the last value, making an XORed value of 0.
            thisVal = storedVal;
        }

        compressedSizeInBits += compressValue(thisVal);
        storedVal = thisVal;
        ++numberOfValues;
    }

    @Override
    public long getCompressedSizeInBits() {
        return storedCompressedSizeInBits;
    }

    @Override
    public byte[] getBytes() {
        return outBuffer;
    }

    @Override
    public void close() {
        compressedSizeInBits += compressValue(Serf32Utils.END_SIGN);
        out.flush();
        outBuffer = Arrays.copyOf(out.getBuffer(), (int) Math.ceil(compressedSizeInBits / 8.0));
        out.refresh();
        storedCompressedSizeInBits = compressedSizeInBits;
        compressedSizeInBits = updateFlagAndPositionsIfNeeded();
    }

    private int compressValue(int value) {
        int thisSize = 0;
        int xor = storedVal ^ value;

        if (xor == 0) {
            // case 01
            if (equalWin) {
                thisSize += out.writeBit(true);
            } else {
                thisSize += out.writeInt(1, 2);
            }
            equalVote++;
        } else {
            int leadingCount = Integer.numberOfLeadingZeros(xor);
            int trailingCount = Integer.numberOfTrailingZeros(xor);
            int leadingZeros = leadingRound[leadingCount];
            int trailingZeros = trailingRound[trailingCount];
            ++leadDistribution[leadingCount];
            ++trailDistribution[trailingCount];

            if (leadingZeros >= storedLeadingZeros && trailingZeros >= storedTrailingZeros &&
                (leadingZeros - storedLeadingZeros) + (trailingZeros - storedTrailingZeros) < 1 + leadingBitsPerValue + trailingBitsPerValue) {
                // case 1
                int centerBits = 32 - storedLeadingZeros - storedTrailingZeros;
                int len;
                if (equalWin) {
                    len = 2 + centerBits;
                    if (len > 32) {
                        out.writeInt(1, 2);
                        out.writeInt(xor >>> storedTrailingZeros, centerBits);
                    } else {
                        out.writeInt((1 << centerBits) | (xor >>> storedTrailingZeros), 2 + centerBits);
                    }
                } else {
                    len = 1 + centerBits;
                    if (len > 32) {
                        out.writeInt(1, 1);
                        out.writeInt(xor >>> storedTrailingZeros, centerBits);
                    } else {
                        out.writeInt((1 << centerBits) | (xor >>> storedTrailingZeros), 1 + centerBits);
                    }
                }
                thisSize += len;
                equalVote--;
            } else {
                storedLeadingZeros = leadingZeros;
                storedTrailingZeros = trailingZeros;
                int centerBits = 32 - storedLeadingZeros - storedTrailingZeros;

                // case 00
                int len = 2 + leadingBitsPerValue + trailingBitsPerValue + centerBits;
                if (len > 32) {
                    out.writeInt((leadingRepresentation[storedLeadingZeros] << trailingBitsPerValue)
                        | trailingRepresentation[storedTrailingZeros], 2 + leadingBitsPerValue + trailingBitsPerValue);
                    out.writeInt(xor >>> storedTrailingZeros, centerBits);
                } else {
                    out.writeInt((((leadingRepresentation[storedLeadingZeros] << trailingBitsPerValue)
                        | trailingRepresentation[storedTrailingZeros]) << centerBits)
                        | (xor >>> storedTrailingZeros), len);
                }
                thisSize += len;
            }
        }
        return thisSize;
    }

    private int updateFlagAndPositionsIfNeeded() {
        int len;
        equalWin = equalVote > 0;
        double thisCompressionRatio = compressedSizeInBits / (numberOfValues * 32.0);
        if (storedCompressionRatio < thisCompressionRatio) {
            // update positions
            int[] leadPositions = PostOfficeSolver32.initRoundAndRepresentation(leadDistribution, leadingRepresentation, leadingRound);
            leadingBitsPerValue = PostOfficeSolver32.positionLength2Bits[leadPositions.length];
            int[] trailPositions = PostOfficeSolver32.initRoundAndRepresentation(trailDistribution, trailingRepresentation, trailingRound);
            trailingBitsPerValue = PostOfficeSolver32.positionLength2Bits[trailPositions.length];
            len = out.writeInt(equalWin ? 3 : 1, 2)
                + PostOfficeSolver32.writePositions(leadPositions, out)
                + PostOfficeSolver32.writePositions(trailPositions, out);
        } else {
            len = out.writeInt(equalWin ? 2 : 0, 2);
        }
        equalVote = 0;
        storedCompressionRatio = thisCompressionRatio;
        numberOfValues = 0;
        Arrays.fill(leadDistribution, 0);
        Arrays.fill(trailDistribution, 0);
        return len;
    }
}
