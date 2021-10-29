import java.util.*;

%%
%public
%class TestParser
%console

%{
    public static enum ParserType {
        //标识符
        IDENTIFIER, 
        //赋值
        EQUALS,
        //分号
        SEMICOLON,
        //或者
        OR,
        //左括号
        LEFT_PARENTHESES,
        //右括号
        RIGHT_PARENTHESES,
        //引用模式开始
        REFERENCE_START,
        //引用模式结束
        REFERENCE_END,
        //分割符
        SPLIT,
        //转义
        ESCAPE,
        //选项
        OPTION
    }

    public static class Token {
        public ParserType type;
        public Object val;

        public Token(ParserType type, Object val) {
            this.type = type;
            this.val = val;
        }

        public Token(ParserType type) {
            this(type, null);
        }

        public String toString() {
            return "<" + type + (val == null ? "" : (" val: " + val.toString())) + ">";
        }
    }

    public List<Token> tokenList = new ArrayList<>();
}%

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
EndOfLineComment = "//"{{InputCharacter}}*{{LineTerminator}}
Identifier = [a-zA-Z_]\w*
Option = %{{Identifier}}

%%

{{EndOfLineComment}} {}
{{Identifier}} {
    tokenList.add(new Token(ParserType.IDENTIFIER, lexWord()));
}
"=" {
    tokenList.add(new Token(ParserType.EQUALS));
}
"|" {
    tokenList.add(new Token(ParserType.OR));
}
";" {
    tokenList.add(new Token(ParserType.SEMICOLON));
}
"\" {
    tokenList.add(new Token(ParserType.ESCAPE));
}
"%%" {
    tokenList.add(new Token(ParserType.SPLIT));
}
"(" {
    tokenList.add(new Token(ParserType.LEFT_PARENTHESES));
}
")" {
    tokenList.add(new Token(ParserType.RIGHT_PARENTHESES));
}
"{{" {
    tokenList.add(new Token(ParserType.REFERENCE_START));
}
"}}" {
    tokenList.add(new Token(ParserType.REFERENCE_END));
}
{{Option}} {
    tokenList.add(new Token(ParserType.OPTION, lexWord()));
}
$ {
    tokenList.forEach(System.out::println);
}
. {}