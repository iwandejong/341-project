package Grammar;
import java.util.*;

public class Transition {
    public State from;
    public State to;
    public List<Character> inputs;

    public Transition (State _from, State _to, List<Character> _inputs) {
        from = _from;
        to = _to;
        inputs = _inputs;
    }

    public State transitionTo (char _input) {
        if (inputs.contains(_input)) {
            return to;
        }
        return null; // ? should this return null?
    }
}
