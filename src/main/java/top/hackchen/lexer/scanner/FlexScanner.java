package top.hackchen.lexer.scanner;

import top.hackchen.lexer.reader.DoubleBufferReader;

import java.io.IOException;

/**
 * @author 陈浩天
 * @date 2021/10/10 16:11 星期日
 */
public class FlexScanner implements AutoCloseable {
    private final HeadScanner headScanner;
    private final DefinitionScanner definitionScanner;
    private final CodeScanner codeScanner;
    private final DoubleBufferReader reader;

    public FlexScanner(String path) throws IOException {
        reader = new DoubleBufferReader(path);
        headScanner = new HeadScanner(reader);
        definitionScanner = new DefinitionScanner(reader);
        codeScanner = new CodeScanner(reader, definitionScanner.getIdMap());
    }

    public void scan() throws IOException {
        headScanner.scan();
        definitionScanner.scan();
        codeScanner.scan();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    public void printTestInfo(){
        definitionScanner.getIdMap().forEach((key, value) -> System.out.println(key + " -> " + value));
        codeScanner.getPatternAndCodes().forEach(System.out::println);
    }

    public HeadScanner getHeadScanner() {
        return headScanner;
    }

    public DefinitionScanner getDefinitionScanner() {
        return definitionScanner;
    }

    public CodeScanner getCodeScanner() {
        return codeScanner;
    }
}
