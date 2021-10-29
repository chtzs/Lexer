package top.hackchen.lexer.regex.common.state;

import java.util.Objects;

/**
 * 条件，也就是状态机中的边
 * @author 陈浩天
 * @date 2021/9/10 15:04 星期五
 */
public class Condition {
    /**
     * 条件类型
     */
    protected ConditionType conditionType;
    /**
     * 条件附带的值
     */
    protected int value;

    public static final Condition EPSILON = new Condition();

    private Condition() {
        conditionType = ConditionType.EPSILON;
    }

    public Condition(int value) {
        conditionType = ConditionType.CHARSET;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Condition condition = (Condition) o;
        return value == condition.value && conditionType == condition.conditionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionType, value);
    }

    @Override
    public String toString() {
        return "Condition{" +
                "conditionType=" + conditionType +
                ", value=" + value +
                '}';
    }
}
