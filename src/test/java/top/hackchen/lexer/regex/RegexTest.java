package top.hackchen.lexer.regex;

import org.junit.Test;
import top.hackchen.lexer.regex.parser.CharacterParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 陈浩天
 * @date 2021/9/27 22:05 星期一
 */
public class RegexTest {
    private void assetRegex(String pattern, String text, boolean should) {
        Regex regex = Regex.compile(pattern);
        boolean nfa, dfa, minimized;
        nfa = dfa = minimized = true;
        if (regex.nfaMatch(text) != should) {
            System.out.println(pattern + " nfaMatch " + text + " failed!");
            nfa = false;
        }
        if (regex.dfaMatch(text) != should) {
            System.out.println(pattern + " dfaMatch " + text + " failed!");
            dfa = false;
        }
        if (regex.minimizedMatch(text) != should) {
            System.out.println(pattern + " minimizedMatch " + text + " failed!");
            minimized = false;
        }
        if (!nfa || !dfa || !minimized) {
            throw new RuntimeException("测试出错！请检查！");
        }
    }

    @Test
    public void simple() {
        Regex regex = Regex.compile("[^01]*abc");
        System.out.println(regex.isMatch("1234567"));
        System.out.println(regex.nfaMatch("22abc"));
        System.out.println(regex.isMatch("abc"));
    }

    @Test
    public void simple2() {
        String pattern = "([0-9])|(a[0-9]*)";
        String matchText = "a1234567";
        Regex regex = Regex.compile(pattern);
        System.out.println(regex.dfaMatch(matchText));
    }

    @Test
    public void or() {
        assetRegex("abc|123", "abc", true);
        assetRegex("abc|123", "abc", true);
        assetRegex("([0-9]|[1-9][0-9]*)", "0", true);
        assetRegex("([0-9]|[1-9][0-9]*)", "0234", false);
        assetRegex("(a)(b)?", "a", true);
    }

    @Test
    public void repeat() {
        assetRegex("\\d{2}", "12", true);
        assetRegex("\\d{2}", "123", false);
        assetRegex("\\d{2}", "1", false);
        assetRegex("\\d{2,}", "1", false);
        assetRegex("\\d{2,}", "12", true);
        assetRegex("\\d{2,}", "123", true);
        assetRegex("\\d{2,}", "1234567890", true);
        assetRegex("\\d{,10}", "1234567890", true);
        assetRegex("\\d{,10}", "12345678901", false);
        assetRegex("\\d{,10}", "", true);
    }

    @Test
    public void charset() {
        CharacterParser.nextCharOrCharset(new char[]{'0'}, 0);
        assetRegex("\\w", "b", true);
        assetRegex("\\d", "5", true);
        assetRegex("\\d+", "5153434516434", true);
        assetRegex("\\D+", "hello world", true);
        assetRegex("\\D+", "hello world0", false);
        assetRegex("[\\u4e00-\\u9fa5]+", "你好世界", true);
        assetRegex("[\\u4e00-\\u9fa5]+", "你好,世界", false);
        assetRegex("abc[\\u4e00-\\u9fa5]+", "abc你好世界", true);
    }

    @Test
    public void closure() {
        assetRegex(".*|abc", "bcd", true);
        assetRegex("(([0-9])|([1-9][0-9]*))(u[0-9]*)?", "0123", false);
        assetRegex("(1*)?abc", "0123", false);
        assetRegex("(1*)?abc", "abc", true);
        assetRegex("(1*)?abc", "1111111abc", true);
        assetRegex("[0-9]|a[0-9]*", "a1234567", true);
        assetRegex("[0-9]|a[0-9]*", "1234567", false);
        assetRegex("[0-9]|a[0-9]*", "0", true);
        assetRegex("[0-9]|a[0-9]*", "kk", false);
        assetRegex("a+", "aaa", true);
        assetRegex("a+", "b", false);
        assetRegex("a+", "ab", false);
    }

    @Test
    public void number() {
        String pattern = "([0-9]|[1-9][0-9]*)(\\.[0-9]*)?([eE][\\-+]?([0-9]|[1-9][0-9]*))?";
        assetRegex(pattern,
                "123", true);
        assetRegex(pattern,
                "123.3", true);
        assetRegex(pattern,
                "123.03", true);
        assetRegex(pattern,
                "0123", false);
        assetRegex(pattern,
                "0", true);
        assetRegex(pattern,
                "123.156e100", true);
        assetRegex(pattern,
                "123.156E-100", true);
        assetRegex(pattern,
                "0.156e100", true);
        assetRegex(pattern,
                "00.156e100", false);
    }

    @Test
    public void complexRegex() {
        //1. 验证邮箱
        assetRegex("\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?", "wsbcht@sina.com", true);
        assetRegex("\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?", "chthsa@163.com", true);
        assetRegex("\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?", "cht8436@gmail.com", true);
        assetRegex("\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?", "example@gmail.com.cn", true);
        assetRegex("\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?", "example@gmail.com.cn123", false);
        //2. 验证空白字符
        assetRegex("\\s+", " \r\n\f\u000B", true);
        //3. 验证手机号
        assetRegex("(\\+\\d+)?1[3458]\\d{9}", "13773363898", true);
        assetRegex("(\\+\\d+)?1[3458]\\d{9}", "+8613773363898", true);
        assetRegex("(\\+\\d+)?1[3458]\\d{9}", "11012345678", false);
        //4. 验证ip地址
        assetRegex("[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))",
                "127.0.0.1", true);
        assetRegex("[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))",
                "0.0.0.1", false);
        assetRegex("[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))",
                "192.168.0.1", true);
    }


    @Test
    public void speedCompare1() {
        String pattern = "([0-9]|[1-9][0-9]*)(\\.[0-9]*)?([eE][\\-+]?([0-9]|[1-9][0-9]*))?";
        String match = "123456.555e-10";
        Pattern p = Pattern.compile(pattern);
        //官方匹配100000次
        int count = 100000;
        long begin = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            Matcher matcher = p.matcher(match);
            matcher.find();
        }
        long end = System.currentTimeMillis();
        System.out.println("官方用法耗时：" + (end - begin) / 1000d + "s");
        //自定义匹配100000次
        Regex number = Regex.compile(pattern);
        begin = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            number.isMatch(match);
        }
        end = System.currentTimeMillis();
        System.out.println("自定义引擎DFA用法耗时：" + (end - begin) / 1000d + "s");
    }

    @Test
    public void speedCompare2() {
        String pattern = ("([0-9]|[1-9][0-9]*)(\\.[0-9]*)?([eE][\\-+]?([0-9]|[1-9][0-9]*))?");
        String match = "123456.555e-10";
        Regex number = Regex.compile(pattern);
        //自定义匹配100000次
        int count = 1000000;
        long begin = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            number.nfaMatch(match);
        }
        long end = System.currentTimeMillis();
        System.out.println("自定义引擎NFA用法耗时：" + (end - begin) / 1000d + "s");
        begin = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            number.dfaMatch(match);
        }
        end = System.currentTimeMillis();
        System.out.println("自定义引擎DFA用法耗时：" + (end - begin) / 1000d + "s");
    }
}