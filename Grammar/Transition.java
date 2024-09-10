package Grammar;
public class Transition {
    public State from;
    public State to;
    public Character input;

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
