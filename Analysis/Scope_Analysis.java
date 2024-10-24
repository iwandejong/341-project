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
    public int varCount = 0;
    public int funcCount = 0;
    public boolean isDeclarationInFunction = false;
    int paramterecount = 0;
    
    // take the parser tree 
    public Symbol_Table start(Tree tree) {
        this.tree = tree;
        // build the symbol table
        symbolTable = buildSymbolTable(tree.root, symbolTable);
        
        System.out.println();
        // print the symbol table 
        // System.out.println("\u001B[33m" + "Symbol Table:" + "\u001B[0m");
        // printSymbolTable();
        // System.out.println();

        // print the scope stack
        System.out.println("\u001B[33m" + "Scope Stack:" + "\u001B[0m");
        printScopeStack();
        System.out.println();

        // validate the symbol table
        System.out.println("\u001B[33m" + "Validate Symbol Table:" + "\u001B[0m");
        validateSymbolTable(symbolTable,scopeStack);
        System.out.println();

        checkSemanticRules();

        return symbolTable;
    }

    public String genNewVar() {
        return "V" + varCount++;
    }

    public String genNewFunc() {
        return "F" + funcCount++;
    }

    // TODO: check all rules
    public Symbol_Table buildSymbolTable(Node node, Symbol_Table symbolTable) {
        // if the node is null, return the symbol table
        // isDeclaration = false;
        if (node == null) {
            return symbolTable;
        }
        
        if(node.token != null && node.token.tokenValue != null){
            // System.out.println("Node value: " + node.token.tokenValue);
        }

        // if the node is not a reserved word, add it to the symbol table
        for (int i = 0; i < node.children.size(); i++) {
            // if the node is a function
            if(node.children!=null&& node.children.get(i) != null && node.children.get(i).token!= null && node.children.get(i).token.tokenValue != null && node.children.get(i).token.tokenValue.equals("void")){
                declarationType = "void";
                isDeclarationInFunction = true;
                isDeclaration = true;
            }else if(node.children!=null&& i>0 && node.children.get(i-1) != null && node.children.get(i-1).token!= null && node.children.get(i-1).token.tokenValue != null && node.children.get(i-1).token.tokenValue.equals("num")){
                declarationType = "num";
                isDeclarationInFunction = true;
                isDeclaration = true;
            }
            if(node.children.get(i).identifier.identifier.startsWith("F_")){
                // every function opens a new scope
                // see if the function is already in the symbol table ( could be due to call in main )
                if(symbolTable.lookupName(node.children.get(i).identifier.identifier) == null){
                    symbolTable.bind(genNewFunc(), node.children.get(i).identifier.identifier, scope, "F", declarationType);
                    declarationType = "num";
                    // scopeStack.push(scope);
                    continue;
                }else{
                    // if the function is already in the symbol table, push the scope
                    symbolTable.bind(genNewFunc(), node.children.get(i).identifier.identifier, scope, "F", declarationType);
                    declarationType = "num";
                    // scopeStack.push(symbolTable.lookupName(node.children.get(i).identifier.identifier).scope);
                    continue;
                }
            }
            // if the node is a variable
            if (node.children != null && node.children.get(i) != null && node.children.get(i).token != null) { // Add this check
                // if it is not a reserved keyword
                if(node.children.get(i).token.tokenClass.equals("reserved_keyword") && node.children.get(i).token.tokenValue.equals("(")){
                    paramterecount = 0;
                    // System.out.println("Setting isDeclarationInFunction to true");
                    // isDeclaration = true;
                    declarationType = "num";
                    // isDeclarationInFunction = true;
                }
                else if(node.children.get(i).token.tokenClass.equals("reserved_keyword") && paramterecount == 3 && isDeclarationInFunction){
                    // System.out.println("Setting isDeclarationInFunction to false and token vlaue is: " + node.children.get(i).token.tokenValue);
                    // isDeclarationInFunction = false;
                    isDeclaration = false;
                }
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
                            // System.out.println("Symbol: " + node.children.get(i).identifier.identifier + "isDecl: " + isDeclaration);
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
                        // it is not a string or number
                        if(!node.children.get(i).token.tokenValue.matches(numberRegex) && !node.children.get(i).token.tokenValue.matches(stringRegex)){
                            // it is not already in table with the same scope
                            if(symbolTable.lookupName(node.children.get(i).identifier.identifier) == null){
                                symbolTable.bind(genNewVar(), node.children.get(i).identifier.identifier, scope, node.children.get(i).token.tokenClass, thisDeclarationType);
                            }
                        }
                    }else{
                        paramterecount++;
                        // System.out.println("hier2");
                        String stringRegex = "\"[a-zA-Z0-9]*\"";
                        String numberRegex = "[0-9]+";
                        if(!node.children.get(i).token.tokenValue.matches(numberRegex) && !node.children.get(i).token.tokenValue.matches(stringRegex)){
                            // it is not already in table with the same scope
                            if(symbolTable.lookupName(node.children.get(i).identifier.identifier) == null){
                                symbolTable.bind(genNewVar(), node.children.get(i).identifier.identifier, scope, node.children.get(i).token.tokenClass, declarationType);
                            }
                        }
                        // symbolTable.bind(genNewVar(), node.children.get(i).identifier.identifier, scope, "D", declarationType);
                    }
                }
                else if(node.children.get(i).token.tokenClass.equals("reserved_keyword") && node.children.get(i).token.tokenValue.equals("void")){
                    ++scope;
                    scopeStack.push(scope);
                }
                // it is the reserved keyword is not , (the declaration "stops")
                if(node.children.get(i).token.tokenClass.equals("reserved_keyword") && !node.children.get(i).token.tokenValue.equals(",") && !isDeclarationInFunction){
                    // System.out.println("isDeclarationInFunction: " + isDeclarationInFunction);
                    isDeclaration = false;
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
            for(String key : symbolTable.table.keySet()){
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
        System.out.println("\u001B[33m" + "Semantic Rules:" + "\u001B[0m");
        // no double declarations
        Rule1();
        // Declaration must be in same or lower scope
        Rule2();
        // If 2 declarations in different scopes have the same name, the one in the higher scope is used
        Rule3();
        // Every variable must be declared before it is used
        Rule4();
        // 5) N/A since variables = V_... and functions = F_...
        // 6) N/A since variables = V_... and doesnt include reserved keywords
        // 7) completed by different id's
        // 8) main = scope 1
        // 9) every void increases the scope thus also 10 + 11
        // 12) + 13) ?? Call commands?
        // 14) no calls to main

        Rule10();
        Rule11();
        System.out.println("\u001B[32m" + "All semantic rules are valid." + "\u001B[0m");
    }

    // Rule 1: No variable name may be declared more than once in the same scope of different types
    public void Rule1(){
        for(String key : symbolTable.table.keySet()){
            // check if the symbol is declared more than once in the same scope
            for(String key2 : symbolTable.table.keySet()){
                if(symbolTable.table.get(key).value.equals(symbolTable.table.get(key2).value) && symbolTable.table.get(key).scope == symbolTable.table.get(key2).scope && !symbolTable.table.get(key).declarationType.equals(symbolTable.table.get(key2).declarationType)){
                    throw new RuntimeException("Symbol: " + symbolTable.table.get(key).value + " is declared more than once in the same scope with different types.");
                }
            }
        }
    }

    // Rule 2: Declaration of used variable name must be found in either own scope or "higher" (in our case lower since highest scope is 1)
    public void Rule2(){
        // TODO: Test this thoroughly to make sure it works at a later time
        for(String key : symbolTable.table.keySet()){
            // check if the symbol is declared more than once in the same scope
            for(String key2 : symbolTable.table.keySet()){
                // if the key doesnt start with F
                if(!key.startsWith("F") && !key2.startsWith("F")){        
                    if(symbolTable.table.get(key).value.equals(symbolTable.table.get(key2).value) && symbolTable.table.get(key).scope < symbolTable.table.get(key2).scope && !symbolTable.table.get(key).declarationType.equals(symbolTable.table.get(key2).declarationType)){
                        throw new RuntimeException("Symbol: " + symbolTable.table.get(key).value + " is declared in a lower scope with a different type.");
                    }
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
        
        for(String key : symbolTable.table.keySet()){
            // find type = V, Use that symbol and check if it is declared
            if(symbolTable.table.get(key).type.equals("V")){
                if(symbolTable.lookupName(symbolTable.table.get(key).value) == null){
                    throw new RuntimeException("Symbol: " + symbolTable.table.get(key).value + " is not declared.");
                }
            } 
        }
    }

    public void Rule10(){
        
    }

    public void Rule11(){

    }
}
