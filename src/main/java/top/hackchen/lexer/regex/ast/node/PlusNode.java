package top.hackchen.lexer.regex.ast.node;


import top.hackchen.lexer.regex.common.constant.RegexConstant;
import top.hackchen.lexer.regex.common.constant.RegexType;

/**
 * @author 陈浩天
 * @date 2021/9/9 22:54 星期四
 */
public class PlusNode extends Node {
    public PlusNode(Node child) {
        super(RegexType.PLUS_CLOSURE);
        this.children.add(child);
    }

    @Override
    public String getRegexString() {
        return children.get(0).getRegexString() + RegexConstant.PLUS_CLOSURE;
    }
}
