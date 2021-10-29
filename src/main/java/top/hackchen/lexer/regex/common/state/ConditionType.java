package top.hackchen.lexer.regex.common.state;

/**
 * @author 陈浩天
 * @date 2021/9/10 15:06 星期五
 */
public enum ConditionType {
    /**
     * ε，无条件转移
     */
    EPSILON,
    /**
     * 匹配对应字符才能转移
     */
    CHARSET
}
