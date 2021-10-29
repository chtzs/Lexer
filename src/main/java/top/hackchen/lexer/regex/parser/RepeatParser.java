package top.hackchen.lexer.regex.parser;

import top.hackchen.lexer.regex.ast.node.RepeatNode;
import top.hackchen.lexer.regex.common.Result;
import top.hackchen.lexer.regex.exception.RegexException;

import static top.hackchen.lexer.regex.common.constant.RegexConstant.*;

/**
 * @author 陈浩天
 * @date 2021/10/1 17:10 星期五
 */
public class RepeatParser {
    //手撸的状态机，图都没画，正确是正确的，就是不知道该怎么解释...
    public static Result<RepeatNode> nextRepeat(char[] str, int begin) {
        begin++;
        //{(s*, 0)([0-9]+, 1)(s*, 2)(,, 3)(s*, 4)([0-9]+, 5)(s*, 6)}
        int state = 0;
        int number = 0;
        int from = 0, to = -1;
        while (begin < str.length) {
            switch (str[begin]) {
                case ' ':
                    if (state == 1) {
                        from = number;
                        number = 0;
                        state = 2;
                    } else if (state == 5) {
                        to = number;
                        state = 6;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if (state == 0) {
                        state = 1;
                    } else if (state == 4) {
                        state = 5;
                    }
                    if (state == 1 || state == 5) {
                        number *= 10;
                        number += str[begin] - '0';
                    } else {
                        throw new RegexException(new String(str), "Illegal digit!", begin);
                    }
                    break;
                case COMMA:
                    if (state == 1 || state == 2 || state == 0) {
                        state = 4;
                        from = number;
                        number = 0;
                    } else {
                        throw new RegexException(new String(str), "Illegal ','!", begin);
                    }
                    break;
                case RIGHT_BRACE:
                    if (state == 0) {
                        throw new RegexException(new String(str), "Empty repeat body!", begin);
                    } else if (state == 5 || state == 6) {
                        to = number;
                    } else if (state == 1 || state == 2) {
                        to = from = number;
                    }
                    RepeatNode res = new RepeatNode(from, to);
                    return new Result<>(begin + 1, res);
                default:
                    throw new RegexException(new String(str), "Illegal character '" + str[begin] + "'!", begin);
            }
            begin++;
        }
        throw new RegexException(new String(str), "Missing '" + RIGHT_BRACE + "'", begin);
    }
}
