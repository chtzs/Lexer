package top.hackchen.lexer.regex.converter;

import top.hackchen.lexer.regex.common.state.State;
import top.hackchen.lexer.regex.dfa.DFAGraph;
import top.hackchen.lexer.regex.dfa.FastDFA;

import java.util.*;

/**
 * @author 陈浩天
 * @date 2021/9/28 10:22 星期二
 */
public class MinimizeDFA {
    private final DFAGraph origin;

    public MinimizeDFA(DFAGraph origin) {
        this.origin = origin;
    }

    //采用Hopcroft算法对DFA进行最小化运算
    public DFAGraph apply() {
        DFAGraph dfa = new DFAGraph();
        List<Set<State>> newState = splitStates();
        Set<Integer> conditions = origin.getConditions();
        State[] stateCache = new State[newState.size()];
        for (int i = 0; i < newState.size(); i++) {
            State temp = new State(i);
            stateCache[i] = temp;
            //只存在相同类型的终状态（参见splitFinalStates)
            Set<State> intersect = intersect(newState.get(i), origin.getFinalStates());
            if (!intersect.isEmpty()) {
                temp.setType(intersect.iterator().next().getType());
                dfa.addFinalState(temp);
            }
            if (newState.get(i).contains(origin.getBeginState())) {
                dfa.setBeginState(temp);
            }
        }

        for (int i = 0; i < newState.size(); i++) {
            for (Integer c : conditions) {
                State move = null;
                for (State s : newState.get(i)) {
                    move = origin.move(s, c);
                    if (move != null) break;
                }
                if (move == null) {
                    continue;
                }

                for (int j = 0; j < newState.size(); j++) {
                    if (newState.get(j).contains(move)) {
                        dfa.connect(stateCache[i], stateCache[j], c);
                        break;
                    }
                }
            }
        }
        return dfa;
    }

    //采用Hopcroft算法对DFA进行最小化运算
    public FastDFA apply2() {
        List<Set<State>> newState = splitStates();
        Set<Integer> conditions = origin.getConditions();
        State[] stateCache = new State[newState.size()];
        FastDFA dfa = new FastDFA(conditions.size(), newState.size());
        for (int i = 0; i < newState.size(); i++) {
            State temp = new State(i);
            stateCache[i] = temp;
            //只存在相同类型的终状态（参见splitFinalStates)
            Set<State> intersect = intersect(newState.get(i), origin.getFinalStates());
            if (!intersect.isEmpty()) {
                temp.setType(intersect.iterator().next().getType());
                dfa.isFinalState[temp.getIdentity()] = true;
                dfa.finalStateType[temp.getIdentity()] = temp.getType();
            }
            if (newState.get(i).contains(origin.getBeginState())) {
                dfa.beginState = temp.getIdentity();
            }
        }

        for (int i = 0; i < newState.size(); i++) {
            for (Integer c : conditions) {
                State move = null;
                for (State s : newState.get(i)) {
                    move = origin.move(s, c);
                    if (move != null) break;
                }
                if (move == null) {
                    continue;
                }

                for (int j = 0; j < newState.size(); j++) {
                    if (newState.get(j).contains(move)) {
                        dfa.transition[stateCache[i].getIdentity()][c] = stateCache[j].getIdentity();
                        break;
                    }
                }
            }
        }
        return dfa;
    }

    //hopcroft_algorithm
    private List<Set<State>> splitStates() {
        Set<State> all = origin.getStates();
        Set<State> copy = new HashSet<>(all);
        copy.removeAll(origin.getFinalStates());

        List<Set<State>> p = new ArrayList<>(splitFinalStates());
        p.add(new HashSet<>(copy));
        p.remove(Collections.EMPTY_SET);

        Set<Set<State>> w = new HashSet<>(splitFinalStates());
        w.add(new HashSet<>(copy));
        w.remove(Collections.EMPTY_SET);

        Set<Integer> conditions = origin.getConditions();

        while (!w.isEmpty()) {
            Set<State> a = w.iterator().next();
            w.remove(a);
            for (Integer c : conditions) {
                Set<State> x = getSourceFrom(origin, a, c);
                if (x.isEmpty()) continue;
                Set<Set<State>> temp = new HashSet<>();

                for (Set<State> y : p) {
                    Set<State> intersect = intersect(x, y);
                    Set<State> sub = subtract(y, x);
                    if (intersect.isEmpty() || sub.isEmpty()) {
                        temp.add(y);
                    } else {
                        temp.add(intersect);
                        temp.add(sub);

                        if (w.contains(y)) {
                            w.remove(y);
                            w.add(intersect);
                            w.add(sub);
                        } else {
                            if (intersect.size() <= sub.size()) {
                                w.add(intersect);
                            } else {
                                w.add(sub);
                            }
                        }
                    }
                }

                p = new ArrayList<>(temp);
            }
        }
        //System.out.println(p);
        return p;
    }

    //分割不同类型的终状态
    private Collection<Set<State>> splitFinalStates() {
        Map<Integer, Set<State>> finalStateMap = new HashMap<>();
        for (State state : origin.getFinalStates()) {
            Set<State> finalStateSet = finalStateMap.getOrDefault(state.getType(), new HashSet<>());
            finalStateSet.add(state);
            finalStateMap.put(state.getType(), finalStateSet);
        }
        return finalStateMap.values();
    }

    private static Set<State> getSourceFrom(DFAGraph dfa, Set<State> input, Integer condition) {
        Set<State> all = dfa.getStates();
        Set<State> res = new HashSet<>();
        for (State s : all) {
            State move = dfa.move(s, condition);
            if (input.contains(move)) {
                res.add(s);
            }
        }
        return res;
    }

    private static Set<State> move(DFAGraph dfa, Set<State> input, Integer condition) {
        Set<State> x = new HashSet<>();
        for (State s : input) {
            State move = dfa.move(s, condition);
            if (move != null) x.add(move);
        }
        return x;
    }

    private static Set<State> intersect(Set<State> a, Set<State> b) {
        Set<State> res = new HashSet<>();
        for (State s : a) {
            if (b.contains(s)) {
                res.add(s);
            }
        }
        return res;
    }

    private static Set<State> subtract(Set<State> a, Set<State> b) {
        Set<State> res = new HashSet<>(a);
        res.removeAll(b);
        return res;
    }
}
