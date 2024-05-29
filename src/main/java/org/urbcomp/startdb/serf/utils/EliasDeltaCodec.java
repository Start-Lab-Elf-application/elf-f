package org.urbcomp.startdb.serf.utils;

import java.io.IOException;

public class EliasDeltaCodec {
    private static final double[] logTable = {
        Double.NaN,
        Math.log(1),
        Math.log(2),
        Math.log(3),
        Math.log(4),
        Math.log(5),
        Math.log(6),
        Math.log(7),
        Math.log(8),
        Math.log(9),
        Math.log(10),
        Math.log(11)
    };

    public static int encode(long number, OutputBitStream outputBitStream) {
        int compressedBits = 0;
        int len;
        int lengthOfLen;
        if (number <= 11) {
            len = 1 + (int) Math.floor(logTable[(int) number] / logTable[2]);
        } else {
            len = 1 + (int) Math.floor(Math.log(number) / logTable[2]);
        }
        if (len <= 11) {
            lengthOfLen = (int) Math.floor(logTable[len] / logTable[2]);
        } else {
            lengthOfLen = (int) Math.floor(Math.log(len) / logTable[2]);
        }
        int totalLen = lengthOfLen + lengthOfLen + len;
        if (totalLen <= 64) {
            compressedBits += outputBitStream.writeLong(((long) len << (len - 1)) |
                (number & ~(0xffffffffffffffffL << (len - 1))), totalLen);
        } else {
            compressedBits += outputBitStream.writeInt(0, lengthOfLen);
            compressedBits += outputBitStream.writeInt(len, lengthOfLen + 1);
            compressedBits += outputBitStream.writeLong(number, len - 1);
        }
        return compressedBits;
    }

    public static long decode(InputBitStream inputBitStream) throws IOException {
        long num = 1;
        int len = 1;
        int lengthOfLen = 0;
        while (inputBitStream.readBit() == 0)
            lengthOfLen++;
        len <<= lengthOfLen;
        len |= inputBitStream.readInt(lengthOfLen);
        num <<= (len - 1);
        num |= inputBitStream.readLong(len - 1);
        return num;
    }
}
