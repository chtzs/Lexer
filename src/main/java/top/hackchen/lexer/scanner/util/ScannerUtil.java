package top.hackchen.lexer.scanner.util;

import top.hackchen.lexer.reader.DoubleBufferReader;

import java.io.IOException;

/**
 * @author 陈浩天
 * @date 2021/10/14 16:44 星期四
 */
public class ScannerUtil {
    private final DoubleBufferReader reader;

    public ScannerUtil(DoubleBufferReader reader) {
        this.reader = reader;
    }

    public void ignoreWhitespace() throws IOException {
        while (isWhitespace(reader.peek())) {
            reader.read();
        }
    }

    public void nextLine() throws IOException {
        while (reader.peek() != '\n') {
            reader.read();
        }
        reader.read();
    }

    public boolean isComment() throws IOException{
        return reader.peek() == '/' && reader.peek(1) == '/';
    }

    public static boolean isW(int c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    public static boolean isWhitespace(int c) {
        return Character.isWhitespace(c);
    }

    public static boolean isDigit(int c) {
        return Character.isDigit(c);
    }
}
