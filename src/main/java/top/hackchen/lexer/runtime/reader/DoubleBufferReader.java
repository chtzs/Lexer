package top.hackchen.lexer.runtime.reader;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author 陈浩天
 * @date 2021/10/13 19:31 星期三
 */
public class DoubleBufferReader implements AutoCloseable {
    private static final int DEFAULT_BUFFER_MAX_SIZE = 8192;

    //文件reader
    private final Reader reader;
    //两个交替的缓冲区
    private final char[][] buffer = new char[2][DEFAULT_BUFFER_MAX_SIZE];
    //每个缓冲区的大小
    private final int[] bufferSize = new int[2];
    //当前选择的缓冲区
    private int currentBuffer = 0;
    //当前缓冲区的读取指针
    private int currPos = 0;
    //读取真实位置
    public int realPos = 0;
    //已经读取的行数
    public int line = 1;
    //当前行的位置
    public int vPos = 0;
    //缓冲区最大大小
    private final int bufferMaxSize;

    public DoubleBufferReader(String file) throws IOException {
        this(file, DEFAULT_BUFFER_MAX_SIZE);
    }

    public DoubleBufferReader(InputStream inputStream) throws IOException {
        this(inputStream, DEFAULT_BUFFER_MAX_SIZE);
    }

    public DoubleBufferReader(String file, int bufferMaxSize) throws IOException {
        reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        this.bufferMaxSize = bufferMaxSize;
        init();
    }

    public DoubleBufferReader(InputStream inputStream, int bufferMaxSize) throws IOException {
        reader = new InputStreamReader(inputStream);
        this.bufferMaxSize = bufferMaxSize;
        init();
    }


    public int peek() {
        return peek(0);
    }

    /**
     * 在当前位置往后读取第offset个字符
     *
     * @param offset 偏移量，必须是非负整数
     * @return 字符
     */
    public int peek(int offset) {
        if (bufferSize[currentBuffer] == -1) {
            return -1;
        } else if (currPos + offset < bufferSize[currentBuffer]) {
            return buffer[currentBuffer][currPos + offset];
        } else {
            int nextPos = currPos + offset - bufferSize[currentBuffer];
            if (nextPos < bufferSize[1 - currentBuffer]) {
                return buffer[1 - currentBuffer][nextPos];
            } else {
                return -1;
            }
        }
    }

    public int read() throws IOException {
        int c = peek();
        movePosTo(1);
        return c;
    }

    /**
     * 移动当前读取指针，如果超过当前缓冲区大小，则更换缓冲区并且更新位置
     *
     * @param offset 当前缓冲区指针移动的偏移量，必须是非负整数
     * @throws IOException 更换缓冲区的时候可能发生的IO异常
     */
    public void movePosTo(int offset) throws IOException {
        if (bufferSize[currentBuffer] == -1) {
            return;
        }

        if (currPos + offset < bufferSize[currentBuffer]) {
            for (int i = 0; i < offset; i++) {
                int peek = peek(i);
                if (peek == '\n') {
                    line++;
                    vPos = 0;
                } else {
                    vPos++;
                }
            }
            currPos += offset;
            realPos += offset;
        } else {
            //切换缓冲区
            currentBuffer = 1 - currentBuffer;
            readToBuffer();
            movePosTo(offset - bufferSize[currentBuffer]);
            //currPos = currPos + offset - bufferSize[currentBuffer];
        }
    }

    /**
     * 为当前缓冲区读取数据，并更新bufferOffset
     *
     * @throws IOException 如果读取时发生IO错误
     */
    private void readToBuffer() throws IOException {
        bufferSize[currentBuffer] = reader.read(buffer[currentBuffer], 0, bufferMaxSize);
    }


    private void init() throws IOException {
        readToBuffer();
        if (bufferSize[0] < bufferMaxSize) {
            bufferSize[1] = -1;
        } else {
            currentBuffer = 1;
            readToBuffer();
            currentBuffer = 0;
        }
    }

    public int getBufferMaxSize() {
        return bufferMaxSize;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
