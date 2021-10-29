package top.hackchen.lexer.regex;

import java.util.Scanner;

/**
 * @author 陈浩天
 * @date 2021/9/27 15:52 星期一
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("暂时没有发现bug，这是一个自定义的纯正则表达式引擎");
        System.out.println("回顾一下正则的定义：");
        System.out.println("1. ε(空串) 是正则表达式");
        System.out.println("2. 任意单个字符x 是正则表达式");
        System.out.println("下面的定义的前提是r, r1, r2是正则表达式");
        System.out.println("3. r1 | r2 是正则表达式");
        System.out.println("4. r1r2 是正则表达式(直接连接)");
        System.out.println("5. (r)是正则表达式");
        System.out.println("6. r*是正则表达式，表示r上的闭包(人话：r会出现0次或者任意次)");
        System.out.println("以上是正则表达式的完整定义，下面是纯正则范围内的扩展方法：");
        System.out.println("7. r+是正则表达式，等价于rr*，表示r出现至少一次");
        System.out.println("8. r?是正则表达式，等价于r|ε，表示r出现0次或者1次");
        System.out.println("9. [abc...]是正则表达式，等价于a|b|c...");
        System.out.println("10. [xm-xn]是正则表达式，等价于(xm)|(xm + 1)|(xm + 2)...|(xn)");
        System.out.println("11. r{m}是正则表达式，等价于rrr...r(m个r)，表示r出现刚好m次");
        System.out.println("12. r{m, n}是正则表达式，等价于r{m}|r{m+1}...|r{n}，表示r出现至少m次，最多n次");
        System.out.println("13. r{m, }是正则表达式，等价于r{m}r*，表示r出现至少m次");
        System.out.println("这个项目前前后后花了我一个星期的时间，现在终于完成了，好激动");
        System.out.println("现在，请输入一个正则表达式，下面将会不断循环，用这个表达式测试您接下来输入的任何字符串");
        System.out.println("注意：控制台会吞入空白字符，请自行编写代码测试空白字符");
        Scanner in = new Scanner(System.in);
        String pattern = in.nextLine().trim();
        Regex regex = Regex.compile(pattern);
        System.out.println("编译完成，请输入测试字符串");
        while (in.hasNextLine()) {
            System.out.println(regex.isMatch(in.nextLine().trim()));
        }
    }
}
