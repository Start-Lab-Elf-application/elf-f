package org.urbcomp.startdb.serf.compressor;

import org.urbcomp.startdb.serf.utils.OutputBitStream;
import org.urbcomp.startdb.serf.utils.PostOfficeSolver;
import org.urbcomp.startdb.serf.utils.Serf64Utils;

import java.util.Arrays;

public class SerfXORCompressor implements ICompressor {
    private final double maxDiff;
    private long storedVal = Double.doubleToLongBits(2);
    private long compressedSizeInBits;      // total number of compressed bits in this block
    private long storedCompressedSizeInBits = 0;    // since we will reset compressedSizeInBits, we use it for backup
    private final OutputBitStream out;
    private byte[] outBuffer;
    private int numberOfValues = 0;
    private double storedCompressionRatio = 0;
    private int equalVote = 0;
    private boolean equalWin = false;

    private final long adjustD;

    private final int[] leadingRepresentation = {
        0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 2, 2, 2, 2,
        3, 3, 4, 4, 5, 5, 6, 6,
        7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7
    };
    private final int[] leadingRound = {
        0, 0, 0, 0, 0, 0, 0, 0,
        8, 8, 8, 8, 12, 12, 12, 12,
        16, 16, 18, 18, 20, 20, 22, 22,
        24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24
    };
    private final int[] trailingRepresentation = {
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 1,
        1, 1, 1, 1, 2, 2, 2, 2,
        3, 3, 3, 3, 4, 4, 4, 4,
        5, 5, 6, 6, 6, 6, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7,
        7, 7, 7, 7, 7, 7, 7, 7,
    };
    private final int[] trailingRound = {
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 22, 22,
        22, 22, 22, 22, 28, 28, 28, 28,
        32, 32, 32, 32, 36, 36, 36, 36,
        40, 40, 42, 42, 42, 42, 46, 46,
        46, 46, 46, 46, 46, 46, 46, 46,
        46, 46, 46, 46, 46, 46, 46, 46,
    };
    private int leadingBitsPerValue = 3;
    private int trailingBitsPerValue = 3;
    private final int[] leadDistribution = new int[64];
    private final int[] trailDistribution = new int[64];
    private int storedLeadingZeros = Integer.MAX_VALUE;
    private int storedTrailingZeros = Integer.MAX_VALUE;

    public SerfXORCompressor(int capacity, double maxDiff, long adjustD) {
        this.maxDiff = maxDiff;
        this.out = new OutputBitStream(new byte[(int) (((capacity + 1) * 8 + capacity / 8 + 1) * 1.2)]);
        this.compressedSizeInBits = out.writeInt(0, 2);
        this.adjustD = adjustD;
    }

    @Override
    public void addValue(double v) {
        long thisVal;
        // note we cannot let > maxDiff, because NaN - v > maxDiff is always false
        if (!(Math.abs(Double.longBitsToDouble(storedVal) - adjustD - v) <= maxDiff)) {
            // in our implementation, we do not consider special cases and overflow case
            double adjustValue = v + adjustD;
            thisVal = Serf64Utils.findAppLong(adjustValue - maxDiff, adjustValue + maxDiff, v, storedVal, maxDiff, adjustD);
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
        compressedSizeInBits += compressValue(Serf64Utils.END_SIGN);
        out.flush();
        outBuffer = Arrays.copyOf(out.getBuffer(), (int) Math.ceil(compressedSizeInBits / 8.0));
        out.refresh();
        storedCompressedSizeInBits = compressedSizeInBits;
        compressedSizeInBits = updateFlagAndPositionsIfNeeded();
    }

    private int compressValue(long value) {
        int thisSize = 0;
        long xor = storedVal ^ value;

        if (xor == 0) {
            // case 01
            if (equalWin) {
                thisSize += out.writeBit(true);
            } else {
                thisSize += out.writeInt(1, 2);
            }
            equalVote++;
        } else {
            int leadingCount = Long.numberOfLeadingZeros(xor);
            int trailingCount = Long.numberOfTrailingZeros(xor);
            int leadingZeros = leadingRound[leadingCount];
            int trailingZeros = trailingRound[trailingCount];
            ++leadDistribution[leadingCount];
            ++trailDistribution[trailingCount];

            if (leadingZeros >= storedLeadingZeros && trailingZeros >= storedTrailingZeros &&
                (leadingZeros - storedLeadingZeros) + (trailingZeros - storedTrailingZeros) < 1 + leadingBitsPerValue + trailingBitsPerValue) {
                // case 1
                int centerBits = 64 - storedLeadingZeros - storedTrailingZeros;
                int len;
                if (equalWin) {
                    len = 2 + centerBits;
                    if (len > 64) {
                        out.writeInt(1, 2);
                        out.writeLong(xor >>> storedTrailingZeros, centerBits);
                    } else {
                        out.writeLong((1L << centerBits) | (xor >>> storedTrailingZeros), 2 + centerBits);
                    }
                } else {
                    len = 1 + centerBits;
                    if (len > 64) {
                        out.writeInt(1, 1);
                        out.writeLong(xor >>> storedTrailingZeros, centerBits);
                    } else {
                        out.writeLong((1L << centerBits) | (xor >>> storedTrailingZeros), 1 + centerBits);
                    }
                }
                thisSize += len;
                equalVote--;
            } else {
                storedLeadingZeros = leadingZeros;
                storedTrailingZeros = trailingZeros;
                int centerBits = 64 - storedLeadingZeros - storedTrailingZeros;

                // case 00
                int len = 2 + leadingBitsPerValue + trailingBitsPerValue + centerBits;
                if (len > 64) {
                    out.writeInt((leadingRepresentation[storedLeadingZeros] << trailingBitsPerValue)
                        | trailingRepresentation[storedTrailingZeros], 2 + leadingBitsPerValue + trailingBitsPerValue);
                    out.writeLong(xor >>> storedTrailingZeros, centerBits);
                } else {
                    out.writeLong(
                        ((((long) leadingRepresentation[storedLeadingZeros] << trailingBitsPerValue) |
                            trailingRepresentation[storedTrailingZeros]) << centerBits) | (xor >>> storedTrailingZeros),
                        len
                    );
                }
                thisSize += len;
            }
        }
        return thisSize;
    }

    private int updateFlagAndPositionsIfNeeded() {
        int len;
        equalWin = equalVote > 0;
        double thisCompressionRatio = compressedSizeInBits / (numberOfValues * 64.0);
        if (storedCompressionRatio < thisCompressionRatio) {
            // update positions
            int[] leadPositions = PostOfficeSolver.initRoundAndRepresentation(leadDistribution, leadingRepresentation, leadingRound);
            leadingBitsPerValue = PostOfficeSolver.positionLength2Bits[leadPositions.length];
            int[] trailPositions = PostOfficeSolver.initRoundAndRepresentation(trailDistribution, trailingRepresentation, trailingRound);
            trailingBitsPerValue = PostOfficeSolver.positionLength2Bits[trailPositions.length];
            len = out.writeInt(equalWin ? 3 : 1, 2)
                + PostOfficeSolver.writePositions(leadPositions, out)
                + PostOfficeSolver.writePositions(trailPositions, out);
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