package Grammar;
import java.util.*;

public class Grammar {
    // States (Q)
    public List<State> Q;

    // Alphabet (E)
    public List<String> E;

    // Rules (R)
    public List<Transition> R;

    // Start State (S)
    public State S;

    // Final States (F)
    public List<State> F;

    public Grammar(List<State> _Q, List<String> _E, List<Transition> _R, State _S, List<State> _F) throws Exception {
        Q = _Q;
        E = _E;
        R = _R;

        for (int i = 0; i < _R.size(); i++) {
            if (!Q.contains(_R.get(i).from)) {
                throw new Exception("Transitions need to be able to transition 'from' existing states");
            }

            if (!Q.contains(_R.get(i).to)) {
                throw new Exception("Transitions need to be able transition 'to' existing states");
            }
        }

        if (!Q.contains(_S)) {
            throw new Exception("Start state needs to be part of the states");
        }

        for (int i = 0; i < _F.size(); i++) {
            if (!Q.contains(_F.get(i))) {
                throw new Exception("Accepting states needs to be part of the states");
            }
        }
    }

    
}
