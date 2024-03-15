package org.urbcomp.startdb.compress.elf.iotest;

import org.junit.jupiter.api.Test;
import org.urbcomp.startdb.compress.elf.filecompressor.*;

import java.io.*;

public class timetest {
    private static final String FILE_PATH = "src/test/resources/ElfTestData";
    private static final String[] FILENAMES = {
            "/POI-lat.csv",
            "/POI-lon.csv",
    };
    @Test

    public void comTimeTest() throws IOException {
        for (String filename : FILENAMES){
            String compressedFileName = filename.replace(".csv",".elf");
            // DecompressFileOperation fileDecompressor = new DecompressFileOperation(FILE_PATH+compressedFileName,"C:\\Users\\25379\\Documents\\第二学期文件\\测试"+filename);
            //ElfFileCompressor fileCompressor = new ElfFileCompressor(FILE_PATH+filename,"C:\\Users\\25379\\Documents\\第三学期文件\\tttrr"+compressedFileName);
            long startTime = System.nanoTime();
            //fileDecompressor.writeDoubleToCSV();
            //fileCompressor.compress();
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            double totalTimes = Math.round(totalTime/100000.0)/10000.0;
            System.out.println(filename+":"+ totalTimes);
        }
    }
}
