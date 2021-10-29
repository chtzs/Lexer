package top.hackchen.lexer.regex.common.constant;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 陈浩天
 * @date 2021/9/9 22:27 星期四
 */
public class RegexConstant {
    /**
     * 空表达式，也被叫作epsilon，ε
     */
    public static final char EMPTY = 'ε';
    /**
     * 左圆括号(
     */
    public static final char LEFT_PARENTHESES = '(';
    /**
     * 右圆括号)
     */
    public static final char RIGHT_PARENTHESES = ')';
    /**
     * 左方括号[
     */
    public static final char LEFT_BRACKET = '[';
    /**
     * 右方括号]
     */
    public static final char RIGHT_BRACKET = ']';
    /**
     * 左花括号{
     */
    public static final char LEFT_BRACE = '{';
    /**
     * 右花括号}
     */
    public static final char RIGHT_BRACE = '}';
    /**
     * 正闭包+
     */
    public static final char PLUS_CLOSURE = '+';
    /**
     * 克林闭包*
     */
    public static final char KLEENE_CLOSURE = '*';
    /**
     * 转义符
     */
    public static final char ESCAPE = '\\';
    /**
     * 反转符
     */
    public static final char REVERSE = '^';
    /**
     * 范围符
     */
    public static final char RANGE = '-';
    /**
     * 或者
     */
    public static final char OR = '|';
    /**
     * 选择符
     */
    public static final char SELECT = '?';
    /**
     * 通配符
     */
    public static final char ANY = '.';
    /**
     * 逗号
     */
    public static final char COMMA = ',';

    /**
     * 关键词数组
     */
    public static final char[] KEYWORD_ARRAY = {LEFT_PARENTHESES, RIGHT_PARENTHESES, LEFT_BRACKET, RIGHT_BRACKET,
            PLUS_CLOSURE, KLEENE_CLOSURE, ESCAPE, REVERSE, RANGE, OR, SELECT, ANY};

    /**
     * 关键词集合
     */
    public static final Set<Character> KEYWORDS = new HashSet<>();

    static {
        for (char c : KEYWORD_ARRAY) {
            KEYWORDS.add(c);
        }
    }
}
