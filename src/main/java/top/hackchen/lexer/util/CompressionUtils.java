package top.hackchen.lexer.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author 陈浩天
 * @date 2021/10/17 11:11 星期日
 */

public class CompressionUtils {
    public static byte[] compress(byte[] data) throws IOException {
        System.out.println("Compressing charset map...");
        Deflater deflater = new Deflater();
        deflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        System.out.println("Original: " + data.length / 1024 + " Kb");
        System.out.println("Compressed: " + output.length / 1024 + " Kb");
        return output;
    }

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();

        System.out.println("Original: " + data.length);
        System.out.println("Compressed: " + output.length);
        return output;
    }

    public static byte[] integersToBytes(int[] values) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(values.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(values);
        return byteBuffer.array();
    }

    public static int[] bytesToIntegers(byte[] values) {
        IntBuffer intBuf =
                ByteBuffer.wrap(values)
                        .order(ByteOrder.BIG_ENDIAN)
                        .asIntBuffer();
        int[] array = new int[intBuf.remaining()];
        intBuf.get(array);
        return array;
    }

    public static int[] compress(int[] values) {
        //b的长度是4的整数倍
        byte[] b = integersToBytes(values);
        try {
            //cb的长度不一定是4的整数倍
            byte[] cb = compress(b);
            //补0
            byte[] re = Arrays.copyOf(cb, cb.length + ((4 - (cb.length % 4)) % 4));
            return bytesToIntegers(re);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Shit");
        }
    }

    public static int[] decompress(int[] values) {
        byte[] cb = integersToBytes(values);
        try {
            byte[] decompress = decompress(cb);
            return bytesToIntegers(decompress);
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
            throw new RuntimeException("Shit");
        }
    }
}