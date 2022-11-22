package org.urbcomp.startdb.compress.elf;

import com.github.kutschkem.fpc.FpcCompressor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.io.compress.brotli.BrotliCodec;
import org.apache.hadoop.hbase.io.compress.lz4.Lz4Codec;
import org.apache.hadoop.hbase.io.compress.xerial.SnappyCodec;
import org.apache.hadoop.hbase.io.compress.xz.LzmaCodec;
import org.apache.hadoop.hbase.io.compress.zstd.ZstdCodec;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.junit.jupiter.api.Test;
import org.urbcomp.startdb.compress.elf.compressor.*;
import org.urbcomp.startdb.compress.elf.decompressor.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCompressor {
    private static final String FILE_PATH = "src/test/resources/ElfTestData";
    private static final String[] FILENAMES = {
            "/Air_pressure.csv",
            "/Air-sensor.csv",
            "/Basel_Temperature.csv",
            "/Basel_Wind_Speed.csv",
            "/Bird-migration.csv",
            "/Bitcoin-price.csv",
            "/Blockchain-tr.csv",
            "/City-temp.csv",
            "/worldcities_latitude.csv",
            "/worldcities_longitude.csv",
            "/Dew-point-temp .csv",
            "/electric_vehicle_charging.csv",
            "/FoodPrices.csv",
            "/IR-bio-temp.csv",
            "/PM10-dust.csv",
            "/SSD-bench.csv",
            "/Stocks_DE.csv",
            "/Stocks_UK.csv",
            "/Stocks_USA.csv",
            "/Wind-dir.csv"
    };
    private static final String STORE_PATH = "src/test/resources/result";

    private static double TIME_PRECISION = 1000.0;
    List<Map<String, ResultStructure>> allResult = new ArrayList<>();

    @Test
    public void testCompressor() throws IOException {
        for (String filename : FILENAMES) {
            Map<String, List<ResultStructure>> result = new HashMap<>();
            System.out.println(filename);
            for (int i = 0; i < 100; i++) {
                testELFCompressor(filename, result);
                testFPC(filename, result);
                testSnappy(filename, result);
                testZstd(filename, result);
                testLZ4(filename, result);
                testBrotli(filename, result);
                testXz(filename, result);
            }
            for (Map.Entry<String, List<ResultStructure>> kv : result.entrySet()) {
                Map<String, ResultStructure> r = new HashMap<>();
                r.put(kv.getKey(),computeAvg(kv.getValue()));
                allResult.add(r);
            }
        }
        storeResult(STORE_PATH + "/result.dat");
    }


    public void testELFCompressor(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws FileNotFoundException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);
        ICompressor[] compressorList = new ICompressor[]{
                new GorillaCompressorOS(),
                new ElfOnGorillaCompressorOS(),
                new ChimpCompressor(),
                new ElfOnChimpCompressor(),
                new ChimpNCompressor(128),
                new ElfOnChimpNCompressor(128),
                new ElfCompressor(),
        };
        float totalBlocks = 0;
        long[] totalSize = new long[compressorList.length];
        double[] values;

        HashMap<String, List<Double>> totalCompressionTime = new HashMap<>();
        HashMap<String, List<Double>> totalDecompressionTime = new HashMap<>();
        while ((values = fileReader.nextBlock()) != null) {
            totalBlocks += 1;
            ICompressor[] compressors = new ICompressor[]{
                    new GorillaCompressorOS(),
                    new ElfOnGorillaCompressorOS(),
                    new ChimpCompressor(),
                    new ElfOnChimpCompressor(),
                    new ChimpNCompressor(128),
                    new ElfOnChimpNCompressor(128),
                    new ElfCompressor(),
            };
            for (int i = 0; i < compressors.length; i++) {
                double encodingDuration = 0;
                double decodingDuration = 0;
                long start = System.nanoTime();
                ICompressor compressor = compressors[i];
                for (double value : values) {
                    compressor.addValue(value);
                }
                compressor.close();

                encodingDuration = System.nanoTime() - start;

                totalSize[i] += compressor.getSize();

                byte[] result = compressor.getBytes();
                IDecompressor[] decompressors = new IDecompressor[]{
                        new GorillaDecompressorOS(result),
                        new ElfOnGorillaDecompressorOS(result),
                        new ChimpDecompressor(result),
                        new ElfOnChimpDecompressor(result),
                        new ChimpNDecompressor(result, 128),
                        new ElfOnChimpNDecompressor(result, 128),
                        new ElfDecompressor(result)
                };

                IDecompressor decompressor = decompressors[i];

                start = System.nanoTime();
                List<Double> uncompressedValues = decompressor.decompress();
                decodingDuration = System.nanoTime() - start;

                for (int j = 0; j < values.length; j++) {
                    assertEquals(values[j], uncompressedValues.get(j), "Value did not match"+compressor.getKey());
                }
                String key = compressor.getKey();
                if (!totalCompressionTime.containsKey(key)) {
                    totalCompressionTime.put(key, new ArrayList<>());
                    totalDecompressionTime.put(key, new ArrayList<>());
                }
                totalCompressionTime.get(key).add(encodingDuration / TIME_PRECISION);
                totalDecompressionTime.get(key).add(decodingDuration / TIME_PRECISION);
            }
        }
        for (int i = 0; i < compressorList.length; i++) {
            String key = compressorList[i].getKey();
            ResultStructure r = new ResultStructure(fileName, key,
                    totalSize[i] / (totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                    totalCompressionTime.get(key),
                    totalDecompressionTime.get(key)
            );
            if (!resultCompressor.containsKey(compressorList[i].getKey())) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }
    }

    public void testFPC(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws FileNotFoundException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();
        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;
            FpcCompressor fpc = new FpcCompressor();

            ByteBuffer buffer = ByteBuffer.allocate(FileReader.DEFAULT_BLOCK_SIZE * 10);
            // Compress
            long start = System.nanoTime();
            fpc.compress(buffer, values);
            encodingDuration += System.nanoTime() - start;

            totalSize += buffer.position() * 8;
            totalBlocks += 1;

            buffer.flip();

            FpcCompressor decompressor = new FpcCompressor();

            double[] dest = new double[FileReader.DEFAULT_BLOCK_SIZE];
            start = System.nanoTime();
            decompressor.decompress(buffer, dest);
            decodingDuration += System.nanoTime() - start;
            assertArrayEquals(dest, values);
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        String key = "FPC";
        ResultStructure r = new ResultStructure(fileName, key,
                totalSize / (totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                totalCompressionTime,
                totalDecompressionTime
        );
        if (!resultCompressor.containsKey(key)) {
            resultCompressor.put(key, new ArrayList<>());
        }
        resultCompressor.get(key).add(r);
    }

    public void testSnappy(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();
        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;
            ByteBuffer bb = ByteBuffer.allocate(values.length * 8);
            for (double d : values) {
                bb.putDouble(d);
            }
            byte[] input = bb.array();

            Configuration conf = HBaseConfiguration.create();
            // ZStandard levels range from 1 to 22.
            // Level 22 might take up to a minute to complete. 3 is the Hadoop default, and will be fast.
            conf.setInt(CommonConfigurationKeys.IO_COMPRESSION_CODEC_ZSTD_LEVEL_KEY, 3);
            SnappyCodec codec = new SnappyCodec();
            codec.setConf(conf);

            // Compress
            long start = System.nanoTime();
            org.apache.hadoop.io.compress.Compressor compressor = codec.createCompressor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressionOutputStream out = codec.createOutputStream(baos, compressor);
            out.write(input);
            out.close();
            encodingDuration += System.nanoTime() - start;
            final byte[] compressed = baos.toByteArray();
            totalSize += compressed.length * 8;
            totalBlocks++;

            final byte[] plain = new byte[input.length];
            org.apache.hadoop.io.compress.Decompressor decompressor = codec.createDecompressor();
            start = System.nanoTime();
            CompressionInputStream in = codec.createInputStream(new ByteArrayInputStream(compressed), decompressor);
            IOUtils.readFully(in, plain, 0, plain.length);
            in.close();
            double[] uncompressed = toDoubleArray(plain);
            decodingDuration += System.nanoTime() - start;
            // Decompressed bytes should equal the original
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], uncompressed[i], "Value did not match");
            }
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        String key = "Snappy";
        ResultStructure r = new ResultStructure(fileName, key,
                totalSize / (totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                totalCompressionTime,
                totalDecompressionTime
        );
        if (!resultCompressor.containsKey(key)) {
            resultCompressor.put(key, new ArrayList<>());
        }
        resultCompressor.get(key).add(r);
    }

    public void testZstd(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();
        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;
            ByteBuffer bb = ByteBuffer.allocate(values.length * 8);
            for (double d : values) {
                bb.putDouble(d);
            }
            byte[] input = bb.array();

            Configuration conf = HBaseConfiguration.create();
            // ZStandard levels range from 1 to 22.
            // Level 22 might take up to a minute to complete. 3 is the Hadoop default, and will be fast.
            conf.setInt(CommonConfigurationKeys.IO_COMPRESSION_CODEC_ZSTD_LEVEL_KEY, 3);
            ZstdCodec codec = new ZstdCodec();
            codec.setConf(conf);

            // Compress
            long start = System.nanoTime();
            org.apache.hadoop.io.compress.Compressor compressor = codec.createCompressor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressionOutputStream out = codec.createOutputStream(baos, compressor);
            out.write(input);
            out.close();
            encodingDuration += System.nanoTime() - start;
            final byte[] compressed = baos.toByteArray();
            totalSize += compressed.length * 8;
            totalBlocks++;

            final byte[] plain = new byte[input.length];
            org.apache.hadoop.io.compress.Decompressor decompressor = codec.createDecompressor();
            start = System.nanoTime();
            CompressionInputStream in = codec.createInputStream(new ByteArrayInputStream(compressed), decompressor);
            IOUtils.readFully(in, plain, 0, plain.length);
            in.close();
            double[] uncompressed = toDoubleArray(plain);
            decodingDuration += System.nanoTime() - start;
            // Decompressed bytes should equal the original
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], uncompressed[i], "Value did not match");
            }
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        String key = "Zstd";
        ResultStructure r = new ResultStructure(fileName, key,
                totalSize / (totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                totalCompressionTime,
                totalDecompressionTime
        );
        if (!resultCompressor.containsKey(key)) {
            resultCompressor.put(key, new ArrayList<>());
        }
        resultCompressor.get(key).add(r);
    }

    public void testLZ4(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();
        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;
            ByteBuffer bb = ByteBuffer.allocate(values.length * 8);
            for (double d : values) {
                bb.putDouble(d);
            }
            byte[] input = bb.array();

            Lz4Codec codec = new Lz4Codec();

            // Compress
            long start = System.nanoTime();
            org.apache.hadoop.io.compress.Compressor compressor = codec.createCompressor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressionOutputStream out = codec.createOutputStream(baos, compressor);
            out.write(input);
            out.close();
            encodingDuration += System.nanoTime() - start;
            final byte[] compressed = baos.toByteArray();
            totalSize += compressed.length * 8;
            totalBlocks++;

            final byte[] plain = new byte[input.length];
            org.apache.hadoop.io.compress.Decompressor decompressor = codec.createDecompressor();
            start = System.nanoTime();
            CompressionInputStream in = codec.createInputStream(new ByteArrayInputStream(compressed), decompressor);
            IOUtils.readFully(in, plain, 0, plain.length);
            in.close();
            double[] uncompressed = toDoubleArray(plain);
            decodingDuration += System.nanoTime() - start;
            // Decompressed bytes should equal the original
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], uncompressed[i], "Value did not match");
            }
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        String key = "LZ4";
        ResultStructure r = new ResultStructure(fileName, key,
                totalSize / (totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                totalCompressionTime,
                totalDecompressionTime
        );
        if (!resultCompressor.containsKey(key)) {
            resultCompressor.put(key, new ArrayList<>());
        }
        resultCompressor.get(key).add(r);
    }

    public void testBrotli(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();
        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;
            ByteBuffer bb = ByteBuffer.allocate(values.length * 8);
            for (double d : values) {
                bb.putDouble(d);
            }
            byte[] input = bb.array();

            BrotliCodec codec = new BrotliCodec();

            // Compress
            long start = System.nanoTime();
            org.apache.hadoop.io.compress.Compressor compressor = codec.createCompressor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressionOutputStream out = codec.createOutputStream(baos, compressor);
            out.write(input);
            out.close();
            encodingDuration += System.nanoTime() - start;
            final byte[] compressed = baos.toByteArray();
            totalSize += compressed.length * 8;
            totalBlocks++;

            final byte[] plain = new byte[input.length];
            org.apache.hadoop.io.compress.Decompressor decompressor = codec.createDecompressor();
            start = System.nanoTime();
            CompressionInputStream in = codec.createInputStream(new ByteArrayInputStream(compressed), decompressor);
            IOUtils.readFully(in, plain, 0, plain.length);
            in.close();
            double[] uncompressed = toDoubleArray(plain);
            decodingDuration += System.nanoTime() - start;
            // Decompressed bytes should equal the original
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], uncompressed[i], "Value did not match");
            }
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        String key = "Brotli";
        ResultStructure r = new ResultStructure(fileName, key,
                totalSize / (totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                totalCompressionTime,
                totalDecompressionTime
        );
        if (!resultCompressor.containsKey(key)) {
            resultCompressor.put(key, new ArrayList<>());
        }
        resultCompressor.get(key).add(r);
    }

    public void testXz(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();
        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;
            ByteBuffer bb = ByteBuffer.allocate(values.length * 8);
            for (double d : values) {
                bb.putDouble(d);
            }
            byte[] input = bb.array();

            Configuration conf = new Configuration();
            // LZMA levels range from 1 to 9.
            // Level 9 might take several minutes to complete. 3 is our default. 1 will be fast.
            conf.setInt(LzmaCodec.LZMA_LEVEL_KEY, 3);
            LzmaCodec codec = new LzmaCodec();
            codec.setConf(conf);

            // Compress
            long start = System.nanoTime();
            org.apache.hadoop.io.compress.Compressor compressor = codec.createCompressor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressionOutputStream out = codec.createOutputStream(baos, compressor);
            out.write(input);
            out.close();
            encodingDuration += System.nanoTime() - start;
            final byte[] compressed = baos.toByteArray();
            totalSize += compressed.length * 8;
            totalBlocks++;

            final byte[] plain = new byte[input.length];
            org.apache.hadoop.io.compress.Decompressor decompressor = codec.createDecompressor();
            start = System.nanoTime();
            CompressionInputStream in = codec.createInputStream(new ByteArrayInputStream(compressed), decompressor);
            IOUtils.readFully(in, plain, 0, plain.length);
            in.close();
            double[] uncompressed = toDoubleArray(plain);
            decodingDuration += System.nanoTime() - start;
            // Decompressed bytes should equal the original
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], uncompressed[i], "Value did not match");
            }
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        String key = "Xz";
        ResultStructure r = new ResultStructure(fileName, key,
                totalSize / (totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                totalCompressionTime,
                totalDecompressionTime
        );
        if (!resultCompressor.containsKey(key)) {
            resultCompressor.put(key, new ArrayList<>());
        }
        resultCompressor.get(key).add(r);
    }


    public void storeResult(String filePath) throws IOException {
        FileWriter fileWriter = new FileWriter(filePath);
        for (Map<String, ResultStructure> result : allResult) {
            for (ResultStructure ls : result.values()) {
                fileWriter.write(ls.toString());
            }
        }
        fileWriter.close();
    }

    public ResultStructure computeAvg(List<ResultStructure> lr) {
        int num = lr.size();
        double compressionTime = 0;
        double maxCompressTime = 0;
        double minCompressTime = 0;
        double mediaCompressTime = 0;
        double decompressionTime = 0;
        double maxDecompressTime = 0;
        double minDecompressTime = 0;
        double mediaDecompressTime = 0;
        for (ResultStructure resultStructure : lr) {
            compressionTime += resultStructure.getCompressionTime();
            maxCompressTime += resultStructure.getMaxCompressTime();
            minCompressTime += resultStructure.getMinCompressTime();
            mediaCompressTime += resultStructure.getMediaCompressTime();
            decompressionTime += resultStructure.getDecompressionTime();
            maxDecompressTime += resultStructure.getMaxDecompressTime();
            minDecompressTime += resultStructure.getMinDecompressTime();
            mediaDecompressTime += resultStructure.getMediaDecompressTime();
        }
        return new ResultStructure(lr.get(0).getFilename(),
                lr.get(0).getCompressorName(),
                lr.get(0).getCompressorRatio(),
                compressionTime / num,
                maxCompressTime / num,
                minCompressTime / num,
                mediaCompressTime / num,
                decompressionTime / num,
                maxDecompressTime / num,
                minDecompressTime / num,
                mediaDecompressTime / num
        );
    }

    public static double[] toDoubleArray(byte[] byteArray) {
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[byteArray.length / times];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = ByteBuffer.wrap(byteArray, i * times, times).getDouble();
        }
        return doubles;
    }
}