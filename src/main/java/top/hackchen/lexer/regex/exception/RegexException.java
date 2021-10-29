package top.hackchen.lexer.regex.exception;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * @author 陈浩天
 * @date 2021/9/9 21:31 星期四
 */
public class RegexException extends RuntimeException {
    public RegexException(String regex, String cause, int position) {
        super(errorMsg(regex, cause, position));
    }

    private static String errorMsg(String regex, String cause, int position) {
        int length = 0;
        try {
            length = new String(regex.substring(0, position).getBytes("GBK"), StandardCharsets.ISO_8859_1).length();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();
        //builder.append("Exception happened when parse regex: ");
        builder.append(cause);
        builder.append('\n');
        builder.append("At line 1, ");
        builder.append(position);
        builder.append('\n');
        builder.append(regex);
        builder.append('\n');
        for (int i = 0; i < length; i++) {
            builder.append(' ');
        }
        builder.append('^');
        return builder.toString();
    }
}
