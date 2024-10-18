package Parser;
import java.util.*;

public class FIRSTFOLLOWtable {
    Symbol nonTerminal;
    List<Symbol> first;
    List<Symbol> follow;
    boolean isNullable;

    public FIRSTFOLLOWtable(Symbol _nonTerminal, boolean _isNullable) {
        nonTerminal = _nonTerminal;
        first = new ArrayList<Symbol>();
        follow = new ArrayList<Symbol>();
        isNullable = _isNullable;
    }

    public void addFirst(Symbol _first) {
        for (Symbol symbol : first) {
            if (symbol.identifier.equals(_first.identifier)) {
                return;
            }
        }
        first.add(_first);
    }

    public void addFollow(Symbol _follow) {
        for (Symbol symbol : follow) {
            if (symbol.identifier.equals(_follow.identifier)) {
                return;
            }
        }
        follow.add(_follow);
    }
}
