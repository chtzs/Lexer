package top.hackchen.lexer.regex.ast.node;

import top.hackchen.lexer.regex.common.constant.RegexConstant;
import top.hackchen.lexer.regex.common.constant.RegexType;

/**
 * @author 陈浩天
 * @date 2021/9/9 23:04 星期四
 */
public class OrNode extends Node {
    public OrNode() {
        super(RegexType.OR);
    }

    @Override
    public String getRegexString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for(Node child : children){
            builder.append(child.getRegexString());
            builder.append(RegexConstant.OR);
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        return builder.toString();
    }
}
