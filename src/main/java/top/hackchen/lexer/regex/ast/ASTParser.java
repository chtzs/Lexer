package top.hackchen.lexer.regex.ast;

import top.hackchen.lexer.regex.ast.node.*;
import top.hackchen.lexer.regex.common.CharsetMap;
import top.hackchen.lexer.regex.common.constant.RegexType;
import top.hackchen.lexer.regex.common.Result;
import top.hackchen.lexer.regex.exception.RegexException;
import top.hackchen.lexer.regex.parser.CharacterParser;
import top.hackchen.lexer.regex.parser.RepeatParser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static top.hackchen.lexer.regex.common.constant.RegexConstant.*;

/**
 * @author 陈浩天
 * @date 2021/9/27 16:24 星期一
 */
public class ASTParser {
    protected Node root;
    protected CharsetMap charsetMap = new CharsetMap();
    protected String pattern;
    protected char[] patternChars;

    public ASTParser(String pattern) {
        this(pattern, false);
    }

    public ASTParser(String pattern, boolean charsetPostApply) {
        this.pattern = pattern;
        this.patternChars = pattern.toCharArray();
        parsePattern();
        root = optimize(root);
        if (!charsetPostApply) {
            charsetMap.apply();
        }
    }

    public String getPattern() {
        return pattern;
    }

    public Node getRoot() {
        return root;
    }

    public CharsetMap getCharsetMap() {
        return charsetMap;
    }

    /**
     * 创造正则抽象语法树，直接跳过词法分析步骤直接语法分析（因为正则太简单了）
     */
    private void parsePattern() {
        //临时栈，用于一些操作符的操作
        Deque<Node> parsedNodes = new ArrayDeque<>();
        //字符串索引
        int scanPos = 0;
        //统计括号对数量
        int parenthesesCount = 0;
        while (scanPos < patternChars.length) {
            switch (patternChars[scanPos]) {
                //(
                case LEFT_PARENTHESES:
                    parseLeftParentheses(parsedNodes);
                    parenthesesCount++;
                    break;
                //)
                case RIGHT_PARENTHESES:
                    parseRightParentheses(parsedNodes, scanPos);
                    parenthesesCount--;
                    break;
                //{ repeat
                case LEFT_BRACE:
                    scanPos = parseRepeatAndGetPos(parsedNodes, scanPos);
                    continue;
                    //'*'
                case KLEENE_CLOSURE:
                    //'+'
                case PLUS_CLOSURE:
                    //'?'
                case SELECT:
                    //'|'
                case OR:
                    parseUnaryOP(parsedNodes, scanPos);
                    break;
                //这些符号不应该出现在这里，因为他们会被相应的parser解析
                case RANGE:
                case RIGHT_BRACE:
                case RIGHT_BRACKET:
                    throw new RegexException(pattern, "Invalid operator '" + patternChars[scanPos] + "'", scanPos);
                    //'['，字符集
                case LEFT_BRACKET:
                default:
                    //读取下一个字符集
                    scanPos = parseCharsetAndGetPos(parsedNodes, scanPos);
                    continue;
            }
            scanPos++;
        }
        //括号对不匹配
        if (parenthesesCount != 0) {
            throw new RegexException(pattern, "'(' is not match to ')'", 0);
        }
        root = tryMergedCurrentNodes(parsedNodes);
    }

    /*
     * 尝试合并当前栈中的所有结点
     * 所谓合并，就是指栈中r1r2r3...rn n用一个Node来表示，即将这些结点作为Node的子结点
     * 注意：1. r1r2r3...rn在Node中的数据结构是List，由于栈的原因，这些结点是倒叙的，也就是rnr(n-1)...r1
     *      2. |结点会被处理，由于栈的原因，|结点的结合是右结合的
     */
    private Node tryMergedCurrentNodes(Deque<Node> parsedNodes) {
        Node or = mergeNodeWithTheOr(parsedNodes);
        if (or.getChildren().isEmpty()) {
            return EmptyNode.getInstance();
        } else if (or.getChildren().size() == 1) {
            return or.getChildren().get(0);
        } else {
            return or;
        }
    }

    private Node mergeNodeWithTheOr(Deque<Node> parsedNodes) {
        Node or = new OrNode();
        List<Node> currentParsedNodeList = new ArrayList<>();
        while (true) {
            Node pop = null;
            if (!parsedNodes.isEmpty()) pop = parsedNodes.pop();
            boolean isStop = parsedNodes.isEmpty() || parsedNodes.peek().getRegexType() == RegexType.LEFT_PARENTHESES;
            boolean isMarkedNode = pop != null && isMarkedOrNode(pop);
            boolean isNeedMerge = isStop || isMarkedNode;
            boolean isNormalNode = pop != null && !isMarkedNode;
            if (isNormalNode) {
                currentParsedNodeList.add(pop);
            }
            if (isNeedMerge) {
                mergeInto(or, currentParsedNodeList);
                currentParsedNodeList = new ArrayList<>();
            }
            if (isStop) break;
        }
        //检查最后的一个
        return or;
    }

