package Analysis;

import java.util.Hashtable;

import Lexer.Token;
import Parser.Node;

public class Symbol_Table {
    // simple hash table
    // key: String, value: String (key = id, value = type)
    Hashtable<Integer, InnerSymbol_Table> table = new Hashtable<Integer, InnerSymbol_Table>();

    // functions: bind (add a new entry), lookup (search for an entry), empty (an empty table is an empty list), enter (old table is remebered (referenced)), exit (old table is forgotten)
    public void bind(int id, String value, int scope, String type, String declarationType) {
        table.put(id, new InnerSymbol_Table(value, scope, type, declarationType));
    }

    public String lookup(int id) {
        // find in the symbol table with key == id
        InnerSymbol_Table innerSymbolTable = table.get(id);
        if (innerSymbolTable == null) {
            return null;
        }
        return innerSymbolTable.value;
    }

    public boolean empty() {
        return table.isEmpty();
    }

    public void enter() {
        // do nothing
    }

    public void exit() {
        // do nothing
    }

    // public void buildSymbolTable(Node node, Symbol_Table symbolTable){

    // }

    public void printTable() {
        if(table.isEmpty()){
            System.out.println("The table is empty.");
        }else{
            // go through the entire thing and print out the id, value, and scope
            for (Integer key : table.keySet()) {
                System.out.println("ID: " + key + ", Value: " + table.get(key).value + ", Scope: " + table.get(key).scope + ", Type: " + table.get(key).type + ", Declaration Type: " + table.get(key).declarationType);
            }
        }
    }
}


