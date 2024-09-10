package Grammar;

public class DFA {
    public Grammar g;

    public DFA (Grammar _g) {
        g = _g;
    }

    public State transition(String input) {
        State s = g.S; // mark as starting state
        return transitionHelper(s, input);
    }

    public State transitionHelper(State s, String input) {
        if (input.length() > 0) {
            s = s.transition(input.charAt(0));
            input = input.substring(1, input.length());

            return transitionHelper(s, input);
        }
        return s;
    }
}
