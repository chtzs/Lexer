package top.hackchen.lexer.scanner;

import org.junit.Test;

import java.io.IOException;

/**
 * @author 陈浩天
 * @date 2021/10/10 21:10 星期日
 */
public class FlexScannerTest {

    @Test
    public void flex() throws IOException {
        FlexScanner scanner = new FlexScanner("D:\\Java-Projects\\test.flex");
        scanner.scan();
        scanner.printTestInfo();
    }
}