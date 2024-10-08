package Parser;

public class Symbol {
    public String identifier;
    public boolean terminal;
    public Symbol epsilonTransition = null; // by default no epsilon transitions

    public Symbol(String _identifier, boolean _terminal) {
        identifier = _identifier;
        terminal = _terminal;
    }
}
