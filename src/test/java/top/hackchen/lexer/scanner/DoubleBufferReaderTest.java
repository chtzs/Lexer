package top.hackchen.lexer.scanner;

import org.junit.Test;
import top.hackchen.lexer.runtime.reader.DoubleBufferReader;

import java.io.FileReader;
import java.io.IOException;

/**
 * @author 陈浩天
 * @date 2021/10/13 19:58 星期三
 */
public class DoubleBufferReaderTest {

    @Test
    public void read() throws IOException {
        try (DoubleBufferReader doubleBufferReader = new DoubleBufferReader("D:\\Java-Projects\\test.flex")) {
            int c;
            while ((c = doubleBufferReader.read()) != -1) {
                System.out.print((char) c);
            }
        }
    }

    @Test
    public void rawRead() throws IOException {
        try (FileReader fileReader = new FileReader("D:\\Java-Projects\\test.flex")) {
            char[] buffer = new char[1024];
            int len = 0;
            while ((len = fileReader.read(buffer, 0, 1024)) != -1) {
                System.out.print(buffer);
            }
        }
    }
}