import top.hackchen.lexer.generator.LexerGenerator;

import java.io.IOException;

/**
 * @author 陈浩天
 * @date 2021/10/29 21:31 星期五
 */
public class Main {
    public static void main(String[] args) throws IOException {
        LexerGenerator lexerGenerator = new LexerGenerator("Example\\src\\TestParser.flex");
        lexerGenerator.printToFile("Example\\src\\TestParser.java");
    }
}
