package top.hackchen.lexer.regex.ast.node;

import top.hackchen.lexer.regex.common.constant.RegexConstant;
import top.hackchen.lexer.regex.common.constant.RegexType;

/**
 * @author 陈浩天
 * @date 2021/9/9 23:02 星期四
 */
public class SelectNode extends Node {
    public SelectNode(Node node) {
        super(RegexType.SELECT);
        this.children.add(node);
    }

    @Override
    public String getRegexString() {
        return children.get(0).getRegexString() + RegexConstant.SELECT;
    }
}
