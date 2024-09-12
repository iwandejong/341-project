package Grammar;
import java.util.*;

public class Grammar {
    // States (Q)
    public List<State> Q;

    // Alphabet (E)
    // public List<String> E; // ? indirectly defined by the states?

    // Rules (R)
    public List<Transition> R;

    // Start State (S)
    public State S;

    // Final States (F)
    // public List<State> F; // ? might remove later since states define if they're accepting or not.

    public Grammar() {
        Q = new ArrayList<State>();
        R = new ArrayList<Transition>();
    }

    public void addState(State _Q) throws Exception {
        Q.add(_Q);
    }

    public void addTransition(Transition _R) throws Exception {
        if (!Q.contains(_R.from)) {
            throw new Exception("Transitions need to be able to transition 'from' existing states");
        }

        if (!Q.contains(_R.to)) {
            throw new Exception("Transitions need to be able transition 'to' existing states");
        }

        R.add(_R);
    }

    public void setStartState(State _S) throws Exception {
        if (!Q.contains(_S)) {
            throw new Exception("Start state needs to be part of the states");
        }
    }
}
