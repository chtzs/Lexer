package top.hackchen.lexer.generator;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author 陈浩天
 * @date 2021/10/17 15:06 星期日
 */
public class LexerGeneratorTest {

    @Test
    public void printToFile() throws IOException {
        LexerGenerator generator = new LexerGenerator("D:\\Java-Projects\\LexerTest\\test.flex");
        generator.printToFile("D:\\Java-Projects\\LexerTest\\Test.Java");
    }
}