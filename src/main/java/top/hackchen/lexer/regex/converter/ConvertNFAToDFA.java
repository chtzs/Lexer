package top.hackchen.lexer.regex.converter;

import top.hackchen.lexer.regex.common.state.Condition;
import top.hackchen.lexer.regex.common.state.State;
import top.hackchen.lexer.regex.dfa.DFAGraph;
import top.hackchen.lexer.regex.nfa.NFAGraph;

import java.util.*;

/**
 * @author 陈浩天
 * @date 2021/9/15 12:17 星期三
 */
public class ConvertNFAToDFA {
    private final NFAGraph graph;

    public ConvertNFAToDFA(NFAGraph graph) {
        this.graph = graph;
    }

    public DFAGraph apply() {
        DFAGraph dfaGraph = new DFAGraph();
        //获取初始闭包
        Set<State> beginStates = graph.epsilonClosure(Collections.singleton(graph.getBeginState()));
        //获取全部输入条件
        Set<Condition> conditions = new HashSet<>(graph.getConditions());
        conditions.remove(Condition.EPSILON);
        //对照表
        Map<Set<State>, State> map = new HashMap<>();
        //产生的集合栈
        Deque<Set<State>> stack = new ArrayDeque<>();
        int identity = 0;

        dfaGraph.setBeginState(new State(identity++));
        map.put(beginStates, dfaGraph.getBeginState());
        stack.push(beginStates);

        while (!stack.isEmpty()) {
            Set<State> currStates = stack.pop();
            State state = map.get(currStates);
            List<State> finalStates = new ArrayList<>();
            for (State s : currStates) {
                if (s.isFinalState()) {
                    finalStates.add(s);
                }
            }
            finalStates.sort(Comparator.comparingInt(State::getType));
            if (!finalStates.isEmpty()) {
                state.setFinalState(true);
                state.setType(finalStates.get(0).getType());
                dfaGraph.addFinalState(state);
            }
            /*if(graph.containFinal(currStates)){
                state.setFinalState(true);
                dfaGraph.addFinalState(state);
            }*/
            for (Condition condition : conditions) {
                Set<State> nextNFAStates = graph.move(currStates, condition);
                if (nextNFAStates.isEmpty()) {
                    continue;
                }
                State nextState;
                if (!map.containsKey(nextNFAStates)) {
                    stack.push(nextNFAStates);
                    nextState = new State(identity++);
                    map.put(nextNFAStates, nextState);
                } else {
                    nextState = map.get(nextNFAStates);
                }
                //System.out.println(state + "---" + condition.getValue() + "--->" + nextState);
                dfaGraph.connect(state, nextState, condition.getValue());
            }
        }
        return dfaGraph;
    }
}
