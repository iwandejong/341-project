package FA;
import java.util.List;

import Parser.*;
import java.util.ArrayList;

public class FA {
    public State root;
    public Symbol identifier;

    public FA(ProductionRule _pr, int stateCount, Symbol _identifier) {
        identifier = _identifier;

        State curr = new State(stateCount);
        root = curr;
        root.startState = true;
        for (int i = 0; i < _pr.rhs.size(); i++) {
            State next = new State(stateCount + i + 1);
            if (i == _pr.rhs.size() - 1) {
                next.accepting = true;
            }
            curr.addTransition(_pr.rhs.get(i), next);
            curr = next;
        }
    }

    // find transitions from a state with a symbol
    public List<State> findTransitions(Symbol sym) {
        List<State> states = new ArrayList<State>();

        if (identifier.identifier.equals(sym.identifier)) {
            states.add(root);
        }

        findTransitionsHelper(root, sym, states);
        return states;
    }

    public void findTransitionsHelper(State s, Symbol sym, List<State> states) {
        if (s == null) {
            return;
        }
        for (Symbol s1 : s.transitions.keySet()) {
            if (s1.identifier.equals(sym.identifier)) {
                states.add(s.transitions.get(s1));
            }
            findTransitionsHelper(s.transitions.get(s1), sym, states);
        }
        return;
    }

    // find transitions to a state with a symbol
    public List<State> findFromTransitions(Symbol sym) {
        List<State> states = new ArrayList<State>();

        findFromTransitionsHelper(root, sym, states);
        return states;
    }

    public void findFromTransitionsHelper(State s, Symbol sym, List<State> states) {
        if (s == null) {
            return;
        }
        for (Symbol s1 : s.transitions.keySet()) {
            if (s1.identifier.equals(sym.identifier)) {
                states.add(s);
            }
            findFromTransitionsHelper(s.transitions.get(s1), sym, states);
        }
        return;
    }

    public List<State> getAllStates() {
        List<State> states = new ArrayList<State>();
        getAllStatesHelper(root, states);
        return states;
    }

    public void getAllStatesHelper(State s, List<State> states) {
        if (s == null) {
            return;
        }
        states.add(s);
        for (Symbol sym : s.transitions.keySet()) {
            getAllStatesHelper(s.transitions.get(sym), states);
        }
    }

    // print dfa such like this: from - symbol -> to
    public String printFA(State s) {
        if (s == null) {
            return "";
        }
        for (Symbol sym : s.transitions.keySet()) {
            String temp = printFA(s.transitions.get(sym));
            if (temp.equals("")) {
                return s.printState() + " -[" + sym.identifier + "]-> " + s.transitions.get(sym).printState();
            }
            return s.printState() + " -[" + sym.identifier + "]-> " + temp;
        }
        return "";
    }

    // raw-print transitions
    public void printTransitions(State s) {
        if (s == null) {
            return;
        }

        for (Symbol sym : s.transitions.keySet()) {
            System.out.println(s.printState() + " - " + sym.identifier + " -> " + s.transitions.get(sym).printState());
            printTransitions(s.transitions.get(sym));
        }
    }
}
