package Parser;
import java.util.*;

public class ProductionRule {
    public Symbol lhs;
    public List<Symbol> rhs;
    public boolean nullable = false;

    public ProductionRule(Symbol _lhs, List<Symbol> _rhs) {
        lhs = _lhs;
        rhs = _rhs;
    }

    public ProductionRule(Symbol _lhs, List<Symbol> _rhs, boolean _nullable) {
        lhs = _lhs;
        rhs = _rhs;
        nullable = _nullable;
    }
}
