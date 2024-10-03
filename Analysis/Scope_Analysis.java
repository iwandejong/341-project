package Analysis;

// import the symbol table
import java.util.Hashtable;

import Analysis.Scope_Stack;
import Parser.Node;
import Parser.Tree;

public class Scope_Analysis {
    // create a symbol table
    Symbol_Table symbolTable = new Symbol_Table();
    Scope_Stack scopeStack = new Scope_Stack();
    // functions for the symbol table
    // add a new entry
    // public void bind(int id, String type) {
    //     symbolTable.put(id, type);
    // }
    
    // search
    // public String lookup(int id) {
    //     return symbolTable.get(id);
    // }

    // throw exception if the table is empty
    // public void empty() {
    //     if (symbolTable.isEmpty()) {
    //         throw new RuntimeException("The symbol table is empty.");
    //     }
    // }
    
    // take the parser tree 
    public void start(Tree tree) {
        // go through the tree and add

        symbolTable = buildSymbolTable(tree.root, symbolTable);
    }

    public Symbol_Table buildSymbolTable(Node node, Symbol_Table symbolTable) {
        // if the node is null, return the symbol table
        if (node == null) {
            return symbolTable;
        }
        int scope = 0;
        // if the node is not a reserved word, add it to the symbol table
        for (int i = 0; i < node.children.size(); i++) {
            scope++;
            if (node.children != null && node.children.get(i) != null && node.children.get(i).token != null) { // Add this check
                if (!node.children.get(i).token.tokenClass.equals("reserved_keyword")) {
                    symbolTable.bind(node.children.get(i).token.id, node.children.get(i).identifier.identifier, scope);
                }
            //   check if the node is a function and push it to the stack
                if(node.children.get(i).identifier.identifier.startsWith("F_")){
                    scopeStack.push(node.children.get(i).token.id);
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
        symbolTable.printTable();
    }

    public void printScopeStack() {
        // print the scope stack
        scopeStack.printStack();
    }
}
