package Parser;

public class LR0 {
    ProductionRule rule;
    int dotPosition;

    public LR0(ProductionRule _rule, int _dotPosition) {
        rule = _rule;
        dotPosition = _dotPosition;
    }

    public boolean equals(LR0 lr0) {
        if (rule.equals(lr0.rule) && dotPosition == lr0.dotPosition) {
            return true;
        }
        return false;
    }

    public boolean isComplete() {
        if (dotPosition == rule.rhs.size()) {
            return true;
        }
        return false;
    }

    public LR0 advanceDot() {
        if (!isComplete()) {
            return new LR0(production, dotPosition + 1);
        }
        return null;
    }
}
