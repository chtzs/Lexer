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
        printDoubleBufferClass();
        decreaseIndent();
        printInnerCode();
        increaseIdent();
        printVariable();
        printConstructor();
        printDecompressFunc();
        printDoActionFunc();
        printLexFunc();
        printLexWordFunc();
        printMain();
        decreaseIndent();
        printlnWithIdent("}");
    }

    private void printVariable() {
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
    }

    private void printInnerCode() {
        printlnWithIdent(innerCode);
    }

    private void printConstructor() {
        printlnWithIdent((options.classModifier.equals("") ? "" : options.classModifier + " ") + options.className + "(DoubleBufferReader reader) throws IOException, DataFormatException{\n" +
                "    this.reader = reader;\n" +
                "    charToIndex = decompress(charToIndex);\n" +
                "}");
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

    private void printLexFunc() {
        printlnWithIdent("");
        printlnWithIdent("public void lex() throws IOException {\n" +
                "    int c;\n\n");
        if (options.isRollbackOptimization) {
            printlnWithIdent("    int bufferMaxSize = reader.bufferMaxSize;\n" +
                    "    //失败数组，只要是failed[状态编号]存在[当前输入]的，必定不能走到终状态。\n" +
                    "    Set<Integer>[] failed = new HashSet[transition.length];\n" +
                    "    for (int i = 0; i < transition.length; i++) {\n" +
                    "        failed[i] = new HashSet<>();\n" +
                    "    }\n" +
                    "    //读取指针偏移量栈，用来记录出错回滚位置\n" +
                    "    int[] offsetStack = new int[bufferMaxSize];\n" +
                    "    //状态栈，用来记录经过的状态\n" +
                    "    int[] stateStack = new int[bufferMaxSize];\n" +
                    "    //栈顶指针\n" +
                    "    int sp = 0;");
        }
        printlnWithIdent("    while ((c = reader.peek(offset)) != -1) {\n" +
                "        int condition = charToIndex[c];\n" +
                "        if (condition < 0) {\n" +
                "            throw new RuntimeException(\"词法分析遇到错误: 字符\" + c + \"在规约中未定义！\");\n" +
                "        }\n" +
                "        state = transition[state][condition];\n" +
                "        //死状态，要尝试回溯\n" +
                "        if (state == -1" + (options.isRollbackOptimization ? " || failed[state].contains(reader.realPos)" : "") +
                ") {\n" +
                "            if (preSuccessType == -1) {\n" +
                "                String msg = \"Token\" + lexWord() + \"不存在！\";\n" +
                "                throw new RuntimeException(\"词法分析遇到错误: \" + msg);\n" +
                "            } else {");
        if (options.isRollbackOptimization) {
            printlnWithIdent("                //这时候栈中保存的都是不可到达终态的<状态，条件>组合\n" +
                    "                while (sp > 0) {\n" +
                    "                    failed[stateStack[sp - 1]].add(offsetStack[sp - 1]);\n" +
                    "                    sp--;\n" +
                    "                }");
        }
        printlnWithIdent("                //回溯并输出\n" +
                "                doAction(preSuccessType);\n" +
                "                \n" +
                "                reader.movePosTo(preSuccessOffset + 1);\n" +
                "                preSuccessType = -1;\n" +
                "                preSuccessOffset = -1;\n" +
                "                offset = 0;\n" +
                "                state = beginState;\n" +
                "                continue;\n" +
                "            }\n" +
                "        } else {");
        if (options.isRollbackOptimization) {
            printlnWithIdent("            //偏移量入栈\n" +
                    "            offsetStack[sp] = reader.realPos + offset;\n" +
                    "            //状态入栈\n" +
                    "            stateStack[sp] = state;\n" +
                    "            sp++;");
        }
        printlnWithIdent("            if (isFinalState[state]) {\n" +
                "                preSuccessType = finalStateType[state];\n" +
                "                preSuccessOffset = offset;");
        if (options.isRollbackOptimization) {
            printlnWithIdent(
                    "                //一旦成功，立刻清空两个栈，确保栈中只有不可到达终态的<状态，条件>组合\n" +
                            "                sp = 0;");
        }
        printlnWithIdent("            }\n" +
                "        }\n" +
                "        offset++;\n" +
                "    }\n" +
                "    if (preSuccessType != -1) {\n" +
                "        doAction(preSuccessType);\n" +
                "    }\n" +
                "}");
    }

    private void printDoActionFunc() {
        printlnWithIdent("");
        printlnWithIdent("private void doAction(int type) {\n" +
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
                "                scanner.lex();\n" +
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

    private void printDoubleBufferClass() {
        printlnWithIdent("public static class DoubleBufferReader implements AutoCloseable {\n" +
                "    private static final int DEFAULT_BUFFER_MAX_SIZE = 8192;\n" +
                "\n" +
                "    //文件reader\n" +
                "    private final Reader reader;\n" +
                "    //两个交替的缓冲区\n" +
                "    private final char[][] buffer = new char[2][DEFAULT_BUFFER_MAX_SIZE];\n" +
                "    //每个缓冲区的大小\n" +
                "    private final int[] bufferSize = new int[2];\n" +
                "    //当前选择的缓冲区\n" +
                "    private int currentBuffer = 0;\n" +
                "    //当前缓冲区的读取指针\n" +
                "    private int currPos = 0;\n" +
                "    //读取真实位置\n" +
                "    public int realPos = 0;\n" +
                "    //缓冲区最大大小\n" +
                "    public final int bufferMaxSize;\n" +
                "\n" +
                "    public DoubleBufferReader(String file) throws IOException {\n" +
                "        reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);\n" +
                "        this.bufferMaxSize = DEFAULT_BUFFER_MAX_SIZE;\n" +
                "        init();\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    public DoubleBufferReader(InputStream in) throws IOException {\n" +
                "        reader = new InputStreamReader(in);\n" +
                "        this.bufferMaxSize = DEFAULT_BUFFER_MAX_SIZE;\n" +
                "        init();\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    public int peek() {\n" +
                "        return peek(0);\n" +
                "    }\n" +
                "\n" +
                "    public int peek(int offset) {\n" +
                "        if (bufferSize[currentBuffer] == -1) {\n" +
                "            return -1;\n" +
                "        } else if (currPos + offset < bufferSize[currentBuffer]) {\n" +
                "            return buffer[currentBuffer][currPos + offset];\n" +
                "        } else {\n" +
                "            int nextPos = currPos + offset - bufferSize[currentBuffer];\n" +
                "            if (nextPos < bufferSize[1 - currentBuffer]) {\n" +
                "                return buffer[1 - currentBuffer][nextPos];\n" +
                "            } else {\n" +
                "                return -1;\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public int read() throws IOException {\n" +
                "        int c = peek();\n" +
                "        movePosTo(1);\n" +
                "        return c;\n" +
                "    }\n" +
                "\n" +
                "    public void movePosTo(int offset) throws IOException {\n" +
                "        if (bufferSize[currentBuffer] == -1) {\n" +
                "            return;\n" +
                "        }\n" +
                "\n" +
                "        if (currPos + offset < bufferSize[currentBuffer]) {\n" +
                "            currPos += offset;\n" +
                "            realPos += offset;\n" +
                "        } else {\n" +
                "            //切换缓冲区\n" +
                "            currentBuffer = 1 - currentBuffer;\n" +
                "            readToBuffer();\n" +
                "            movePosTo(offset - bufferSize[currentBuffer]);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void readToBuffer() throws IOException {\n" +
                "        bufferSize[currentBuffer] = reader.read(buffer[currentBuffer], 0, bufferMaxSize);\n" +
                "    }\n" +
                "\n" +
                "    private void init() throws IOException {\n" +
                "        readToBuffer();\n" +
                "        if (bufferSize[0] < bufferMaxSize) {\n" +
                "            bufferSize[1] = -1;\n" +
                "        } else {\n" +
                "            currentBuffer = 1;\n" +
                "            readToBuffer();\n" +
                "            currentBuffer = 0;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void close() throws IOException {\n" +
                "        reader.close();\n" +
                "    }\n" +
                "}\n");
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
