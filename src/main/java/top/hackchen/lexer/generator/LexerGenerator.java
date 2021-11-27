package top.hackchen.lexer.generator;

import top.hackchen.lexer.option.Options;
import top.hackchen.lexer.regex.RegexCombiner;
import top.hackchen.lexer.regex.dfa.FastDFA;
import top.hackchen.lexer.scanner.CodeScanner;
import top.hackchen.lexer.scanner.FlexScanner;
import top.hackchen.lexer.util.CompressionUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author 陈浩天
 * @date 2021/10/17 10:59 星期日
 */
public class LexerGenerator {
    private static final int IDENT = 4;

    private final int[][] transition;
    private final int beginState;
    private final boolean[] isFinalState;
    private final int[] finalStateType;
    private final int[] charToIndex;
    private final String[] codes;

    private final String head;
    private final String innerCode;
    private final Options options;

    private BufferedWriter out;
    private int ident = 0;

    public LexerGenerator(String flexPath) throws IOException {
        RegexCombiner regexCombiner = new RegexCombiner();
        FlexScanner flexScanner = new FlexScanner(flexPath);
        flexScanner.scan();
        List<CodeScanner.PatternAndCode> patternAndCodes = flexScanner.getCodeScanner().getPatternAndCodes();
        int i = 0;
        codes = new String[patternAndCodes.size()];
        for (CodeScanner.PatternAndCode patternAndCode : patternAndCodes) {
            regexCombiner.add(patternAndCode.pattern, i);
            codes[i] = patternAndCode.code;
            i++;
        }
        FastDFA fastDFA = regexCombiner.apply();
        transition = fastDFA.transition;
        beginState = fastDFA.beginState;
        isFinalState = fastDFA.isFinalState;
        finalStateType = fastDFA.finalStateType;
        charToIndex = regexCombiner.getCharsetMap().charToNumber;

        head = flexScanner.getHeadScanner().getHead();
        innerCode = flexScanner.getDefinitionScanner().getInnerCode();
        options = flexScanner.getDefinitionScanner().getOptions();
    }

    public Options getOptions() {
        return options;
    }

