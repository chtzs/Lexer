package top.hackchen.lexer.regex.common.constant;

/**
 * @author 陈浩天
 * @date 2021/9/9 18:39 星期四
 */
public enum RegexType {
    /**
     * 空表达式，也被叫作epsilon，ε
     */
    EMPTY,
    /**
     * 左圆括号(
     */
    LEFT_PARENTHESES,
    /**
     * 右圆)括号
     */
    RIGHT_PARENTHESES,
    /**
     * 左方括号[
     */
    LEFT_BRACKET,
    /**
     * 右方括号]
     */
    RIGHT_BRACKET,
    /**
     * 正闭包+
     */
    PLUS_CLOSURE,
    /**
     * 克林闭包*
     */
    KLEENE_CLOSURE,
    /**
     * 表示或者|
     */
    OR,
    /**
     * 选择符?
     */
    SELECT,
    /**
     * 重复
     */
    REPEAT,
    /**
     * 字符集
     */
    CHARSET,
    /**
     * 普通节点
     */
    NODE;
}
