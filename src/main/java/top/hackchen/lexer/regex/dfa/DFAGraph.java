package top.hackchen.lexer.regex.dfa;

import top.hackchen.lexer.regex.common.state.State;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author 陈浩天
 * @date 2021/9/27 21:46 星期一
 */
public class DFAGraph {
    protected State beginState;
    protected Set<State> finalStates = new HashSet<>();
    protected final Map<State, State>[] conditionToState = new HashMap[65536];
    protected Set<State> states = new HashSet<>();
    protected Set<Integer> conditions = new HashSet<>();

    public State getBeginState() {
        return beginState;
    }

    public void setBeginState(State beginState) {
        states.add(beginState);
        this.beginState = beginState;
    }

    public void addFinalState(State finalState) {
        finalState.setFinalState(true);
        states.add(finalState);
        finalStates.add(finalState);
    }

    public void addAllFinalState(Set<State> finalStates) {
        this.finalStates.addAll(finalStates);
    }

    public void removeFinalState(State finalState) {
        finalStates.remove(finalState);
    }

    public Set<State> getFinalStates() {
        return finalStates;
    }

    public Set<State> getStates() {
        return states;
    }

    public boolean containFinal(State dfaState) {
        return finalStates.contains(dfaState);
    }

    public Set<Integer> getConditions() {
        return conditions;
    }

    public void connect(State a, State b, Integer edgeValue) {
        if (conditionToState[edgeValue] == null) {
            conditionToState[edgeValue] = new HashMap<>();
        }
        conditionToState[edgeValue].put(a, b);
        conditions.add(edgeValue);
        states.add(a);
        states.add(b);
    }

    public State move(State currState, Integer condition) {
        if (conditionToState[condition] != null && conditionToState[condition].containsKey(currState)) {
            return conditionToState[condition].get(currState);
        } else {
            return null;
        }
    }
}
