package org.urbcomp.startdb.compress.elf.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {
    private Workbook workbook;
    private Sheet sheet;
    private int currentRow = 0; // 当前正在读取的行
    private int blockSize=1024;

    public ExcelReader(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(new File(filePath));
        this.workbook = new XSSFWorkbook(fis);
        this.sheet = workbook.getSheetAt(0);
    }
    public ExcelReader(String filePath, int blockSize) throws IOException {
        this.blockSize = blockSize;
        FileInputStream fis = new FileInputStream(new File(filePath));
        this.workbook = new XSSFWorkbook(fis);
        this.sheet = workbook.getSheetAt(0);
    }
    public int getColCount() throws IOException {
        if(sheet.getRow(0)!=null){
            return sheet.getRow(0).getLastCellNum();
        }
        return 0;
    }
    public double[] nextBlock()  throws IOException{//逐行读取EXCEL，满足block的SIZE的时候，就返回
        List<Double> valuesList = new ArrayList<>();
        int currentBlockCount = 0; // 当前块中已读取的数值数量
        while(currentRow<=sheet.getLastRowNum()){
             Row row = sheet.getRow(currentRow);
             if(row==null){
                 continue;
             }
            for (Cell cell : row) {//必然是读取整数行的，当读某一行超过了blocksize，也应读完此行再返回
                if (cell.getCellType() == CellType.NUMERIC) {
                    valuesList.add(cell.getNumericCellValue());
                }
                currentBlockCount++;
            }
            if(currentBlockCount>=blockSize){
                 break;
            }
        }
        System.out.println("这是第"+currentRow+"行，DEBUG读取到数据"+currentBlockCount+"个");
        currentRow++;
        if (!valuesList.isEmpty()) {
            return valuesList.stream().mapToDouble(Double::doubleValue).toArray();
        }
        return null;
    }
    public void readData(String filePath) throws IOException {
        for(Row row:sheet){
            for(Cell cell:row){
                switch (cell.getCellType()){
                    case NUMERIC:
                        double data = cell.getNumericCellValue();
                        System.out.println(data+"\t");
                        break;
                    case STRING:
                        String str = cell.getStringCellValue();
                        System.out.println(str+"\t");
                        break;
                    default:
                        System.out.println("error");
                }
            }
            System.out.println();
        }
    }
}
