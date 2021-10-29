package top.hackchen.lexer.regex.common;

/**
 * @author 陈浩天
 * @date 2021/9/9 19:29 星期四
 */
public class Result<T> {
    protected int nextPos;
    protected T value;

    public Result(int nextPos, T value) {
        this.nextPos = nextPos;
        this.value = value;
    }

    public int getNextPos() {
        return nextPos;
    }

    public void setNextPos(int nextPos) {
        this.nextPos = nextPos;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Result{" +
                "nextPos=" + nextPos +
                ", value=" + value +
                '}';
    }
}