    public void printToFile(String filePath) throws IOException {
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));
        printHead();
        printClass();
        out.flush();
    }

    private void printHead() {
        printlnWithIdent("//Generated automatically by C-Flex. Do not modify.");
        printlnWithIdent(head);
        printlnWithIdent("import java.io.*;\n" +
                "import java.nio.ByteBuffer;\n" +
                "import java.nio.ByteOrder;\n" +
                "import java.nio.IntBuffer;\n" +
                "import java.nio.charset.StandardCharsets;\n" +
                "import java.util.zip.DataFormatException;\n" +
                "import java.util.zip.Inflater;\n");
        if (options.isCParser) {
            printlnWithIdent("import top.hackchen.parser.runtime.*;");
            printlnWithIdent("import top.hackchen.parser.runtime.symbol.*;");
        }
        printlnWithIdent("");
    }

    private void printClass() {
        if (options.classModifier.equals("")) {
            print("class ");
        } else {
            print(options.classModifier + " class ");
        }
        print(options.className);
        printlnWithIdent(" {");
        increaseIdent();
        decreaseIndent();
        printInnerCode();
        increaseIdent();
        printVariable();
        printConstructor();
        printCParserFunc();
        printDecompressFunc();
        printDoActionFunc();
        printDisableAdvanceFunc();
        printLexFunc();
        printLexWordFunc();
        printMain();
        decreaseIndent();
        printlnWithIdent("}");
    }

    private void printVariable() {
        if (options.isCParser) {
            printlnWithIdent("public static final List<Symbol> _cParserTokenList = new ArrayList<>();\n");
        }
        printlnWithIdent("private final int beginState = " + beginState + ";");
        printlnWithIdent("private final DoubleBufferReader reader;");
        printlnWithIdent("private int beginPos = 0;");
        printlnWithIdent("private int state = beginState;\n");
        printlnWithIdent("private int preSuccessType = -1;\n");
        printlnWithIdent("private int preSuccessOffset = -1;\n");
        printlnWithIdent("private int offset = 0;\n");
        printIdent();
        print("private final int[][] transition = ");
        printArray(transition);
        print(";\n");
        printIdent();
        print("private final boolean[] isFinalState = ");
        printArray(isFinalState);
        print(";\n");
        printIdent();
        print("private final int[] finalStateType = ");
        printArray(finalStateType);
        print(";\n");
        printIdent();
        print("private int[] charToIndex = ");
        printArray(CompressionUtils.compress(charToIndex));
        print(";\n");
        printlnWithIdent("");
        printLexVariable();
    }

    private void printInnerCode() {
        printlnWithIdent(innerCode);
    }

    private void printConstructor() {
        printlnWithIdent((options.classModifier.equals("") ? "" : options.classModifier + " ") + options.className + "(DoubleBufferReader reader) throws IOException, DataFormatException{\n" +
                "    this.reader = reader;\n" +
                "    charToIndex = decompress(charToIndex);\n");
        if (options.isRollbackOptimization) {
            printlnWithIdent("    //回溯优化相关\n" +
                    "    int _bufferMaxSize = reader.bufferMaxSize;\n" +
                    "    _failed = new HashSet[transition.length];\n" +
                    "    for (int i = 0; i < transition.length; i++) {\n" +
                    "        _failed[i] = new HashSet<>();\n" +
                    "    }\n" +
                    "    _offsetStack = new int[_bufferMaxSize];\n" +
                    "    _stateStack = new int[_bufferMaxSize];\n");
        }
        printlnWithIdent("}");
    }

    private void printDecompressFunc() {
        printlnWithIdent("");
        printlnWithIdent("public static byte[] decompress(byte[] data) throws IOException, DataFormatException {\n" +
                "    Inflater inflater = new Inflater();\n" +
                "    inflater.setInput(data);\n" +
                "\n" +
                "    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);\n" +
                "    byte[] buffer = new byte[1024];\n" +
                "    while (!inflater.finished()) {\n" +
                "        int count = inflater.inflate(buffer);\n" +
                "        outputStream.write(buffer, 0, count);\n" +
                "    }\n" +
                "    outputStream.close();\n" +
                "\n" +
                "    return outputStream.toByteArray();\n" +
                "}\n");
        printlnWithIdent("");
        printlnWithIdent("public static int[] decompress(int[] values) throws IOException, DataFormatException {\n" +
                "    byte[] cb = integersToBytes(values);\n" +
                "    byte[] decompress = decompress(cb);\n" +
                "    return bytesToIntegers(decompress);\n" +
                "}\n");
        printlnWithIdent("");
        printlnWithIdent("public static byte[] integersToBytes(int[] values) {\n" +
                "    ByteBuffer byteBuffer = ByteBuffer.allocate(values.length * 4);\n" +
                "    IntBuffer intBuffer = byteBuffer.asIntBuffer();\n" +
                "    intBuffer.put(values);\n" +
                "    return byteBuffer.array();\n" +
                "}\n" +
                "\n" +
                "public static int[] bytesToIntegers(byte[] values) {\n" +
                "    IntBuffer intBuf =\n" +
                "            ByteBuffer.wrap(values)\n" +
                "                    .order(ByteOrder.BIG_ENDIAN)\n" +
                "                    .asIntBuffer();\n" +
                "    int[] array = new int[intBuf.remaining()];\n" +
                "    intBuf.get(array);\n" +
                "    return array;\n" +
                "}");
    }

    private void printCParserFunc() {
        if (!options.isCParser) return;
        printlnWithIdent("");
        printlnWithIdent("public Symbol createWith(String name) {\n" +
                "    Symbol symbol = new Symbol(name);\n" +
                "    symbol.pos = reader.vPos;\n" +
                "    symbol.line = reader.line;\n" +
                "    return symbol;\n" +
                "}\n" +
                "\n" +
                "public Symbol createWithInt(String name, int value) {\n" +
                "    Symbol symbol = createWith(name);\n" +
                "    symbol.intValue = value;\n" +
                "    return symbol;\n" +
                "}\n" +
                "\n" +
                "public Symbol createWithLong(String name, long value) {\n" +
                "    Symbol symbol = createWith(name);\n" +
                "    symbol.longValue = value;\n" +
                "    return symbol;\n" +
                "}\n" +
                "\n" +
                "public Symbol createWithDouble(String name, double value) {\n" +
                "    Symbol symbol = createWith(name);\n" +
                "    symbol.doubleValue = value;\n" +
                "    return symbol;\n" +
                "}\n" +
                "\n" +
                "public Symbol createWithFloat(String name, float value) {\n" +
                "    Symbol symbol = createWith(name);\n" +
                "    symbol.floatValue = value;\n" +
                "    return symbol;\n" +
                "}\n" +
                "\n" +
                "public Symbol createWithString(String name, long value) {\n" +
                "    Symbol symbol = createWith(name);\n" +
                "    symbol.stringValue = value;\n" +
                "    return symbol;\n" +
                "}\n" +
                "\n" +
                "public Symbol createWithValue(String name, Object value) {\n" +
                "    Symbol symbol = createWith(name);\n" +
                "    symbol.value = value;\n" +
                "    return symbol;\n" +
                "}");
        printlnWithIdent("");
    }

    private void printDisableAdvanceFunc() {
        printlnWithIdent("public void disableAdvance() throws IOException {\n" +
                "    preSuccessOffset = -1;\n" +
                "}\n\n");
    }

    private void printLexVariable() {
        if (options.isRollbackOptimization) {
            printlnWithIdent("//回溯相关\n" +
                    "//失败数组，只要是_failed[状态编号]存在[当前输入]的，必定不能走到终状态。\n" +
                    "Set<Integer>[] _failed;\n" +
                    "//读取指针偏移量栈，用来记录出错回滚位置\n" +
                    "int[] _offsetStack;\n" +
                    "//状态栈，用来记录经过的状态\n" +
                    "int[] _stateStack;\n" +
                    "//栈顶指针\n" +
                    "int _sp = 0;");
        }
    }

    private void printLexFunc() {
        printlnWithIdent("");
        printlnWithIdent("public boolean lex() throws IOException {\n" +
                "    int c;\n\n");

        printlnWithIdent("    while((c = reader.peek(offset)) != -1) {\n" +
                "        int condition = charToIndex[c];\n" +
                "        if (condition < 0) {\n" +
                "            throw new RuntimeException(\"词法分析遇到错误: 字符\" + c + \"在规约中未定义！\");\n" +
                "        }\n" +
                "        state = transition[state][condition];\n" +
                "        //死状态，要尝试回溯\n" +
                "        if (state == -1" + (options.isRollbackOptimization ? " || _failed[state].contains(reader.realPos)" : "") +
                ") {\n" +
                "            if (preSuccessType == -1) {\n" +
                "                String msg = \"Token\" + lexWord() + \"不存在！\";\n" +
                "                throw new RuntimeException(\"词法分析遇到错误: \" + msg);\n" +
                "            } else {");
        if (options.isRollbackOptimization) {
            printlnWithIdent("                    //这时候栈中保存的都是不可到达终态的<状态，条件>组合\n" +
                    "                    while (_sp > 0) {\n" +
                    "                        _failed[_stateStack[_sp - 1]].add(_offsetStack[_sp - 1]);\n" +
                    "                        _sp--;\n" +
                    "                    }");
        }
        printlnWithIdent("                //回溯并输出\n" +
                "                try {\n" +
                "                    doAction(preSuccessType);\n" +
                "                } catch (Exception e) {\n" +
                "                    throw new RuntimeException(e);\n" +
                "                }\n" +
                "                \n" +
                "                reader.movePosTo(preSuccessOffset + 1);\n" +
                "                preSuccessType = -1;\n" +
                "                preSuccessOffset = -1;\n" +
                "                offset = 0;\n" +
                "                state = beginState;\n" +
                "                return true;\n" +
                "            }\n" +
                "        } else {");
        if (options.isRollbackOptimization) {
            printlnWithIdent("            //偏移量入栈\n" +
                    "            _offsetStack[_sp] = reader.realPos + offset;\n" +
                    "            //状态入栈\n" +
                    "            _stateStack[_sp] = state;\n" +
                    "            _sp++;");
        }
        printlnWithIdent("            if (isFinalState[state]) {\n" +
                "                preSuccessType = finalStateType[state];\n" +
                "                preSuccessOffset = offset;");
        if (options.isRollbackOptimization) {
            printlnWithIdent("                //一旦成功，立刻清空两个栈，确保栈中只有不可到达终态的<状态，条件>组合\n" +
                    "                _sp = 0;");
        }
        printlnWithIdent("        }\n" +
                "        }\n" +
                "        offset++;\n" +
                "    }\n" +
                "    if (preSuccessType != -1) {\n" +
                "        try {"+
                "            doAction(preSuccessType);\n" +
                "            preSuccessType = -1;\n" +
                "        } catch (Exception e) {\n" +
                "            throw new RuntimeException(e);\n" +
                "        }\n" +
                "        return true;\n" +
                "    }\n" +
                "    return false;\n" +
                "}\n");
    }

    private void printLexWordFunc() {
        printlnWithIdent("");
        printlnWithIdent("private String lexWord() {\n" +
                "    char[] wordChar = new char[offset];\n" +
                "    for (int i = 0; i < offset; i++) {\n" +
                "        wordChar[i] = (char) reader.peek(i);\n" +
                "    }\n" +
                "    return new String(wordChar);\n" +
                "}");
    }

    private void printDoActionFunc() {
        printlnWithIdent("");
        printlnWithIdent("private void doAction(int type) throws Exception {\n" +
                "    switch (type) {\n");
        for (int i = 0; i < codes.length; i++) {
            printlnWithIdent("        case " + i + ":");
            print("            ");
            printlnWithIdent(codes[i]);
            print("            ");
            printlnWithIdent("break;");
        }
        printlnWithIdent("    }\n" +
                "}");
    }

    private void printMain() {
        printlnWithIdent("");
        printlnWithIdent("public static void main(String[] argv) {\n" +
                "    if (argv.length == 0) {\n");
        if (options.isConsoleMode) {
            printlnWithIdent("        try {\n" +
                    "            DoubleBufferReader reader = new DoubleBufferReader(System.in);\n" +
                    "            " + options.className + " scanner = new " + options.className + "(reader);\n" +
                    "            scanner.lex();\n" +
                    "        } catch (Exception e) {\n" +
                    "            e.printStackTrace();\n" +
                    "        }");
        } else {
            printlnWithIdent("        System.out.println(\"Usage : java " + options.className + " <input file(s)>\");\n");
        }

        printlnWithIdent("    }\n" +
                "    else {\n" +
                "        for (int i = 0; i < argv.length; i++) {\n" +
                "            try {\n" +
                "                DoubleBufferReader reader = new DoubleBufferReader(argv[i]);\n" +
                "                " + options.className + " scanner = new " + options.className + "(reader);\n" +
                "                while(scanner.lex());\n" +
                "                reader.close();\n" +
                "            }\n" +
                "            catch (java.io.FileNotFoundException e) {\n" +
                "                System.out.println(\"File not found : \\\"\"+argv[i]+\"\\\"\");\n" +
                "            }\n" +
                "            catch (java.io.IOException e) {\n" +
                "                System.out.println(\"IO error scanning file \\\"\"+argv[i]+\"\\\"\");\n" +
                "            }\n" +
                "            catch (Exception e) {\n" +
                "                System.out.println(\"Unexpected exception:\");\n" +
                "                e.printStackTrace();\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    private void printArray(int[][] values) {
        print("{\n");
        increaseIdent();
        for (int i = 0; i < values.length - 1; i++) {
            printIdent();
            printArray(values[i]);
            print(", \n");
        }
        printIdent();
        printArray(values[values.length - 1]);
        print("\n");
        decreaseIndent();
        printIdent();
        print("}");
    }

    private void printArray(int[] values) {
        print("{ ");
        for (int i = 0; i < values.length - 1; i++) {
            print(values[i] + ", ");
        }
        print(String.valueOf(values[values.length - 1]));
        print(" }");
    }

    private void printArray(boolean[] values) {
        print("{ ");
        for (int i = 0; i < values.length - 1; i++) {
            print(values[i] + ", ");
        }
        print(String.valueOf(values[values.length - 1]));
        print(" }");
    }

    private void print(String text) {
        for (int i = 0, len = text.length(); i < len; i++) {
            try {
                out.write(text.charAt(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printlnWithIdent(String text) {
        String[] ss = text.split("\n");
        for (String s : ss) {
            printIdent();
            print(s);
            try {
                out.write('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printIdent() {
        for (int i = 0; i < ident; i++) {
            try {
                out.write(' ');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void increaseIdent() {
        ident += IDENT;
    }

    private void decreaseIndent() {
        ident -= IDENT;
        if (ident < 0) ident = 0;
    }
}
