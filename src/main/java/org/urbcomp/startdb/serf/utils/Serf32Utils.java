package org.urbcomp.startdb.serf.utils;

public class Serf32Utils {
    public final static int END_SIGN = Float.floatToIntBits(Float.NaN);

    private static final int[] bw = new int[32];   // note: from right to left, bit weight in a integer value

    static {
        bw[0] = 1;
        for (int i = 1; i < 32; i++) {
            bw[i] = bw[i - 1] << 1;
        }
    }

    public static int findAppInt(float min, float max, float v, int lastInt, float maxDiff) {
        if (min >= 0) {
            // both positive
            return findAppInt(min, max, 0, v, lastInt, maxDiff);
        } else if (max <= 0) {
            // both negative
            return findAppInt(-max, -min, 0x80000000, v, lastInt, maxDiff);
        } else if (lastInt >= 0) {
            // consider positive part only, to make more leading zeros
            return findAppInt(0, max, 0, v, lastInt, maxDiff);
        } else {
            // consider negative part only, to make more leading zeros
            return findAppInt(0, -min, 0x80000000, v, lastInt, maxDiff);
        }
    }

    private static int findAppInt(float minFloat, float maxFloat, int sign,
                                    float original, int lastInt, float maxDiff) {
        int min = Float.floatToRawIntBits(minFloat) & 0x7fffffff; // may be negative zero
        int max = Float.floatToRawIntBits(maxFloat);
        int leadingZeros = Integer.numberOfLeadingZeros(min ^ max);
        int frontMask = 0xffffffff << (32 - leadingZeros);
        int shift = 32 - leadingZeros;
        int resultInt;
        float diff;
        int append;
        while (shift >= 0) {
            int front = frontMask & min;
            int rear = (~frontMask) & lastInt;

            append = rear | front;
            if (append >= min && append <= max) {
                resultInt = append ^ sign;
                diff = Float.intBitsToFloat(resultInt) - original;
                if (diff >= -maxDiff && diff <= maxDiff) {
                    return resultInt;
                }
            }

            append = (append + bw[shift]) & 0x7fffffff; // may be overflow
            if (append <= max) {    // append must be greater than min
                resultInt = append ^ sign;
                diff = Float.intBitsToFloat(resultInt) - original;
                if (diff >= -maxDiff && diff <= maxDiff) {
                    return resultInt;
                }
            }

            frontMask = frontMask >> 1;

            --shift;
        }

        return Float.floatToRawIntBits(original);    // we do not find a satisfied value, so we return the original value
    }
}
