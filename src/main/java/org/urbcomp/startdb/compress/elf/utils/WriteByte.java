package org.urbcomp.startdb.compress.elf.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class WriteByte {
    public static void insert(String srcFilePath, String destFilePath, int strHeadInt){
        byte[] strHead = WriteByteToCSV.intToBytes(strHeadInt);
        long startTime = System.currentTimeMillis();
        try {
            // 映射原文件到内存
            RandomAccessFile srcRandomAccessFile = new RandomAccessFile(srcFilePath, "r");
            FileChannel srcAccessFileChannel = srcRandomAccessFile.getChannel();
            long srcLength = srcAccessFileChannel.size();
            System.out.println("src file size:"+srcLength);  // src file size:296354010
            MappedByteBuffer srcMap = srcAccessFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, srcLength);


            // 映射目标文件到内存
            RandomAccessFile destRandomAccessFile = new RandomAccessFile(destFilePath, "rw");
            FileChannel destAccessFileChannel = destRandomAccessFile.getChannel();
//            long destLength = srcLength + strHead.getBytes().length;
            long destLength = srcLength + strHead.length;
            System.out.println("dest file size:"+destLength);  // dest file size:296354025
            MappedByteBuffer destMap = destAccessFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, destLength);

            // 开始文件追加 : 先添加头部内容，再添加原来文件内容
            destMap.position(0);
//            destMap.put(strHead.getBytes());
            destMap.put(strHead);
            destMap.put(srcMap);
            destAccessFileChannel.close();
            File file = new File(srcFilePath);
            System.out.println("dest real file size:"+new RandomAccessFile(destFilePath,"r").getChannel().size());
            System.out.println("total time :" + (System.currentTimeMillis() - startTime));// 貌似时间不准确，异步操作？
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void insert2(String srcFilePath, String destFilePath, byte[] strHead){
        long startTime = System.currentTimeMillis();
        try {
            // 映射原文件到内存
            RandomAccessFile srcRandomAccessFile = new RandomAccessFile(srcFilePath, "r");
            FileChannel srcAccessFileChannel = srcRandomAccessFile.getChannel();
            long srcLength = srcAccessFileChannel.size();
            System.out.println("src file size:"+srcLength);  // src file size:296354010
            MappedByteBuffer srcMap = srcAccessFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, srcLength);


            // 映射目标文件到内存
            RandomAccessFile destRandomAccessFile = new RandomAccessFile(destFilePath, "rw");
            FileChannel destAccessFileChannel = destRandomAccessFile.getChannel();
//            long destLength = srcLength + strHead.getBytes().length;
            long destLength = srcLength + strHead.length;
            System.out.println("dest file size:"+destLength);  // dest file size:296354025
            MappedByteBuffer destMap = destAccessFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, destLength);

            // 开始文件追加 : 先添加头部内容，再添加原来文件内容
            destMap.position(0);
//            destMap.put(strHead.getBytes());
            destMap.put(strHead);
            destMap.put(srcMap);
            destAccessFileChannel.close();
            File file = new File(srcFilePath);
            System.out.println("dest real file size:"+new RandomAccessFile(destFilePath,"r").getChannel().size());
            System.out.println("total time :" + (System.currentTimeMillis() - startTime));// 貌似时间不准确，异步操作？
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
