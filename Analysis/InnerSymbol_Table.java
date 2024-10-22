package Analysis;

public class InnerSymbol_Table {
    public String value;
    public int scope;
    public String type;
    public String declarationType;

    public InnerSymbol_Table(String value, int scope, String type, String declarationType) {
        this.value = value;
        this.scope = scope;
        this.type = type;
        this.declarationType = declarationType;
    }
    
    public void print() {
        System.out.println("Value: " + value + " Scope: " + scope + " Type: " + type + " Declaration Type: " + declarationType);
    }
}
