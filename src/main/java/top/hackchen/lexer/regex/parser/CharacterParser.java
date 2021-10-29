package top.hackchen.lexer.regex.parser;

import top.hackchen.lexer.regex.common.Result;
import top.hackchen.lexer.regex.exception.RegexException;
import top.hackchen.lexer.regex.ast.node.CharsetNode;

import java.util.*;

import static top.hackchen.lexer.regex.common.constant.RegexConstant.*;

/**
 * 字符解析器
 *
 * @author 陈浩天
 * @date 2021/9/9 19:17 星期四
 */
public class CharacterParser {
    private static final char[] INVALID_CHARS = {'[', '(', ')', '*', '{', '}'};
    private static final Set<Character> INVALID_SET = new HashSet<>();
    private static final String WORD_REGEX = "[0-9a-zA-Z_]";
    private static final String WORD_REGEX_R = "[^0-9a-zA-Z_]";
    private static final String DIGIT_REGEX = "[0-9]";
    private static final String DIGIT_REGEX_R = "[^0-9]";
    private static final String SPACE_REGEX = "[ \\t\\n\\r\\f\\u000B]";
    private static final String SPACE_REGEX_R = "[^ \\t\\n\\r\\f\\u000B]";
    private static final List<CharsetNode> WORD;
    private static final List<CharsetNode> WORD_R;
    private static final List<CharsetNode> DIGIT;
    private static final List<CharsetNode> DIGIT_R;
    private static final List<CharsetNode> SPACE;
    private static final List<CharsetNode> SPACE_R;

    static {
        for (char c : INVALID_CHARS) {
            INVALID_SET.add(c);
        }
        WORD = nextCharOrCharset(WORD_REGEX.toCharArray(), 0).getValue();
        WORD_R = nextCharOrCharset(WORD_REGEX_R.toCharArray(), 0).getValue();
        DIGIT = nextCharOrCharset(DIGIT_REGEX.toCharArray(), 0).getValue();
        DIGIT_R = nextCharOrCharset(DIGIT_REGEX_R.toCharArray(), 0).getValue();
        SPACE = nextCharOrCharset(SPACE_REGEX.toCharArray(), 0).getValue();
        SPACE_R = nextCharOrCharset(SPACE_REGEX_R.toCharArray(), 0).getValue();
    }

    private static boolean isHex(char c) {
        return Character.isDigit(c) || ('a' <= c && c <= 'f');
    }

    private static int getHex(char c) {
        if (Character.isDigit(c)) {
            return c - '0';
        } else {
            return 10 + (c - 'a');
        }
    }

    private static Result<CharsetNode> nextSingleCharWithEscape(char[] str, int begin) {
        char res;
        switch (str[begin]) {
            case 'n':
                res = '\n';
                break;
            case 't':
                res = '\t';
                break;
            case 'r':
                res = '\r';
                break;
            case 'f':
                res = '\f';
                break;
            /*case LEFT_BRACE:
            case LEFT_BRACKET:
            case LEFT_PARENTHESES:
            case KLEENE_CLOSURE:
            case RIGHT_BRACE:
            case RIGHT_BRACKET:
            case RIGHT_PARENTHESES:
            case ANY:
            case RANGE:
            case PLUS_CLOSURE:
            case OR:
            case ESCAPE:
                res = str[begin];
                break;*/
            case 'u':
                begin++;
                if (begin + 4 >= str.length) {
                    throw new RegexException(new String(str), "Illegal '\\u'!", begin);
                }
                int sum = 0;
                for (int end = begin + 4; begin < end; begin++) {
                    char c = Character.toLowerCase(str[begin]);
                    if (isHex(c)) {
                        sum <<= 4;
                        sum += getHex(c);
                    } else {
                        throw new RegexException(new String(str), "Illegal hex number!", begin);
                    }
                }
                res = (char) sum;
                begin--;
                break;
            default:
                res = str[begin];
        }
        return new Result<>(begin + 1, new CharsetNode(res));
    }

    private static Result<List<CharsetNode>> nextCharsetWithEscape(char[] str, int begin) {
        List<CharsetNode> res;
        switch (str[begin]) {
            case 's':
                res = SPACE;
                break;
            case 'S':
                res = SPACE_R;
                break;
            case 'w':
                res = WORD;
                break;
            case 'W':
                res = WORD_R;
                break;
            case 'd':
                res = DIGIT;
                break;
            case 'D':
                res = DIGIT_R;
                break;
            default:
                return null;
        }
        return new Result<>(begin + 1, res);
    }

