package top.hackchen.lexer.scanner;

import top.hackchen.lexer.reader.DoubleBufferReader;

import java.io.IOException;

/**
 * @author 陈浩天
 * @date 2021/10/31 23:11 星期日
 */
public class InnerCodeScanner extends AbstractScanner {
    private String innerCode = "";

    public InnerCodeScanner(DoubleBufferReader reader) {
        super(reader);
    }

    @Override
    public void scan() throws IOException {
        if (reader.peek() != '%' || reader.peek(1) != '{') {
            return;
        }
        reader.read();
        reader.read();
        StringBuilder innerCodeBuilder = new StringBuilder();
        while (reader.peek() != -1) {
            //用}%}%代替}%
            if (reader.peek() == '}' && reader.peek(1) == '%'
                    && reader.peek(2) == '}' && reader.peek(3) == '%') {
                reader.read();
                reader.read();
                innerCodeBuilder.append((char) reader.read());
                innerCodeBuilder.append((char) reader.read());
            } else if (reader.peek() == '}' && reader.peek(1) == '%') {
                break;
            } else {
                innerCodeBuilder.append((char) reader.read());
            }
        }
        innerCode = innerCodeBuilder.toString();
        //读掉最后一个}
        reader.read();
        reader.read();
        util.ignoreWhitespace();
    }

    public String getInnerCode() {
        return innerCode;
    }
}
