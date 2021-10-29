package top.hackchen.lexer.scanner;

import top.hackchen.lexer.reader.DoubleBufferReader;

import java.io.IOException;
import java.util.HashMap;

import static top.hackchen.lexer.scanner.util.ScannerUtil.isW;

/**
 * @author 陈浩天
 * @date 2021/10/14 16:41 星期四
 */
public class RegexScanner extends AbstractScanner {
    private final HashMap<String, String> idMap;
    private String regex;
    private char end = '\n';

    public RegexScanner(DoubleBufferReader reader, HashMap<String, String> idMap) {
        super(reader);
        this.idMap = idMap;
    }

    public void setEnd(char end) {
        this.end = end;
    }

    @Override
    public void scan() throws IOException {
        /* ************************解析正则************************ */
        int c;

        StringBuilder regexBuilder = new StringBuilder();
        while ((c = reader.read()) != -1) {
            if (c == '\\') {
                //跳过下一个字符
                regexBuilder.append('\\');
                regexBuilder.append((char) reader.peek());
                reader.read();
                continue;
            } else if (c == '\r') {
                continue;
            } else if (c == '"') {
                //转义""之间的字符
                while (reader.peek() != -1 && reader.peek() != '"') {
                    regexBuilder.append('\\');
                    regexBuilder.append((char) reader.read());
                }
                reader.read();
                continue;
            } else if (c == end) {
                break;
            }

            scanReference(c, regexBuilder);
        }
        regex = regexBuilder.toString();
    }

    private void scanReference(int c, StringBuilder regexBuilder) throws IOException {
        if (c == '{' && reader.peek() == '{') {
            reader.read();
            util.ignoreWhitespace();
            StringBuilder idRefBuilder = new StringBuilder();
            while (isW(reader.peek())) {
                idRefBuilder.append((char) reader.read());
            }
            util.ignoreWhitespace();
            if (reader.peek() != '}' || reader.peek(1) != '}') {
                throw new RuntimeException("Shit, 引用模式没有正确关闭！");
            }
            reader.read();
            reader.read();
            String idRef = idRefBuilder.toString();
            //在已存标识符和正则的对照表中找到标识符的引用
            if (idMap.containsKey(idRef)) {
                //注意：这里不能直接替换，而是需要包上一层括号！
                //举个例子：假设定义单词=ABC，那么{{单词}}*应该是(ABC)*而不是ABC*！
                regexBuilder.append('(');
                regexBuilder.append(idMap.get(idRef));
                regexBuilder.append(')');
            } else {
                throw new RuntimeException("引用的模式" + idRef + "不存在！");
            }
        } else {
            regexBuilder.append((char) c);
        }
    }

    public String getRegex() {
        return regex;
    }
}
