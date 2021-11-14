package top.hackchen.lexer.scanner;

import top.hackchen.lexer.runtime.reader.DoubleBufferReader;
import top.hackchen.lexer.scanner.util.ScannerUtil;

/**
 * @author 陈浩天
 * @date 2021/10/14 11:43 星期四
 */
public abstract class AbstractScanner {
    protected DoubleBufferReader reader;
    protected ScannerUtil util;

    public AbstractScanner(DoubleBufferReader reader) {
        this.reader = reader;
        this.util = new ScannerUtil(reader);
    }

    public abstract void scan() throws Exception;
}
