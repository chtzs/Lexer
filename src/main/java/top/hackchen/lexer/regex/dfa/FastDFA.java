package top.hackchen.lexer.regex.dfa;

import top.hackchen.lexer.regex.common.state.State;

import java.util.Arrays;
import java.util.Map;

/**
 * @author 陈浩天
 * @date 2021/9/29 12:00 星期三
 */
public class FastDFA {
    public final int[][] transition;
    public int beginState;
    public final boolean[] isFinalState;
    public final int[] finalStateType;

    public FastDFA(int conditionCount, int stateCount) {
        transition = new int[stateCount][conditionCount];
        isFinalState = new boolean[stateCount];
        finalStateType = new int[stateCount];
        for (int[] next : transition) {
            Arrays.fill(next, -1);
        }
        Arrays.fill(finalStateType, -1);
    }

    public void connect(int stateA, int stateB, int condition) {
        transition[stateA][condition] = stateB;
    }

    public static FastDFA fromDFA(DFAGraph dfaGraph) {
        FastDFA res = new FastDFA(dfaGraph.conditions.size(), dfaGraph.states.size());
        for (int i = 0; i < dfaGraph.conditions.size(); i++) {
            for (Map.Entry<State, State> keyValue : dfaGraph.conditionToState[i].entrySet()) {
                res.connect(keyValue.getKey().getIdentity(), keyValue.getValue().getIdentity(), i);
            }
        }
        res.beginState = dfaGraph.beginState.getIdentity();
        for (State finalState : dfaGraph.finalStates) {
            res.isFinalState[finalState.getIdentity()] = true;
            res.finalStateType[finalState.getIdentity()] = finalState.getType();
        }
        return res;
    }
}
