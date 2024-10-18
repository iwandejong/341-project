package Parser;

public class Symbol {
    public String identifier;
    public boolean terminal;
    public boolean nullable = false;
    public boolean visited = false;

    public Symbol(String _identifier, boolean _terminal) {
        identifier = _identifier;
        terminal = _terminal;
    }
}
