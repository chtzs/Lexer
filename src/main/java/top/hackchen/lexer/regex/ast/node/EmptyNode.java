package top.hackchen.lexer.regex.ast.node;

import top.hackchen.lexer.regex.common.constant.RegexConstant;
import top.hackchen.lexer.regex.common.constant.RegexType;

/**
 * @author 陈浩天
 * @date 2021/9/9 23:21 星期四
 */
public class EmptyNode extends Node {
    private static final EmptyNode INSTANCE = new EmptyNode();

    public static EmptyNode getInstance() {
        return INSTANCE;
    }

    private EmptyNode() {
        super(RegexType.EMPTY);
    }

    @Override
    public String getRegexString() {
        return String.valueOf(RegexConstant.EMPTY);
    }
}
