package Analysis;

public class InnerSymbol_Table {
    String value;
    int scope;
    String type;
    String declarationType;

    public InnerSymbol_Table(String value, int scope, String type, String declarationType) {
        this.value = value;
        this.scope = scope;
        this.type = type;
        this.declarationType = declarationType;
    }
    
}
