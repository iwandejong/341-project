package Analysis;

// import the symbol table
import java.util.Hashtable;

import Analysis.Scope_Stack;
import Parser.Node;
import Parser.Tree;

public class Scope_Analysis {
    // create a symbol table
    public Symbol_Table symbolTable = new Symbol_Table();
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

        checkSemanticRules();
    }
    // TODO: check all rules
    public Symbol_Table buildSymbolTable(Node node, Symbol_Table symbolTable) {
        // if the node is null, return the symbol table
        if (node == null) {
            return symbolTable;
        }
        
        if(node.token != null && node.token.tokenValue != null){
            // System.out.println("Node value: " + node.token.tokenValue);
        }

        // if the node is not a reserved word, add it to the symbol table
        for (int i = 0; i < node.children.size(); i++) {
            // print the current node;
            // scope++;
            if (node.children != null && node.children.get(i) != null && node.children.get(i).token != null) { // Add this check
                // if it is not a reserved keyword
                if (!node.children.get(i).token.tokenClass.equals("reserved_keyword")) {
                    if(!isDeclaration){
                        // lookup the value in the symbol table and set that declaration type
                        String thisDeclarationType = "";
                        try{
                            // check if it is a actual number
                            String numberRegex = "[0-9]+";
                            // String has the form "words"
                            String stringRegex = "\"[a-zA-Z0-9]*\"";
                            // if its not a number, get the declaration type from the symbol table
                            if(!node.children.get(i).token.tokenValue.matches(numberRegex) && !node.children.get(i).token.tokenValue.matches(stringRegex)){
                                thisDeclarationType = symbolTable.lookupName(node.children.get(i).identifier.identifier).declarationType;
                            }
                        }catch(Exception e){
                            // print current symbol that throws the exception
                            System.out.println("Symbol: " + node.children.get(i).identifier.identifier);
                            // print symbol table for now
                            symbolTable.printTable();
                            throw new RuntimeException("Variable is used before declaration.");
                        }
                        //* Rule 4: Every variable must be declared before it is used
                        if(thisDeclarationType == null){
                            throw new RuntimeException("Symbol: " + node.children.get(i).identifier.identifier + " is not declared.");
                        }
                        String stringRegex = "\"[a-zA-Z0-9]*\"";
                        String numberRegex = "[0-9]+";
                        if(!node.children.get(i).token.tokenValue.matches(numberRegex) && !node.children.get(i).token.tokenValue.matches(stringRegex)){
                            symbolTable.bind(node.children.get(i).token.id, node.children.get(i).identifier.identifier, scope, node.children.get(i).token.tokenClass, thisDeclarationType);
                        }
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
                // * check if scopes are correct
                if(scopeStack.scopeStack.contains(symbolTable.table.get(key).scope)){
                    
                }else{
                    // print out the scope that is invalid
                    System.out.println("\u001B[31m" + "Symbol: " + key + " is not in scope. Scope: " + symbolTable.table.get(key).scope + "\u001B[0m");
                    // invalid scope
                    throw new RuntimeException("Symbol: " + key + " is not in scope.");
                }

            }
            System.out.println("\u001B[32m" + "Initial symbol table is valid." + "\u001B[0m");
        }
    }

    public void checkSemanticRules(){
        // 1) 
        System.out.println("\u001B[33m" + "Semantic Rules:" + "\u001B[0m");
        Rule1();
        Rule2();
        Rule3();
        Rule4();
        Rule5();
        System.out.println("\u001B[32m" + "All semantic rules are valid." + "\u001B[0m");
    }

    // Rule 1: No variable name may be declared more than once in the same scope of different types
    public void Rule1(){
        for(Integer key : symbolTable.table.keySet()){
            // check if the symbol is declared more than once in the same scope
            for(Integer key2 : symbolTable.table.keySet()){
                if(symbolTable.table.get(key).value.equals(symbolTable.table.get(key2).value) && symbolTable.table.get(key).scope == symbolTable.table.get(key2).scope && !symbolTable.table.get(key).declarationType.equals(symbolTable.table.get(key2).declarationType)){
                    throw new RuntimeException("Symbol: " + symbolTable.table.get(key).value + " is declared more than once in the same scope with different types.");
                }
            }
        }
    }

    // Rule 2: Declaration of used variable name must be found in either own scope or "higher" (in our case lower since highest scope is 1)
    public void Rule2(){
        // TODO: Test this thoroughly to make sure it works at a later time
        for(Integer key : symbolTable.table.keySet()){
            // check if the symbol is declared more than once in the same scope
            for(Integer key2 : symbolTable.table.keySet()){
                if(symbolTable.table.get(key).value.equals(symbolTable.table.get(key2).value) && symbolTable.table.get(key).scope < symbolTable.table.get(key2).scope && !symbolTable.table.get(key).declarationType.equals(symbolTable.table.get(key2).declarationType)){
                    throw new RuntimeException("Symbol: " + symbolTable.table.get(key).value + " is declared in a lower scope with a different type.");
                }
            }
        }
    }

    // Rule 3: If 2 declarations in different scopes have the same name, the one in the higher scope is used
    public void Rule3(){
        // TODO: implement this rule

    }

    // Rule 4: Every variable must be declared before it is used
    public void Rule4(){
        // look through the symbol table and check if the symbol is declared
        
        for(Integer key : symbolTable.table.keySet()){
            // find type = V, Use that symbol and check if it is declared
            if(symbolTable.table.get(key).type.equals("V")){
                if(symbolTable.lookupName(symbolTable.table.get(key).value) == null){
                    throw new RuntimeException("Symbol: " + symbolTable.table.get(key).value + " is not declared.");
                }
            } 
        }
    }

    public void Rule5(){
        
    }
}
