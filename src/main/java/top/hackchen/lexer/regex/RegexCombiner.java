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

import java.util.ArrayList;
import java.util.List;

/**
 * @author 陈浩天
 * @date 2021/10/9 21:07 星期六
 */
public class RegexCombiner {
    private static class RegexAndType {
        String regex;
        int type;

        public RegexAndType(String regex, int type) {
            this.regex = regex;
            this.type = type;
        }
    }

    private final NFAGraph all;
    private int identity = 1;
    private final State begin;
    private final List<RegexAndType> regexAndTypes = new ArrayList<>();
    private CharsetMap charsetMap;

    public RegexCombiner() {
        all = new NFAGraph();
        begin = new State(0);
        all.setBeginState(begin);
    }

    public void add(String regex, int type) {
        regexAndTypes.add(new RegexAndType(regex, type));
    }

    private void buildNFA() {
        List<ASTParser> astParserList = new ArrayList<>();
        charsetMap = new CharsetMap();
        for (RegexAndType regexAndType : regexAndTypes) {
            ASTParser astParser = new ASTParser(regexAndType.regex, true);
            astParserList.add(astParser);
            charsetMap.addMap(astParser.getCharsetMap());
        }
        //合并成一个巨大的charsetMap
        charsetMap.apply();
        for (int i = 0; i < astParserList.size(); i++) {
            ConvertASTToNFA convertASTToNFA = new ConvertASTToNFA(charsetMap, astParserList.get(i).getRoot(), identity);
            NFAGraph nfaGraph = convertASTToNFA.apply();
            for (State state : nfaGraph.getFinalStates()) {
                state.setType(regexAndTypes.get(i).type);
            }
            identity = convertASTToNFA.getIdentify();

            all.addAllFinalStates(nfaGraph.getFinalStates());
            all.add(nfaGraph);
            all.connect(begin, nfaGraph.getBeginState(), Condition.EPSILON);
        }
    }

    public FastDFA apply() {
        buildNFA();
        if (all.getFinalStates().isEmpty()) {
            throw new RuntimeException("Shit");
        }
        DFAGraph dfaGraph = new ConvertNFAToDFA(all).apply();
        DFAGraph minimizeDFA = new MinimizeDFA(dfaGraph).apply();
        return FastDFA.fromDFA(minimizeDFA);
    }

    public CharsetMap getCharsetMap() {
        return charsetMap;
    }
}