    public static Result<List<CharsetNode>> nextCharOrCharset(char[] str, int begin) {
        if (str[begin] == LEFT_BRACKET) {
            return nextCharset(str, begin + 1);
        } else if (str[begin] == ESCAPE) {
            Result<List<CharsetNode>> re0 = nextCharsetWithEscape(str, begin + 1);
            if (re0 != null) {
                return re0;
            }
            Result<CharsetNode> re1 = nextSingleCharWithEscape(str, begin + 1);
            Result<List<CharsetNode>> res;
            res = new Result<>(re1.getNextPos(), Collections.singletonList(re1.getValue()));
            return res;
        } else if (str[begin] == ANY) {
            return new Result<>(begin + 1, Collections.singletonList(new CharsetNode(0, 65535)));
        } else {
            return new Result<>(begin + 1, Collections.singletonList(new CharsetNode(str[begin])));
        }
    }

    private static Result<CharsetNode> nextSingleChar(char[] str, int begin) {
        if (str[begin] == ESCAPE) {
            return nextSingleCharWithEscape(str, begin + 1);
        } else if (str[begin] == ANY) {
            return new Result<>(begin + 1, new CharsetNode(0, 65535));
        }
        return new Result<>(begin + 1, new CharsetNode(str[begin]));
    }

    private static Result<List<CharsetNode>> nextCharset(char[] str, int begin) {
        Deque<CharsetNode> stack = new ArrayDeque<>();
        boolean reversed = false;
        //如果字符集翻转就设置翻转
        if (str[begin] == REVERSE) {
            reversed = true;
            begin++;
        }
        CharsetNode n;

        while (begin < str.length && str[begin] != RIGHT_BRACKET) {
            if (str[begin] == RANGE) {
                //空栈肯定是不行的
                if (stack.isEmpty()) {
                    throw new RegexException(new String(str), "operator '-' missing first char", begin);
                }
                //-后面没东西是不行的
                if (begin == str.length - 1 || str[begin + 1] == RIGHT_BRACKET) {
                    throw new RegexException(new String(str), "operator '-' missing second char", begin);
                }
                //-后面还是-是不行的
                if (str[begin + 1] == RANGE) {
                    throw new RegexException(new String(str), "the use of operator '-' is invalid", begin);
                }
                //读取-后面的字符
                Result<CharsetNode> next = nextSingleChar(str, begin + 1);
                CharsetNode first = stack.pop();
                CharsetNode second = next.getValue();

                //前面的字符比后面大是不行的
                if (first.getBegin() > second.getBegin()) {
                    throw new RegexException(new String(str),
                            "the range between " + first.getBegin() + ", " + second.getBegin()
                                    + "is reversed", begin);
                }
                //入结果栈
                n = new CharsetNode(first.getBegin(), second.getBegin());
                begin = next.getNextPos();
            } else {
                Result<CharsetNode> next = nextSingleChar(str, begin);
                n = next.getValue();
                begin = next.getNextPos();
            }
            stack.push(n);
        }

        List<CharsetNode> res = new ArrayList<>();
        List<CharsetNode> list = new ArrayList<>(stack);
        if (reversed) {
            res.addAll(getReversedCharset(list));
        } else {
            res.addAll(list);
        }

        //判断方右括号是否存在
        if (begin == str.length && str[begin - 1] != RIGHT_BRACKET) {
            throw new RegexException(new String(str), "']' is missing", begin - 1);
        }
        return new Result<>(begin + 1, res);
    }

    private static List<CharsetNode> getReversedCharset(List<CharsetNode> charsetList) {
        List<CharsetNode> res = new ArrayList<>();
        Collections.sort(charsetList);
        CharsetNode rest = new CharsetNode(0, 65535);
        for (CharsetNode r : charsetList) {
            CharsetNode[] subtract = rest.subtract(r);
            if (!subtract[0].isEmpty()) {
                res.add(subtract[0]);
            }
            if (subtract[1].isEmpty()) {
                break;
            }
            rest = subtract[1];
        }

        if (!rest.isEmpty()) {
            res.add(rest);
        }
        return res;
    }
}
