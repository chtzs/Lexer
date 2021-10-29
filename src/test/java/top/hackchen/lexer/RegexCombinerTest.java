package top.hackchen.lexer;

import org.junit.Test;
import top.hackchen.lexer.regex.RegexCombiner;

/**
 * @author 陈浩天
 * @date 2021/10/10 11:17 星期日
 */
public class RegexCombinerTest {

    @Test
    public void apply() {
        RegexCombiner combiner = new RegexCombiner();
        combiner.add("\\*", 0);
        combiner.add("\\*\\*", 1);
        combiner.apply();
        System.out.println((int)((char)-1));
    }
}