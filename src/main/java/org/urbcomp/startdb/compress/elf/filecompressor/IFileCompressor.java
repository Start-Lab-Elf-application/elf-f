package org.urbcomp.startdb.compress.elf.filecompressor;

public interface IFileCompressor {
    void setoutputFilePath(String path);
    void setFilePath(String path);
    String getFilePath();
    String getOutputFilePath();

    //压缩时间
}
