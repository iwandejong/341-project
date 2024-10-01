package Parser;

public class Symbol {
    public String identifier;
    public boolean terminal;

    public Symbol(String _identifier, boolean _terminal) {
        identifier = _identifier;
        terminal = _terminal;
    }
}
