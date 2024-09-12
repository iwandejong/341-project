package Grammar;
public class Transition {
    public State from;
    public State to;
    public Character input; // * where 'ε' is an epsilon-transition
    // TODO: Figure out a way to allow for non-terminal transitions (e.g. PROG -> main GLOBVARS ALGO)
        // in this instance, first visit GLOBVARS, but pop GLOBVARS from the stack once one, to return to ALGO.

    public Transition (State _from, State _to, Character _input) {
        from = _from;
        to = _to;
        input = _input;
    }

    public State transitionTo (char _input) {
        if (input == _input) {
            return to;
        }
        return null; // ? should this return null?
    }
}