    /*
     * 将currentParsedNodeList中保存的结点合并到or中
     */
    private void mergeInto(Node or, List<Node> currentParsedNodeList) {
        Node merged = new Node();
        //将当前检查过的结点合并到一个结点中
        merged.setChildren(currentParsedNodeList);
        //清空currentParsedNodeList，要用new不能直接clear，否则会造成多个node引用同一个list
        //加入合并的node
        or.getChildren().add(merged);
    }


    /*
     * 判断是否是标记的|结点（标记的|结点是指没有子结点的空|结点）
     */
    private boolean isMarkedOrNode(Node node) {
        return node.getRegexType() == RegexType.OR && node.getChildren().isEmpty();
    }


    /*
     * 分析字符集并返回分析后的位置
     */
    private int parseCharsetAndGetPos(Deque<Node> parsedNodes, int scanPos) {
        Result<List<CharsetNode>> charOrCharset = CharacterParser.nextCharOrCharset(patternChars, scanPos);
        List<CharsetNode> value = charOrCharset.getValue();
        if (value.size() == 1) {
            charsetMap.addNode(value.get(0));
            parsedNodes.push(value.get(0));
        } else {
            Node or = new OrNode();
            for (CharsetNode n : value) {
                charsetMap.addNode(n);
                or.getChildren().add(n);
            }
            parsedNodes.push(or);
        }
        return charOrCharset.getNextPos();
    }

    /*
     * 分析重复操作{m,n}并返回分析后的位置
     */
    private int parseRepeatAndGetPos(Deque<Node> parsedNodes, int scanPos) {
        if (parsedNodes.isEmpty()) {
            throw new RegexException(pattern, "Repeating operator need an element!", scanPos);
        }
        Result<RepeatNode> nextRepeat = RepeatParser.nextRepeat(patternChars, scanPos);
        nextRepeat.getValue().getChildren().add(parsedNodes.pop());
        parsedNodes.push(nextRepeat.getValue());
        return nextRepeat.getNextPos();
    }

    /*
     * 分析右括号(
     */
    private void parseRightParentheses(Deque<Node> parsedNodes, int scanPos) {
        Node node = tryMergedCurrentNodes(parsedNodes);
        //栈空都没遇到'('，说明缺少
        if (parsedNodes.isEmpty()) {
            throw new RegexException(pattern, "Missing '('", scanPos);
        }
        //弹出'('
        parsedNodes.pop();
        //压入连接的子结点
        parsedNodes.push(node);
    }

    /*
     * 分析左括号)
     */
    private void parseLeftParentheses(Deque<Node> parsedNodes) {
        parsedNodes.push(new Node(RegexType.LEFT_PARENTHESES));
    }

    /*
    * 处理一元操作符
     */
    private void parseUnaryOP(Deque<Node> parsedNodes, int scanPos) {
        char op = patternChars[scanPos];
        if (parsedNodes.isEmpty()) {
            throw new RegexException(pattern, "Invalid operator '" + op + "'", scanPos);
        }
        Node parsedNode;
        switch (op) {
            case KLEENE_CLOSURE:
                parsedNode = new KleeneNode(parsedNodes.pop());
                break;
            case PLUS_CLOSURE:
                parsedNode = new PlusNode(parsedNodes.pop());
                break;
            case SELECT:
                parsedNode = new SelectNode(parsedNodes.pop());
                break;
            case OR:
                parsedNode = new OrNode();
                break;
            default:
                throw new IllegalArgumentException("Unknown operator '" + op + "'");
        }
        parsedNodes.push(parsedNode);
    }


    private static Node optimize(Node root) {
        Node opt = optimizeRecursive(root);
        if (opt == null) {
            return EmptyNode.getInstance();
        }
        return opt;
    }

    /**
     * 优化一个AST树：
     * 1. 删除所有子结点为空的结点
     * 2. 单子结点合并
     *
     * @param root AST树的根结点
     * @return 优化后的AST树
     */
    private static Node optimizeRecursive(Node root) {
        Node newNode = root.clone();

        for (Node node : root.getChildren()) {
            Node opt = optimizeRecursive(node);
            if (opt != null) {
                newNode.getChildren().add(opt);
            }
        }

        /*if (newNode.getChildren().isEmpty() && newNode.getRegexType() == RegexType.NODE) {
            return null;
        } else */
        if (newNode.getChildren().size() == 1 && newNode.getRegexType() == RegexType.NODE) {
            return newNode.getChildren().get(0);
        }
        return newNode;
    }
}
