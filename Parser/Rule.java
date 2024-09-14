package Parser;

public class Rule {
    public String identifier;
    public Rule next;
    public boolean terminal;

    public Rule(String _identifier, Rule _next, boolean _terminal) {
        identifier = _identifier;
        next = _next;
        terminal = _terminal;
    }
}
