















































































































































































































































































































































































//Generated automatically by C-Flex. Do not modify.import java.util.*;import java.io.*;import java.nio.ByteBuffer;import java.nio.ByteOrder;import java.nio.IntBuffer;import java.nio.charset.StandardCharsets;import java.util.zip.DataFormatException;import java.util.zip.Inflater;public class TestParser {    private static class DoubleBufferReader implements AutoCloseable {        private static final int DEFAULT_BUFFER_MAX_SIZE = 8192;            //文件reader        private final Reader reader;        //两个交替的缓冲区        private final char[][] buffer = new char[2][DEFAULT_BUFFER_MAX_SIZE];        //每个缓冲区的大小        private final int[] bufferSize = new int[2];        //当前选择的缓冲区        private int currentBuffer = 0;        //当前缓冲区的读取指针        private int currPos = 0;        //读取真实位置        public int realPos = 0;        //缓冲区最大大小        public final int bufferMaxSize;            public DoubleBufferReader(String file) throws IOException {            reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);            this.bufferMaxSize = DEFAULT_BUFFER_MAX_SIZE;            init();        }                public DoubleBufferReader(InputStream in) throws IOException {            reader = new InputStreamReader(in);            this.bufferMaxSize = DEFAULT_BUFFER_MAX_SIZE;            init();        }                public int peek() {            return peek(0);        }            /**         * 在当前位置往后读取第offset个字符         *         * @param offset 偏移量，必须是非负整数         * @return 字符         */        public int peek(int offset) {            if (bufferSize[currentBuffer] == -1) {                return -1;            } else if (currPos + offset < bufferSize[currentBuffer]) {                return buffer[currentBuffer][currPos + offset];            } else {                int nextPos = currPos + offset - bufferSize[currentBuffer];                if (nextPos < bufferSize[1 - currentBuffer]) {                    return buffer[1 - currentBuffer][nextPos];                } else {                    return -1;                }            }        }            public int read() throws IOException {            int c = peek();            movePosTo(1);            return c;        }            /**         * 移动当前读取指针，如果超过当前缓冲区大小，则更换缓冲区并且更新位置         *         * @param offset 当前缓冲区指针移动的偏移量，必须是非负整数         * @throws IOException 更换缓冲区的时候可能发生的IO异常         */        public void movePosTo(int offset) throws IOException {            if (bufferSize[currentBuffer] == -1) {                return;            }                if (currPos + offset < bufferSize[currentBuffer]) {                currPos += offset;                realPos += offset;            } else {                //切换缓冲区                currentBuffer = 1 - currentBuffer;                readToBuffer();                movePosTo(offset - bufferSize[currentBuffer]);                //currPos = currPos + offset - bufferSize[currentBuffer];            }        }            /**         * 为当前缓冲区读取数据，并更新bufferOffset         *         * @throws IOException 如果读取时发生IO错误         */        private void readToBuffer() throws IOException {            bufferSize[currentBuffer] = reader.read(buffer[currentBuffer], 0, bufferMaxSize);        }            private void init() throws IOException {            readToBuffer();            if (bufferSize[0] < bufferMaxSize) {                bufferSize[1] = -1;            } else {                currentBuffer = 1;                readToBuffer();                currentBuffer = 0;            }        }            @Override        public void close() throws IOException {            reader.close();        }    }    public static enum ParserType {        //标识符        IDENTIFIER,         //赋值        EQUALS,        //分号        SEMICOLON,        //或者        OR,        //左括号        LEFT_PARENTHESES,        //右括号        RIGHT_PARENTHESES,        //引用模式开始        REFERENCE_START,        //引用模式结束        REFERENCE_END,        //分割符        SPLIT,        //转义        ESCAPE,        //选项        OPTION    }    public static class Token {        public ParserType type;        public Object val;        public Token(ParserType type, Object val) {            this.type = type;            this.val = val;        }        public Token(ParserType type) {            this(type, null);        }        public String toString() {            return "<" + type + (val == null ? "" : (" val: " + val.toString())) + ">";        }    }    public List<Token> tokenList = new ArrayList<>();    private final int beginState = 20;    private final DoubleBufferReader reader;    private int beginPos = 0;    private int state = beginState;    private int preSuccessType = -1;    private int preSuccessOffset = -1;    private int offset = 0;    private final int[][] transition = {
        { -1, 17, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, 13, -1, -1, 16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 13, 13 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, 14, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, 13, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 13, -1, 13, 13 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }, 
        { 17, 17, 18, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 3, 17, 17, 17, 17, 17, 17, 17 }, 
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 3, -1, -1, -1, -1, -1, -1, -1 }, 
        { -1, -1, -1, 19, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 19, -1, 19, 19 }, 
        { 1, 0, 1, 19, 4, 2, 6, 5, 8, 7, 1, 1, 1, 1, 9, 1, 11, 10, 12, 1, 1, 1, 1, 1, 1, 1, 1, 19, 19 }
    };
    private final boolean[] isFinalState = { true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, false };
    private final int[] finalStateType = { 13, 13, 4, 0, 2, 8, 13, 13, 13, 7, 5, 12, 3, 11, 9, 10, 6, -1, 0, 1, -1 };
    private int[] charToIndex = { 2023550403, 923714112, 311331, -409161217, -11659543, 168464604, -529906022, 1389015924, 2076792125, -856317970, 1939519354, -1682719617, 969426887, -677583815, -944113192, -151720560, -881158981, 1819949775, 1664021156, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1431655766, -1430274560, -1751645677 };
        public TestParser(DoubleBufferReader reader) throws IOException, DataFormatException{        this.reader = reader    ;    charToIndex = decompress(charToIndex);    }        public static byte[] decompress(byte[] data) throws IOException, DataFormatException {        Inflater inflater = new Inflater();        inflater.setInput(data);            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);        byte[] buffer = new byte[1024];        while (!inflater.finished()) {            int count = inflater.inflate(buffer);            outputStream.write(buffer, 0, count);        }        outputStream.close();            return outputStream.toByteArray();    }        public static int[] decompress(int[] values) throws IOException, DataFormatException {        byte[] cb = integersToBytes(values);        byte[] decompress = decompress(cb);        return bytesToIntegers(decompress);    }        public static byte[] integersToBytes(int[] values) {        ByteBuffer byteBuffer = ByteBuffer.allocate(values.length * 4);        IntBuffer intBuffer = byteBuffer.asIntBuffer();        intBuffer.put(values);        return byteBuffer.array();    }        public static int[] bytesToIntegers(byte[] values) {        IntBuffer intBuf =                ByteBuffer.wrap(values)                        .order(ByteOrder.BIG_ENDIAN)                        .asIntBuffer();        int[] array = new int[intBuf.remaining()];        intBuf.get(array);        return array;    }        private void doAction(int type) {        switch (type) {            case 0:                {}                break;            case 1:                {        tokenList.add(new Token(ParserType.IDENTIFIER, lexWord()));    }                break;            case 2:                {        tokenList.add(new Token(ParserType.EQUALS));    }                break;            case 3:                {        tokenList.add(new Token(ParserType.OR));    }                break;            case 4:                {        tokenList.add(new Token(ParserType.SEMICOLON));    }                break;            case 5:                {        tokenList.add(new Token(ParserType.ESCAPE));    }                break;            case 6:                {        tokenList.add(new Token(ParserType.SPLIT));    }                break;            case 7:                {        tokenList.add(new Token(ParserType.LEFT_PARENTHESES));    }                break;            case 8:                {        tokenList.add(new Token(ParserType.RIGHT_PARENTHESES));    }                break;            case 9:                {        tokenList.add(new Token(ParserType.REFERENCE_START));    }                break;            case 10:                {        tokenList.add(new Token(ParserType.REFERENCE_END));    }                break;            case 11:                {        tokenList.add(new Token(ParserType.OPTION, lexWord()));    }                break;            case 12:                {        tokenList.forEach(System.out::println);    }                break;            case 13:                {}                break;        }    }        private void lex() throws IOException {        int c;        while ((c = reader.peek(offset)) != -1) {            int condition = charToIndex[c];            if (condition < 0) {                throw new RuntimeException("词法分析遇到错误: 字符" + c + "在规约中未定义！");            }            state = transition[state][condition];            //死状态，要尝试回溯            if (state == -1) {                if (preSuccessType == -1) {                    String msg = "Token" + lexWord() + "不存在！";                    throw new RuntimeException("词法分析遇到错误: " + msg);                } else {                    //回溯并输出                    doAction(preSuccessType);                                        reader.movePosTo(preSuccessOffset + 1);                    preSuccessType = -1;                    preSuccessOffset = -1;                    offset = 0;                    state = beginState;                    continue;            }            } else {                //一旦成功，立刻清空两个栈，确保栈中只有不可到达终态的<状态，条件>组合                if (isFinalState[state]) {                    preSuccessType = finalStateType[state];                    preSuccessOffset = offset;                }            }            offset++;        }        if (preSuccessType != -1) {            doAction(preSuccessType);        }    }        private String lexWord() {        char[] wordChar = new char[offset];        for (int i = 0; i < offset; i++) {            wordChar[i] = (char) reader.peek(i);        }        return new String(wordChar);    }        public static void main(String[] argv) {        if (argv.length == 0) {            try {                DoubleBufferReader reader = new DoubleBufferReader(System.in);                TestParser scanner = new TestParser(reader);                scanner.lex();            } catch (Exception e) {                e.printStackTrace();            }        }        else {            for (int i = 0; i < argv.length; i++) {                try {                    DoubleBufferReader reader = new DoubleBufferReader(argv[i]);                    TestParser scanner = new TestParser(reader);                    scanner.lex();                    reader.close();                }                catch (java.io.FileNotFoundException e) {                    System.out.println("File not found : \""+argv[i]+"\"");                }                catch (java.io.IOException e) {                    System.out.println("IO error scanning file \""+argv[i]+"\"");                    System.out.println(e);                }                catch (Exception e) {                    System.out.println("Unexpected exception:");                    e.printStackTrace();                }            }        }    }}