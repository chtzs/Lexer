package top.hackchen.lexer.scanner;

import top.hackchen.lexer.option.Options;
import top.hackchen.lexer.runtime.reader.DoubleBufferReader;

import java.io.IOException;
import java.util.HashMap;

import static top.hackchen.lexer.scanner.util.ScannerUtil.*;

/**
 * @author 陈浩天
 * @date 2021/10/14 11:42 星期四
 */
public class DefinitionScanner extends AbstractScanner {
    private final RegexScanner regexScanner;
    private final InnerCodeScanner innerCodeScanner;
    private final Options options = new Options();
    private final HashMap<String, String> idMap = new HashMap<>();
    private String innerCode = "";

    public DefinitionScanner(DoubleBufferReader reader) {
        super(reader);
        regexScanner = new RegexScanner(reader, idMap);
        innerCodeScanner = new InnerCodeScanner(reader);
    }

    @Override
    public void scan() throws IOException {
        while (reader.peek() != -1) {
            util.ignoreWhitespace();
            if (util.isComment()) {
                util.nextLine();
            } else if (reader.peek() == '%') {
                if (reader.peek(1) == '{') {
                    readInnerCode();
                } else if (reader.peek(1) == '%') {
                    reader.read();
                    reader.read();
                    util.ignoreWhitespace();
                    break;
                } else {
                    readOption();
                }
            } else {
                //读完后换到开头
                readDefinition();
            }
        }
    }

    private String nextToken() throws IOException {
        StringBuilder tokenBuilder = new StringBuilder();
        util.ignoreWhitespace();
        while (!isWhitespace(reader.peek())) {
            tokenBuilder.append((char) reader.read());
        }
        return tokenBuilder.toString();
    }

    private void readOption() throws IOException {
        reader.read();
        util.ignoreWhitespace();
        String option = nextToken();
        switch (option) {
            case "public":
                break;
            case "private":
                options.classModifier = "private";
                break;
            case "protected":
                options.classModifier = "protected";
                break;
            case "package-protected":
                options.classModifier = "";
                break;
            case "class":
                options.className = nextToken();
                break;
            case "unicode":
                options.isUnicode = true;
                break;
            case "rollback-optimization":
                options.isRollbackOptimization = true;
                break;
            case "console":
                options.isConsoleMode = true;
                break;
            case "c-parser":
                options.isCParser = true;
                break;
        }
        util.ignoreWhitespace();
    }

    private void readInnerCode() throws IOException {
        innerCodeScanner.scan();
        innerCode = innerCodeScanner.getInnerCode();
    }

    /**
     * 读取形同 [标识符] = [正则] 的词法单元定义，其中[正则]中可以通过嵌套{{标识符}}的方式来引用先前的定义
     *
     * @throws IOException 可能抛出的读取异常
     */
    private void readDefinition() throws IOException {
        String identifier;
        String regex;
        int c;

        /* ************************解析标识符************************ */
        StringBuilder identifierBuilder = new StringBuilder();
        util.ignoreWhitespace();
        c = reader.peek();
        if (isDigit(c)) {
            throw new RuntimeException("Shit, 标识符第一个字母不能为数字！");
        }
        while (isW(reader.peek())) {
            c = reader.read();
            identifierBuilder.append((char) c);
        }
        identifier = identifierBuilder.toString();

        //读取到标识符的后一个字符，尝试忽略空格
        util.ignoreWhitespace();
        //判断空格后面下一个字符是不是等号
        if (reader.read() != '=') {
            throw new RuntimeException("Shit, 这里应该有一个等号的");
        }
        //读取到等号的后一个字符，尝试忽略空格
        util.ignoreWhitespace();

        /* ************************解析正则************************ */
        regexScanner.scan();
        regex = regexScanner.getRegex();
        idMap.put(identifier, regex);
    }

    public HashMap<String, String> getIdMap() {
        return idMap;
    }

    public Options getOptions() {
        return options;
    }

    public String getInnerCode() {
        return innerCode;
    }
}
