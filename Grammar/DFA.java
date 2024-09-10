package Grammar;

public class DFA {
    public State transition(String input, Grammar g) {
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
