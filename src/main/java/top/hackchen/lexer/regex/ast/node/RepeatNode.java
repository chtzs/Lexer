package top.hackchen.lexer.regex.ast.node;

import top.hackchen.lexer.regex.common.constant.RegexType;

/**
 * @author 陈浩天
 * @date 2021/10/1 17:10 星期五
 */
public class RepeatNode extends Node {
    protected int from;
    protected int to;

    public RepeatNode(int from, int to) {
        super(RegexType.REPEAT);
        this.from = from;
        this.to = to;
        if (from > to && to >= 0) {
            throw new RuntimeException("Shit");
        }
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
