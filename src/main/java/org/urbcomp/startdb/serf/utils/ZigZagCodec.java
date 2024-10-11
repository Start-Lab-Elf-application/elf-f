package org.urbcomp.startdb.serf.utils;

public class ZigZagCodec {
    public static long encode(long value) {
        return (value << 1) ^ (value >> 63);
    }

    public static long decode(long value) {
        return (value >>> 1) ^ -(value & 1);
    }
}
