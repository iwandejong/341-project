package Parser;
import java.util.*;

public class ProductionRule {
    public Symbol lhs;
    public List<Symbol> rhs;

    public ProductionRule(Symbol _lhs, List<Symbol> _rhs) {
        lhs = _lhs;
        rhs = _rhs;
    }
}
