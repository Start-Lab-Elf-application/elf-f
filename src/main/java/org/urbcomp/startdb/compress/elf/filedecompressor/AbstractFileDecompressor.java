package org.urbcomp.startdb.compress.elf.filedecompressor;

import org.urbcomp.startdb.compress.elf.filecompressor.IFileCompressor;

import java.io.IOException;

public abstract class AbstractFileDecompressor implements IFileDecompressor {
    private String filePath = "";
    private String outputFilePath = "";

    @Override
    public void setFilePath(String path) {
        this.filePath = path;
    }

    @Override
    public void setoutputFilePath(String path) {
        this.outputFilePath = path;
    }

    @Override
    public String getFilePath(){
        return this.filePath;
    }

    @Override
    public String getOutputFilePath(){
        return this.outputFilePath;
    }

    protected abstract void decompress() throws IOException;
}
