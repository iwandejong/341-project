package Grammar;

public class Transition {
    public State current;
    public State next;

    public Transition (State _current, State _next) {
        current = _current;
        next = _next;
    }
}
