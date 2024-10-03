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
    Tree tree;
    public static int scope = 1;
    public static boolean isDeclaration = false;
    public static String declarationType = "";
    
    // take the parser tree 
    public void start(Tree tree) {
        this.tree = tree;
        // build the symbol table
        symbolTable = buildSymbolTable(tree.root, symbolTable);
        
        System.out.println();
        // print the symbol table 
        System.out.println("\u001B[33m" + "Symbol Table:" + "\u001B[0m");
        printSymbolTable();
        System.out.println();

        // print the scope stack
        System.out.println("\u001B[33m" + "Scope Stack:" + "\u001B[0m");
        printScopeStack();
        System.out.println();

        // validate the symbol table
        System.out.println("\u001B[33m" + "Validate Symbol Table:" + "\u001B[0m");
        validateSymbolTable(symbolTable,scopeStack);
        System.out.println();

        // checkRules();
    }
    // TODO: check all rules
    public Symbol_Table buildSymbolTable(Node node, Symbol_Table symbolTable) {
        // if the node is null, return the symbol table
        if (node == null) {
            return symbolTable;
        }
        
        // if the node is not a reserved word, add it to the symbol table
        for (int i = 0; i < node.children.size(); i++) {
            // scope++;
            if (node.children != null && node.children.get(i) != null && node.children.get(i).token != null) { // Add this check
                // if it is not a reserved keyword
                if (!node.children.get(i).token.tokenClass.equals("reserved_keyword")) {
                    if(!isDeclaration){
                        symbolTable.bind(node.children.get(i).token.id, node.children.get(i).identifier.identifier, scope, node.children.get(i).token.tokenClass, "");
                    }else{
                        symbolTable.bind(node.children.get(i).token.id, node.children.get(i).identifier.identifier, scope, "D", declarationType);
                    }
                }

                // it is the reserved keyword is not , (the declaration "stops")
                if(node.children.get(i).token.tokenClass.equals("reserved_keyword") && !node.children.get(i).token.tokenValue.equals(",")){
                    isDeclaration = false;
                }
                
                //   check if the node is a function and push it to the stack
                if(node.children.get(i).identifier.identifier.startsWith("F_")){
                    // every function opens a new scope
                    scopeStack.push(++scope);
                }
                
                // if reserved keyword = text or num, add the rest of the children to the symbol table with type = D for declaration
                if (node.children.get(i).token.tokenClass.equals("reserved_keyword") && (node.children.get(i).token.tokenValue.equals("text") || node.children.get(i).token.tokenValue.equals("num"))) {
                    declarationType = node.children.get(i).token.tokenValue;
                    isDeclaration = true;
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

    public void checkSemanticRules(){
        // 1) no vairable name may be declared more than once in the same scope

    }
}
