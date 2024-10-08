package FA;
import java.io.*;
import java.util.*;
import Parser.*;

public class State {
    public int identifier;
    public Map<Symbol, State> transitions = new HashMap<Symbol, State>();
    public boolean accepting = false;
    public boolean startState = false;

    public State(int _identifier) {
        identifier = _identifier;
    }

    public void addTransition(Symbol s, State to) {
        transitions.put(s, to);
    }

    public State transition(Symbol s) {
        return transitions.get(s);
    }

    public String printState() {
        String result = "q";
        if (accepting) {
            result = "\u001B[32m" + "q" + identifier + "\u001B[0m";
        } else if (startState) {
            result = "\u001B[31m" + "q" + identifier + "\u001B[0m";
        } else {
            result += identifier;
        }
        return result;
    }

    // getTransitionsForSymbol
    public List<State> getTransitionsForSymbol(Symbol s) {
        List<State> states = new ArrayList<State>();
        for (Symbol sym : transitions.keySet()) {
            if (sym.identifier.equals(s.identifier)) {
                states.add(transitions.get(sym));
            }
        }
        return states;
    }
}
