package Analysis;

// import the symbol table
import java.util.Hashtable;

public class Scope_Analysis {
    // create a symbol table
    Hashtable<String, String> symbolTable = new Hashtable<String, String>();
    
    // functions for the symbol table
    // add a new entry
    public void bind(String id, String type) {
        symbolTable.put(id, type);
    }
    
    // search
    public String lookup(String id) {
        return symbolTable.get(id);
    }

    // throw exception if the table is empty
    public void empty() {
        if (symbolTable.isEmpty()) {
            throw new RuntimeException("The symbol table is empty.");
        }
    }

    // generate new unique names for variables and functions (user-defined)
    public void generateUniqueName() {
        // for variables assign v1, v2, v3, ...
        // for functions assign f1, f2, f3, ...
        // go through symbol table and assign unique names
        for(String key : symbolTable.keySet()) {
            // check if the key is a variable
            int variableCount = 1;
            if (symbolTable.get(key).equals("variable")) {
                // assign a unique name
                symbolTable.put(key, "v" + variableCount);
            }

            // check if the key is a function
            int functionCount = 1;
            if (symbolTable.get(key).equals("function")) {
                // assign a unique name
                symbolTable.put(key, "f" + functionCount);
            }
        }
    }
    
    
    
    // read from lexer_output.xml and populate the symbol table
    public void start() {

    }

}
