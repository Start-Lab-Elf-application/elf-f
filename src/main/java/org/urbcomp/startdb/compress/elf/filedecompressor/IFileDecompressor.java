package org.urbcomp.startdb.compress.elf.filedecompressor;

public interface IFileDecompressor {
    void setoutputFilePath(String path);
    void setFilePath(String path);
    String getFilePath();
    String getOutputFilePath();
}
