package top.hackchen.lexer.scanner;

import top.hackchen.lexer.runtime.reader.DoubleBufferReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 陈浩天
 * @date 2021/10/14 11:42 星期四
 */
public class CodeScanner extends AbstractScanner {
    public static class PatternAndCode {
        public String pattern;
        public String code;

        public PatternAndCode(String pattern, String code) {
            this.pattern = pattern;
            this.code = code;
        }

        @Override
        public String toString() {
            return pattern + " : " + code;
        }
    }

    private final RegexScanner regexScanner;
    private final InnerCodeScanner innerCodeScanner;
    private final List<PatternAndCode> patternAndCodes = new ArrayList<>();

    public CodeScanner(DoubleBufferReader reader, HashMap<String, String> idMap) {
        super(reader);
        regexScanner = new RegexScanner(reader, idMap);
        regexScanner.setEnd(' ');
        innerCodeScanner = new InnerCodeScanner(reader);
    }

    /**
     * 读取类似[Regex]:%{[Code]}%的代码
     *
     * @throws IOException 可能会抛出的IO异常
     */
    @Override
    public void scan() throws IOException {
        while (reader.peek() != -1) {
            util.ignoreWhitespace();
            if (util.isComment()) {
                util.nextLine();
            } else {
                //读取正则
                regexScanner.scan();
                String pattern = regexScanner.getRegex();

                util.ignoreWhitespace();

                //读取内嵌代码
                innerCodeScanner.scan();
                String code = '{' + innerCodeScanner.getInnerCode() + '}';

                patternAndCodes.add(new PatternAndCode(pattern, code));
            }
        }
    }

    public List<PatternAndCode> getPatternAndCodes() {
        return patternAndCodes;
    }
}
