package Analysis;

import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import Lexer.Token;
import Parser.Node;

public class Symbol_Table {
    // simple hash table
    // key: String, value: String (key = id, value = type)
    public Hashtable<String, InnerSymbol_Table> table = new Hashtable<String, InnerSymbol_Table>();

    // functions: bind (add a new entry), lookup (search for an entry), empty (an empty table is an empty list), enter (old table is remebered (referenced)), exit (old table is forgotten)
    public void bind(String id, String value, int scope, String type, String declarationType) {
        table.put(id, new InnerSymbol_Table(value, scope, type, declarationType));
    }

    public String lookup(String id) {
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

    public InnerSymbol_Table lookupName(String value){
        // go through the entire table and find the value
        for (String key : table.keySet()) {
            if(table.get(key).value.equals(value)){
                return table.get(key);
            }
        }
        return null;
    }

    public String lookupID(String value){
        // go through the entire table and find the value
        for (String key : table.keySet()) {
            if(table.get(key).value.equals(value)){
                return key;
            }
        }
        return "";
    }

    public String[] findIds(String value){
        List<String> matchingEntries = new ArrayList<>();
        for (Map.Entry<String, InnerSymbol_Table> entry : table.entrySet()) {
            if (entry.getValue().value.equals(value)) {
                matchingEntries.add(entry.getKey());
            }
        }
    
        return matchingEntries.toArray(new String[0]);
    }

    public void printTable() {
        if(table.isEmpty()){
            System.out.println("The table is empty.");
        }else{
            // go through the entire thing and print out the id, value, and scope
            for (String key : table.keySet()) {
                System.out.println("ID: " + key + ", Value: " + table.get(key).value + ", Scope: " + table.get(key).scope  + ", Declaration Type: " + table.get(key).declarationType);
            }
        }
    }
}


