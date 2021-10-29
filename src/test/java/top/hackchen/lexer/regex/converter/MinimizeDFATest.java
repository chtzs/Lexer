package top.hackchen.lexer.regex.converter;

import org.junit.Test;
import top.hackchen.lexer.regex.ast.ASTParser;
import top.hackchen.lexer.regex.common.CharsetMap;
import top.hackchen.lexer.regex.common.state.State;
import top.hackchen.lexer.regex.dfa.DFAGraph;
import top.hackchen.lexer.regex.nfa.NFAGraph;

/**
 * @author 陈浩天
 * @date 2021/9/28 14:16 星期二
 */
public class MinimizeDFATest {

    @Test
    public void apply() {
        String pattern = "([0-9])|([1-9][0-9]*)(\\.[0-9]*)?([eE][\\-+]?([0-9])|([1-9][0-9]*))?";
        ASTParser astParser = new ASTParser(pattern);
        CharsetMap charsetMap = astParser.getCharsetMap();
        NFAGraph nfaGraph = new ConvertASTToNFA(astParser.getCharsetMap(), astParser.getRoot()).apply();
        DFAGraph dfaGraph = new DFAGraph();//new ConvertNFAToDFA().apply(nfaGraph);
        State a = new State(0);
        State b = new State(1);
        State c = new State(2);
        State d = new State(3);
        State e = new State(4);
        State f = new State(5);
        dfaGraph.connect(a, b, 0);
        dfaGraph.connect(a, c, 1);
        dfaGraph.connect(b, d, 1);
        dfaGraph.connect(b, a, 0);
        dfaGraph.connect(c, f, 1);
        dfaGraph.connect(c, e, 0);
        dfaGraph.connect(d, f, 1);
        dfaGraph.connect(d, e, 0);
        dfaGraph.connect(e, e, 0);
        dfaGraph.connect(e, f, 1);
        dfaGraph.connect(f, f, 0);
        dfaGraph.connect(f, f, 1);
        dfaGraph.setBeginState(a);
        dfaGraph.addFinalState(c);
        dfaGraph.addFinalState(d);
        dfaGraph.addFinalState(e);
        MinimizeDFA minimizeDFA = new MinimizeDFA(dfaGraph);
        DFAGraph apply = minimizeDFA.apply();
        System.out.println("success");
    }
}