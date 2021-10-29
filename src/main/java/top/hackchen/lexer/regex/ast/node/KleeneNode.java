package top.hackchen.lexer.regex.ast.node;

import top.hackchen.lexer.regex.common.constant.RegexType;

/**
 * @author 陈浩天
 * @date 2021/9/9 22:48 星期四
 */
public class KleeneNode extends Node {
    public KleeneNode(Node child) {
        super(RegexType.KLEENE_CLOSURE);
        this.children.add(child);
    }

    @Override
    public String getRegexString() {
        return children.get(0).getRegexString() + "*";
    }
}
