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
    
    // take the parser tree 
    public void start(Tree tree) {
        // go through the tree and add
        symbolTable = buildSymbolTable(tree.root, symbolTable);
        System.out.println();
        System.out.println("\u001B[33m" + "Symbol Table:" + "\u001B[0m");
        printSymbolTable();
        System.out.println();
        System.out.println("\u001B[33m" + "Scope Stack:" + "\u001B[0m");
        printScopeStack();
        System.out.println();
        System.out.println("\u001B[33m" + "Validate Symbol Table:" + "\u001B[0m");
        validateSymbolTable(symbolTable,scopeStack);
        System.out.println();
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

    public void validateSymbolTable(Symbol_Table symbolTable, Scope_Stack scopeStack){
        // check if the symbol table is empty
        if(symbolTable.empty()){
            System.out.println("The symbol table is empty.");
        }else{
            // go through the entire symbol table and check if symbol scope matches the current scope
            for(Integer key : symbolTable.table.keySet()){
                if(scopeStack.scopeStack.contains(symbolTable.table.get(key).scope)){
                    
                }else{
                    // print out the scope that is invalid
                    System.out.println("\u001B[31m" + "Symbol: " + key + " is not in scope. Scope: " + symbolTable.table.get(key).scope + "\u001B[0m");
                    // invalid scope
                    throw new RuntimeException("Symbol: " + key + " is not in scope.");
                }
            }
            System.out.println("\u001B[32m" + "Symbol table is valid." + "\u001B[0m");
        }
    }
}
