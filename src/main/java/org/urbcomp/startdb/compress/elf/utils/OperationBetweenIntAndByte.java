package org.urbcomp.startdb.compress.elf.utils;

public class OperationBetweenIntAndByte {

    public static byte[] intToTwoBytes(int value) {
        byte[] result = new byte[2];
        result[0] = (byte) ((value >> 8) & 0xFF);
        result[1] = (byte) (value & 0xFF);
        return result;
    }

    public static int twoBytesToInt(byte[] bytes) {
        int result = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
        return result;
    }

    public static int oneBytesToInt(byte abyte) {
        int result = (abyte & 0xFF);
        return result;
    }
}
