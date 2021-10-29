package top.hackchen.lexer.regex.common;

import top.hackchen.lexer.regex.ast.node.CharsetNode;

import java.util.*;

/**
 * @author 陈浩天
 * @date 2021/9/27 16:25 星期一
 */
public class CharsetMap {
    //字符转换成对应的字符集下标
    public int[] charToNumber = new int[65536];
    //原始字符集
    protected Set<CharsetNode> originalCharset = new HashSet<>();
    //原始字符集分割成任意两两不相交的集合(如[0-9], [1], [6] -> [0], [1], [2-5], [6], [7-9])
    protected Set<CharsetNode> dividedCharset = new HashSet<>();
    //字符集分割后，不可能有任意两个集合有交集，因此任何字符集的区间的开头begin能唯一代表该字符集
    protected Map<Integer, CharsetNode> dividedCharsetMap = new HashMap<>();
    //分割后的字符集对应的数字
    protected Map<CharsetNode, Integer> dividedCharsetNumber = new HashMap<>();
    //字符集对应的数字集
    protected Map<CharsetNode, Set<Integer>> charsetNumbersCache = new HashMap<>();

    public CharsetMap() {
        Arrays.fill(charToNumber, -1);
    }

    public void addNode(CharsetNode charsetNode) {
        originalCharset.add(charsetNode);
    }

    public void addMap(CharsetMap charsetMap) {
        originalCharset.addAll(charsetMap.originalCharset);
    }

    /**
     * 计算正交字符集，填充字符-数字对照数组
     */
    public void apply() {
        dividedCharset = splitCharsets();
        fillMap();
    }

    /**
     * 填充字符-数字对照数组，分割后的字符集的对照map和字符集标号分配map
     */
    private void fillMap() {
        int index = 0;
        for (CharsetNode charsetNode : dividedCharset) {
            dividedCharsetMap.put(charsetNode.getBegin(), charsetNode);
            dividedCharsetNumber.put(charsetNode, index);
            for (int begin = charsetNode.getBegin(), end = charsetNode.getEnd(); begin <= end; begin++) {
                charToNumber[begin] = index;
            }
            index++;
        }
    }

    /**
     * 分割原始字符集
     * @return 分割后的字符集
     */
    private Set<CharsetNode> splitCharsets() {
        Set<CharsetNode> all = new HashSet<>();
        for (CharsetNode splitter : originalCharset) {
            all = splitCharsetsWithSplitter(all, splitter);
        }
        return all;
    }

    /**
     * 用splitter分割dest字符集集合中的每一个字符集，并且用分割后的字符集回分割splitter，最终合并两者的分割结果
     * @param dest 目标字符集集合
     * @param splitter 分割器
     * @return 分割后的字符集
     */
    private Set<CharsetNode> splitCharsetsWithSplitter(Set<CharsetNode> dest, CharsetNode splitter) {
        Set<CharsetNode> result = new HashSet<>();
        Set<CharsetNode> splitterSet = new HashSet<>();
        splitterSet.add(splitter);
        for (CharsetNode n : dest) {
            Set<CharsetNode> dividedSplitter = new HashSet<>();
            for (CharsetNode s : splitterSet) {
                Set<CharsetNode> divided = splitCharsetWithSplitter(s, n);
                dividedSplitter.addAll(divided);
            }
            splitterSet = dividedSplitter;

            for (CharsetNode s : dividedSplitter) {
                Set<CharsetNode> divided = splitCharsetWithSplitter(n, s);
                result.addAll(divided);
            }
        }

        result.addAll(splitterSet);
        return result;
    }

    /**
     * 用splitter分割dest，分割方法是：
     * I = dest ∩ splitter
     * result = dest - I
     * @param dest 被分割的字符集
     * @param splitter 分割器
     * @return 分割后的字符集集合
     */
    private Set<CharsetNode> splitCharsetWithSplitter(CharsetNode dest, CharsetNode splitter) {
        CharsetNode intersect = dest.intersect(splitter);
        CharsetNode[] subtract = dest.subtract(intersect);
        Set<CharsetNode> result = new HashSet<>();
        addAll(result, subtract);
        addAll(result, intersect);
        return result;
    }

    private void addAll(Set<CharsetNode> dest, CharsetNode... charsetNodes) {
        for (CharsetNode charsetNode : charsetNodes) {
            if (!charsetNode.isEmpty()) {
                dest.add(charsetNode);
            }
        }
    }

    /**
     * 获取字符集对应的数字编号
     * @param target 目标字符集
     * @return 编号集合
     */
    public Set<Integer> getReplacedNumbers(CharsetNode target) {
        if (!charsetNumbersCache.containsKey(target)) {
            charsetNumbersCache.put(target, replaceCharsetToNumbers(target));
        }
        return charsetNumbersCache.get(target);
    }

    private Set<Integer> replaceCharsetToNumbers(CharsetNode target) {
        Set<Integer> numbers = new HashSet<>();
        int begin = target.getBegin();
        int end = target.getEnd();
        //因为分割后的字符集肯定是两两不相交的，所以对于target,
        //必定存在dividedCharset其中的元素，按照大小头尾相接紧密连接，能够组成区间[target.begin, target.end]。
        while (begin <= end) {
            CharsetNode e = dividedCharsetMap.get(begin);
            if (e == null) {
                //这种情况不应该发生...
                throw new RuntimeException("Shit");
            }
            int index = dividedCharsetNumber.get(e);
            numbers.add(index);
            begin = e.getEnd() + 1;
        }
        return numbers;
    }

    public int getNumber(char c) {
        return charToNumber[c];
    }
}
