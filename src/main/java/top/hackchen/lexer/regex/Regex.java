package top.hackchen.lexer.regex;

import top.hackchen.lexer.regex.ast.ASTParser;
import top.hackchen.lexer.regex.common.CharsetMap;
import top.hackchen.lexer.regex.common.state.Condition;
import top.hackchen.lexer.regex.common.state.State;
import top.hackchen.lexer.regex.converter.ConvertASTToNFA;
import top.hackchen.lexer.regex.converter.ConvertNFAToDFA;
import top.hackchen.lexer.regex.converter.MinimizeDFA;
import top.hackchen.lexer.regex.dfa.DFAGraph;
import top.hackchen.lexer.regex.dfa.FastDFA;
import top.hackchen.lexer.regex.nfa.NFAGraph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 陈浩天
 * @date 2021/9/27 22:01 星期一
 */
public class Regex {
    protected NFAGraph nfaGraph;
    protected FastDFA fastDFA;
    protected DFAGraph minimized;
    protected DFAGraph dfaGraph;
    protected CharsetMap charsetMap;

    private Regex(String pattern) {
        ASTParser astParser = new ASTParser(pattern);
        charsetMap = astParser.getCharsetMap();
        nfaGraph = new ConvertASTToNFA(astParser.getCharsetMap(), astParser.getRoot()).apply();
        dfaGraph = new ConvertNFAToDFA(nfaGraph).apply();
        MinimizeDFA minimizeDFA = new MinimizeDFA(dfaGraph);
        minimized = minimizeDFA.apply();
        fastDFA = minimizeDFA.apply2();
    }

    public static Regex compile(String pattern) {
        return new Regex(pattern);
    }

    public boolean nfaMatch(String text) {
        Set<State> states = nfaGraph.epsilonClosure(new HashSet<>(Collections.singleton(nfaGraph.getBeginState())));
        for (int i = 0, len = text.length(); i < len; i++) {
            if (states.size() == 0) return false;
            states = nfaGraph.move(states, new Condition(charsetMap.getNumber(text.charAt(i))));
        }
        states.retainAll(nfaGraph.getFinalStates());
        return states.size() > 0;
    }

    public boolean dfaMatch(String text) {
        return dfaMatch(text, dfaGraph);
    }

    public boolean minimizedMatch(String text) {
        return dfaMatch(text, minimized);
    }

    private boolean dfaMatch(String text, DFAGraph dfa) {
        State state = dfa.getBeginState();
        int charIndex;
        for (char c : text.toCharArray()) {
            if (state == null) return false;
            charIndex = charsetMap.charToNumber[c];
            if (charIndex < 0) return false;
            state = dfa.move(state, charsetMap.getNumber(c));
        }
        if (state == null) return false;
        return dfa.containFinal(state);
    }

    public boolean fastDfaMatch(String text) {
        int state = fastDFA.beginState;
        int charIndex;
        for (char c : text.toCharArray()) {
            charIndex = charsetMap.charToNumber[c];
            if (charIndex < 0) return false;
            state = fastDFA.transition[state][charIndex];
            if (state < 0) return false;
        }
        return fastDFA.isFinalState[state];
    }

    public boolean isMatch(String text) {
        return fastDfaMatch(text);
    }
}
