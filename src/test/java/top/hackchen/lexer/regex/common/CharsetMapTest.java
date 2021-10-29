package top.hackchen.lexer.regex.common;

import org.junit.Test;
import top.hackchen.lexer.regex.ast.node.CharsetNode;

/**
 * @author 陈浩天
 * @date 2021/9/27 17:54 星期一
 */
public class CharsetMapTest {

    @Test
    public void add() {
        CharsetMap map = new CharsetMap();
        map.addNode(new CharsetNode('a', 'z'));
        map.addNode(new CharsetNode('A', 'Z'));
        map.addNode(new CharsetNode('0'));
        map.apply();
        System.out.println(map.getNumber('1'));
        System.out.println(map.getNumber('2'));
        System.out.println(map.getNumber('0'));
        System.out.println(map.getNumber('a'));
        System.out.println(map.getNumber('A'));
        System.out.println(map.getNumber((char) ('Z' + 1)));
        System.out.println(map);
    }

    @Test
    public void add2() {
        CharsetMap map = new CharsetMap();
        CharsetNode charsetNode = new CharsetNode('0');
        charsetNode.setReversed(true);
        map.addNode(charsetNode);
        map.apply();

        System.out.println(map.getNumber('a'));
        System.out.println(map.getNumber('A'));
        System.out.println(map.getNumber('0'));
        System.out.println(map.getNumber((char) ('0' - 1)));
        System.out.println(map.getNumber((char) ('0' + 1)));
    }

    @Test
    public void add3() {
        CharsetMap map = new CharsetMap();
        map.addNode(new CharsetNode(0, 65535));
        map.addNode(new CharsetNode(100));
        map.apply();
    }

    @Test
    public void add4() {
        CharsetMap map = new CharsetMap();
        map.addNode(new CharsetNode('a', 'z'));
        map.addNode(new CharsetNode('k'));
        map.addNode(new CharsetNode('l'));
        map.addNode(new CharsetNode('m'));
        map.addNode(new CharsetNode('o'));
        map.addNode(new CharsetNode('A', 'Z'));
    }
}