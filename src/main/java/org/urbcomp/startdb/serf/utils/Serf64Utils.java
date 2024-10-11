package org.urbcomp.startdb.serf.utils;

public class Serf64Utils {
    public final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    private static final long[] bw = new long[64];   // note: from right to left, bit weight in a long value

    static {
        bw[0] = 1;
        for (int i = 1; i < 64; i++) {
            bw[i] = bw[i - 1] << 1;
        }
    }

    public static long findAppLong(double min, double max, double v, long lastLong, double maxDiff, double adjustD) {
        if (min >= 0) {
            // both positive
            return findAppLong(min, max, 0, v, lastLong, maxDiff, adjustD);
        } else if (max <= 0) {
            // both negative
            return findAppLong(-max, -min, 0x8000000000000000L, v, lastLong, maxDiff, adjustD);
        } else if (lastLong >= 0) {
            // consider positive part only, to make more leading zeros
            return findAppLong(0, max, 0, v, lastLong, maxDiff, adjustD);
        } else {
            // consider negative part only, to make more leading zeros
            return findAppLong(0, -min, 0x8000000000000000L, v, lastLong, maxDiff, adjustD);
        }
    }

    // We suppose min, max are both positive.
    private static long findAppLong(double minDouble, double maxDouble, long sign,
                                    double original, long lastLong, double maxDiff, double adjustD) {
        long min = Double.doubleToRawLongBits(minDouble) & 0x7fffffffffffffffL; // may be negative zero
        long max = Double.doubleToRawLongBits(maxDouble);
        int leadingZeros = Long.numberOfLeadingZeros(min ^ max);
        long frontMask = 0xffffffffffffffffL << (64 - leadingZeros);
        int shift = 64 - leadingZeros;
        long resultLong;
        double diff;
        long append;
        while (shift >= 0) {
            long front = frontMask & min;
            long rear = (~frontMask) & lastLong;

            append = rear | front;
            if (append >= min && append <= max) {
                resultLong = append ^ sign;
                diff = Double.longBitsToDouble(resultLong) - adjustD - original;
                if (diff >= -maxDiff && diff <= maxDiff) {
                    return resultLong;
                }
            }

            append = (append + bw[shift]) & 0x7fffffffffffffffL; // may be overflow
            if (append <= max) {    // append must be greater than min
                resultLong = append ^ sign;
                diff = Double.longBitsToDouble(resultLong) - adjustD - original;
                if (diff >= -maxDiff && diff <= maxDiff) {
                    return resultLong;
                }
            }

            frontMask = frontMask >> 1;

            --shift;
        }

        return Double.doubleToRawLongBits(original + adjustD);    // we do not find a satisfied value, so we return the original value
    }
}