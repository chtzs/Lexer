package top.hackchen.lexer.regex.ast.node;

import top.hackchen.lexer.regex.common.constant.RegexType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 陈浩天
 * @date 2021/9/9 19:19 星期四
 */
public class Node implements Cloneable {
    protected RegexType regexType;
    protected List<Node> children = new ArrayList<>();

    public RegexType getRegexType() {
        return regexType;
    }

    public void setRegexType(RegexType regexType) {
        this.regexType = regexType;
    }

    public Node() {
        this(RegexType.NODE);
    }

    public Node(RegexType regexType) {
        this.regexType = regexType;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    @Override
    public Node clone() {
        try {
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            Node newNode = (Node) super.clone();
            newNode.children = new ArrayList<>();
            return newNode;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String getRegexString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        for (int i = children.size() - 1; i >= 0; i--) {
            builder.append(children.get(i).getRegexString());
        }
        builder.append(')');
        return builder.toString();
    }
}
