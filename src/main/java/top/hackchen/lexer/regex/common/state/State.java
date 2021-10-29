package top.hackchen.lexer.regex.common.state;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author 陈浩天
 * @date 2021/9/10 14:59 星期五
 */
public class State {
    /**
     * 是否是终态
     */
    protected boolean finalState;
    /**
     * 一个数字，代表状态唯一身份
     */
    protected int identity;
    /**
     * 状态附带的类型，比如可以指定[0-9]解析出来的状态的终态为数字类型等等。
     */
    protected int type = -1;

    public State(int identity) {
        this(false, identity);
    }

    public State(boolean finalState, int identity) {
        this.finalState = finalState;
        this.identity = identity;
    }

    public boolean isFinalState() {
        return finalState;
    }

    public void setFinalState(boolean finalState) {
        this.finalState = finalState;
    }

    public int getIdentity() {
        return identity;
    }

    public void setIdentity(int identity) {
        this.identity = identity;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return identity == state.identity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity);
    }

    @Override
    public String toString() {
        return String.valueOf(identity);
    }
}
