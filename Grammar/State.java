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

    public State transition (char input) {
        Transition t = null; // pick transition to go to

        for (int i = 0; i < transitions.size(); i++) {
            if (transitions.get(i).inputs.contains(input)) {
                t = transitions.get(i);
                break;
            }
        }

        if (t != null) {
            return t.transitionTo(input);
        }
        return null; // ! no transitions found for input
    }
}
