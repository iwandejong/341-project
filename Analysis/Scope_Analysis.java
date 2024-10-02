package Analysis;

// import the symbol table
import java.util.Hashtable;

import Parser.Node;
import Parser.Tree;

public class Scope_Analysis {
    // create a symbol table
    Hashtable<Integer, String> symbolTable = new Hashtable<Integer, String>();
    
    // functions for the symbol table
    // add a new entry
    public void bind(int id, String type) {
        symbolTable.put(id, type);
    }
    
    // search
    public String lookup(int id) {
        return symbolTable.get(id);
    }

    // throw exception if the table is empty
    public void empty() {
        if (symbolTable.isEmpty()) {
            throw new RuntimeException("The symbol table is empty.");
        }
    }
    
    // take the parser tree 
    public void start(Tree tree) {
        // go through the tree and add

        symbolTable = buildSymbolTable(tree.root, symbolTable);
    }

    public Hashtable<Integer, String> buildSymbolTable(Node node, Hashtable<Integer, String> symbolTable) {
        // if the node is null, return the symbol table
        if (node == null) {
            return symbolTable;
        }

        // if the node is not a reserved word, add it to the symbol table
        for (int i = 0; i < node.children.size(); i++) {
            if (node.children != null && node.children.get(i) != null && 
                node.children.get(i).token != null) { // Add this check
              if (!node.children.get(i).token.tokenClass.equals("reserved_keyword")) {
                symbolTable.put(node.children.get(i).token.id, node.children.get(i).identifier.identifier);
              }
            }
          }
        
        // recursively call the function for the children
        for (int i = 0; i < node.children.size(); i++) {
            symbolTable = buildSymbolTable(node.children.get(i), symbolTable);
        }

        return symbolTable;
    }

    public void printSymbolTable() {
        // print the symbol table
        System.out.println("Symbol Table:");
        for (Integer key : symbolTable.keySet()) {
            System.out.println("ID: " + key + " Type: " + symbolTable.get(key));
        }
    }
}
