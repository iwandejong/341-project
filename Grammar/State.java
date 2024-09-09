package Grammar;
import java.util.*;

public class State {
    public String identifier;
    public boolean acceptingState;
    public List<Transition> transitions;

    public State (String _identifier, boolean _acceptingState, List<Transition> _transitions) {
        identifier = _identifier;
        acceptingState = _acceptingState;
        transitions = _transitions;
    }
}
