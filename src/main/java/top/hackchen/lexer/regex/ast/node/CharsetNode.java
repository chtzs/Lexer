package top.hackchen.lexer.regex.ast.node;


import top.hackchen.lexer.regex.common.constant.RegexType;

import java.util.Objects;

/**
 * @author 陈浩天
 * @date 2021/9/9 19:38 星期四
 */
public class CharsetNode extends Node implements Comparable<CharsetNode> {
    protected boolean reversed = false;
    protected int begin;
    protected int end;

    public CharsetNode() {
        super(RegexType.CHARSET);
        begin = -1;
        end = -2;
    }

    public CharsetNode(int value) {
        super(RegexType.CHARSET);
        begin = end = value;
    }

    public CharsetNode(int begin, int end) {
        super(RegexType.CHARSET);
        this.begin = begin;
        this.end = end;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public boolean isEmpty() {
        return begin > end;
    }

    public boolean contains(int val) {
        return begin <= val && val <= end;
    }

    public CharsetNode intersect(CharsetNode other) {
        CharsetNode front = begin <= other.begin ? this : other;
        CharsetNode back = begin > other.begin ? this : other;
        if (front.contains(back.begin)) {
            return new CharsetNode(back.begin, Math.min(front.end, back.end));
        } else {
            return new CharsetNode();
        }
    }

    public CharsetNode[] subtract(CharsetNode other) {
        CharsetNode intersect = intersect(other);
        if (intersect.isEmpty()) {
            return new CharsetNode[]{new CharsetNode(this.begin, this.end)};
        } else {
            return new CharsetNode[]{new CharsetNode(begin, other.begin - 1), new CharsetNode(other.end + 1, end)};
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharsetNode that = (CharsetNode) o;
        return reversed == that.reversed && begin == that.begin && end == that.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reversed, begin, end);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "CharsetNode {Empty body}";
        } else {
            StringBuilder builder = new StringBuilder();
            if (begin == end) {
                builder.append((char) begin);
            } else {
                builder.append('[');
                builder.append((char) begin);
                builder.append('-');
                builder.append((char) end);
                builder.append(']');
            }
            return "CharsetNode{" +
                    "ints=" + builder +
                    ", reversed=" + reversed +
                    '}';
        }
    }

    @Override
    public String getRegexString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append(reversed ? "^" : "");
        if (begin == end) {
            builder.append((char) begin);
        } else {
            builder.append((char) begin);
            builder.append('-');
            builder.append((char) end);
        }
        builder.append(']');
        return builder.toString();
    }

    @Override
    public int compareTo(CharsetNode o) {
        return begin - o.begin;
    }
}
