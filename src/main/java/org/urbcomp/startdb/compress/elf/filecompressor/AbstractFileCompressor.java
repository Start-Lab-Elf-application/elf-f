package org.urbcomp.startdb.compress.elf.filecompressor;

import java.io.IOException;
import java.util.HashMap;

public abstract class AbstractFileCompressor implements IFileCompressor{
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

    public abstract void compress() throws IOException;
}
