package org.urbcomp.startdb.compress.elf.utils;

import java.nio.ByteBuffer;

public class ByteToInt {

    public static int byteToInt(byte[] bytes) {
        int ans=0;
        for(int i=0;i<4;i++){
            ans<<=8;
            ans|=(bytes[3-i]&0xff);
        }
        return ans;
    }
}
