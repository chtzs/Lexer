package top.hackchen.lexer.regex.nfa;

import top.hackchen.lexer.regex.common.state.Condition;
import top.hackchen.lexer.regex.common.state.State;

import java.util.*;

/**
 * @author 陈浩天
 * @date 2021/9/27 21:40 星期一
 */
public class NFAGraph {
    protected State beginState;
    protected Set<State> finalStates = new HashSet<>();
    protected final Map<Condition, Map<State, Set<State>>> conditionToState = new HashMap<>();
    protected Set<State> states = new HashSet<>();

    public Set<State> getStates() {
        return states;
    }

    public Set<Condition> getConditions() {
        return conditionToState.keySet();
    }

    public State getBeginState() {
        return beginState;
    }

    public void setBeginState(State beginState) {
        this.beginState = beginState;
        states.add(beginState);
    }

    public Set<State> getFinalStates() {
        return finalStates;
    }

    public void addFinalState(State finalState) {
        finalStates.add(finalState);
        states.add(finalState);
    }

    public void addAllFinalStates(Set<State> finalStates) {
        this.finalStates.addAll(finalStates);
        states.addAll(finalStates);
    }

    public void removeFinalState(State finalState) {
        finalStates.remove(finalState);
    }

    public void removeAllFinalStates(Set<State> finalStates) {
        this.finalStates.removeAll(finalStates);
    }

    public boolean containFinal(Set<State> nfaStates) {
        for (State finalState : finalStates) {
            if (nfaStates.contains(finalState)) {
                return true;
            }
        }
        return false;
    }

    public void connect(State a, State b, Condition edgeValue) {
        if (!conditionToState.containsKey(edgeValue)) {
            conditionToState.put(edgeValue, new HashMap<>());
        }
        Map<State, Set<State>> stateMap = conditionToState.get(edgeValue);
        if (!stateMap.containsKey(a)) {
            stateMap.put(a, new HashSet<>());
        }

        states.add(a);
        states.add(b);
        stateMap.get(a).add(b);
    }

    public void add(NFAGraph other) {
        for (Map.Entry<Condition, Map<State, Set<State>>> entry : other.conditionToState.entrySet()) {
            Map<State, Set<State>> stateMap = conditionToState.getOrDefault(entry.getKey(), new HashMap<>());
            stateMap.putAll(entry.getValue());
            conditionToState.put(entry.getKey(), stateMap);
        }
        states.addAll(other.states);
    }

    public void fastMerge(NFAGraph other) {
        add(other);
        for (State finalState : finalStates) {
            finalState.setFinalState(false);
            connect(finalState, other.beginState, Condition.EPSILON);
        }
        this.finalStates = other.finalStates;
    }

    public void fastMergeAndKeepFinalStates(NFAGraph other) {
        add(other);
        for (State finalState : finalStates) {
            connect(finalState, other.beginState, Condition.EPSILON);
        }
        this.finalStates.addAll(other.finalStates);
    }

    public Set<State> move(Set<State> currStates, Condition condition) {
        Set<State> res = new HashSet<>();
        for (State currState : currStates) {
            Set<State> relevantState =
                    conditionToState.getOrDefault(condition, Collections.emptyMap())
                            .getOrDefault(currState, Collections.emptySet());
            res.addAll(relevantState);
        }
        return epsilonClosure(res);
    }

    public Set<State> closureAndMove(Set<State> currStates, Condition condition) {
        Set<State> newStates = epsilonClosure(currStates);
        return move(newStates, condition);
    }

    public Set<State> epsilonClosure(Set<State> currStates) {
        Set<State>[] product = new HashSet[2];
        Set<State> res = new HashSet<>(currStates);
        int curr = 0;
        product[0] = new HashSet<>(currStates);
        product[1] = new HashSet<>();

        int size;
        do {
            size = res.size();
            for (State state : product[curr]) {
                Set<State> relevantState =
                        conditionToState.getOrDefault(Condition.EPSILON, Collections.emptyMap())
                                .getOrDefault(state, Collections.emptySet());
                res.addAll(relevantState);
                product[1 - curr].addAll(relevantState);
            }
            product[curr].clear();
            curr = 1 - curr;
        } while (size != res.size());
        return res;
    }
}
