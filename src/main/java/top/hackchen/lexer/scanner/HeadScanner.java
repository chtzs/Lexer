package top.hackchen.lexer.scanner;

import top.hackchen.lexer.runtime.reader.DoubleBufferReader;

import java.io.IOException;

/**
 * @author 陈浩天
 * @date 2021/10/14 11:41 星期四
 */
public class HeadScanner extends AbstractScanner {
    private String head;

    public HeadScanner(DoubleBufferReader reader) {
        super(reader);
    }

    @Override
    public void scan() throws IOException {
        StringBuilder builder = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '%') {
                if (reader.peek() == '%') {
                    util.ignoreWhitespace();
                    break;
                }
            } else {
                builder.append((char) c);
            }
        }
        head = builder.toString();
    }

    public String getHead() {
        return head;
    }
}
