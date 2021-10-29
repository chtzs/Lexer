package top.hackchen.lexer.regex.converter;


import top.hackchen.lexer.regex.ast.node.CharsetNode;
import top.hackchen.lexer.regex.ast.node.Node;
import top.hackchen.lexer.regex.ast.node.RepeatNode;
import top.hackchen.lexer.regex.common.CharsetMap;
import top.hackchen.lexer.regex.common.state.Condition;
import top.hackchen.lexer.regex.common.state.State;
import top.hackchen.lexer.regex.nfa.NFAGraph;

import java.util.List;
import java.util.Set;

/**
 * @author 陈浩天
 * @date 2021/9/14 15:02 星期二
 */
public class ConvertASTToNFA {
    private int identify = 0;
    private final CharsetMap charsetMap;
    private final Node astRoot;
    private final int baseIdentify;

    public ConvertASTToNFA(CharsetMap charsetMap, Node astRoot) {
        this(charsetMap, astRoot, 0);
    }

    public ConvertASTToNFA(CharsetMap charsetMap, Node astRoot, int baseIdentify) {
        this.charsetMap = charsetMap;
        this.astRoot = astRoot;
        this.baseIdentify = baseIdentify;
    }

    public NFAGraph apply() {
        identify = baseIdentify;
        return applyRecursive(astRoot);
    }

    public int getIdentify() {
        return identify;
    }

    private State getNewState() {
        return new State(identify++);
    }

    private State getNewFinalState() {
        return new State(true, identify++);
    }

    private NFAGraph applyRecursive(Node astRoot) {
        NFAGraph res = new NFAGraph();
        List<Node> children = astRoot.getChildren();

        switch (astRoot.getRegexType()) {
            case NODE: {
                State beginState = getNewState();
                State finalState = getNewFinalState();
                res.setBeginState(beginState);
                res.addFinalState(finalState);
                res.connect(beginState, finalState, Condition.EPSILON);
                if (children.size() > 0) {
                    res = applyRecursive(children.get(children.size() - 1));
                    for (int i = children.size() - 2; i >= 0; i--) {
                        res.fastMerge(applyRecursive(children.get(i)));
                    }
                }
                return res;
            }

            case CHARSET: {
                State beginState = getNewState();
                State finalState = getNewFinalState();
                res.setBeginState(beginState);
                Set<Integer> identities = charsetMap.getReplacedNumbers((CharsetNode) astRoot);
                for (int i : identities) {
                    res.connect(beginState, finalState, new Condition(i));
                }
                res.addFinalState(finalState);

                return res;
            }

            case OR: {
                State beginState = getNewState();
                State finalState = getNewFinalState();
                res.setBeginState(beginState);
                res.addFinalState(finalState);
                if (children.isEmpty()) {
                    res.connect(beginState, finalState, Condition.EPSILON);
                }
                for (Node child : children) {
                    NFAGraph g = applyRecursive(child);
                    res.add(g);
                    res.connect(res.getBeginState(), g.getBeginState(), Condition.EPSILON);
                    res.addAllFinalStates(g.getFinalStates());
                }
                return res;
            }

            case SELECT: {
                NFAGraph child = applyRecursive(children.get(0));
                State beginState = getNewState();
                State finalState = getNewFinalState();
                res.setBeginState(beginState);
                res.addFinalState(finalState);
                res.add(child);
                res.connect(beginState, child.getBeginState(), Condition.EPSILON);
                res.connect(beginState, finalState, Condition.EPSILON);
                for (State f : child.getFinalStates()) {
                    res.connect(f, finalState, Condition.EPSILON);
                }
                return res;
            }

            case PLUS_CLOSURE: {
                NFAGraph child = kleeneClosure(children.get(0));
                NFAGraph repeat = applyRecursive(children.get(0));
                repeat.fastMerge(child);
                return repeat;
            }

            case KLEENE_CLOSURE: {
                return kleeneClosure(children.get(0));
            }
            //r{m, n}, 对r重复至少m次，最多n次
            case REPEAT: {
                State beginState = getNewState();
                State finalState = getNewFinalState();
                res.setBeginState(beginState);
                res.addFinalState(finalState);
                res.connect(beginState, finalState, Condition.EPSILON);
                RepeatNode repeatNode = (RepeatNode) astRoot;
                State preBegin = null;
                //连接前m个
                for (int i = 0; i < repeatNode.getFrom(); i++) {
                    NFAGraph child = applyRecursive(children.get(0));
                    preBegin = child.getBeginState();
                    res.fastMerge(child);
                }
                //形如r{m, }的表达式
                if (preBegin != null && repeatNode.getTo() < 0) {
                    State newFinal = getNewFinalState();
                    for (State f : res.getFinalStates()) {
                        res.connect(f, newFinal, Condition.EPSILON);
                    }
                    // 我知道我在做什么。本来states的状态集合就是多余的，只是为了调试方便而已。
                    // 因此没有在states中也移除终结结点的必要。
                    res.getFinalStates().clear();
                    res.addFinalState(newFinal);
                    res.connect(newFinal, preBegin, Condition.EPSILON);
                } else {
                    for (int i = repeatNode.getFrom(); i < repeatNode.getTo(); i++) {
                        NFAGraph child = applyRecursive(children.get(0));
                        res.fastMergeAndKeepFinalStates(child);
                    }
                }
                return res;
            }
            default:
                throw new RuntimeException("What happened?");
        }
    }

    private NFAGraph kleeneClosure(Node child) {
        NFAGraph childGraph = applyRecursive(child);
        State finalState = getNewFinalState();
        for (State f : childGraph.getFinalStates()) {
            f.setFinalState(false);
            childGraph.connect(f, finalState, Condition.EPSILON);
        }
        childGraph.getFinalStates().clear();
        childGraph.addFinalState(finalState);
        childGraph.connect(finalState, childGraph.getBeginState(), Condition.EPSILON);
        childGraph.connect(childGraph.getBeginState(), finalState, Condition.EPSILON);
        return childGraph;
    }
}
