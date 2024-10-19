package Type_Checker;

import Analysis.InnerSymbol_Table;
import Analysis.Symbol_Table;
import Parser.Node;
import Parser.Tree;

public class Type_Checker {
    // recursive boolean procedure that analyses syntax tree
    
    // public Tree syntaxTree;
    public Symbol_Table symbolTable;
    public boolean result = true;
    public Node node;
    // array storing the types of children nodes
    public boolean[] childrenTypeChecks;
    // pass in root node of syntax tree
    public void check(Node node, Symbol_Table symbolTable){
        // print in yellow "Type Checking"
        System.out.println();
        System.out.println("\u001B[33m" + "Type Checking:" + "\u001B[0m");
        this.symbolTable = symbolTable;
        this.node = node;
        // loop through tree and do typecheck for each non terminal?
        boolean result = typeCheck(node, symbolTable);
        // print in green "Type Checking Complete"
        if(result){
            System.out.println("\u001B[32m" + "Type Checking Passed" + "\u001B[0m");
        }else{
            System.out.println("\u001B[31m" + "Type Checking Failed" + "\u001B[0m");
        }
    }
    
    // 2 functions: typecheck, typeof. typecheck = main procedure, typeof = helps typecheck procedure. has access to existing symbol table and reports type info
    public boolean typeCheck(Node node, Symbol_Table symbolTable){
        if(node == null){
            return true;
        }
        if(node.children.size() == 0){
            return true;
        }
        boolean[] childrenTypeChecks = new boolean[node.children.size()];
        if(node.identifier.identifier == "PROG"){
            // check children
            for(Node child : node.children){
                if(child != null && child.token != null && child.token.tokenValue != "main"){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }else if(child != null && child.token != null && child.token.tokenValue == "main"){
                    // main function to true automatically
                    childrenTypeChecks[node.children.indexOf(child)] = true;
                }
            }
            // check if all children are true except for main
            for(boolean check : childrenTypeChecks){
                if(check == false){
                    return false;
                }
            }
            return true;
        }else if(node.identifier.identifier == "GLOBVARS"){
            // epsilon transition
            if(node.children.size() == 0){
                return true;
            }

            for(Node child : node.children){
                if(child.identifier.identifier == "GLOBVARS"){
                    return childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
            }
        }else if(node.identifier.identifier == "ALGO"){
            // epsilon transition
            for(Node child : node.children){
                if(child.identifier.identifier == "ALGO"){
                    return childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
            }
        }else if(node.identifier.identifier == "INSTRUC"){
            // epsilon transition
            if(node.children.size() == 0){
                return true;
            }
            for(Node child : node.children){
                if(child.identifier.identifier == "INSTRUC" || child.identifier.identifier == "COMMAND"){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }else{
                    // if ;
                    childrenTypeChecks[node.children.indexOf(child)] = true;
                }
            }
            for(boolean check : childrenTypeChecks){
                if(check == false){
                    return false;
                }
            }
            return true;
        }else if(node.identifier.identifier == "COMMAND"){
            for(Node child: node.children){
                if(child.token != null && child.token.tokenValue == "skip" || child.token.tokenValue == "halt"){
                    return true;
                }
                if(child.identifier.identifier == "ATOMIC"){
                    if(typeOf(symbolTable, child) == "num" || typeOf(symbolTable, child) == "text"){
                        return true;
                    }else{
                        return false;
                    }
                }
                if(child.identifier.identifier == "ASSIGN" || child.identifier.identifier == "BRANCH"){
                    return typeCheck(child, symbolTable);
                }
                if(child.identifier.identifier == "CALL"){
                    if(typeOf(symbolTable, child) == "void"){
                        return true;
                    }else{
                        return false;
                    }
                }
                // TODO: return ATOMIC for FUNCTIONS
            }
        }else if(node.identifier.identifier == "ASSIGN"){
            // 2 types of assign: VNAME < input, VNAME = TERM. 
            // VNAME < input. 0 = VNAME, 1 = <, 2 = input
            if(node.children.get(2).token != null && node.children.get(2).token.tokenValue == "input"){
                if(typeOf(symbolTable, node.children.get(0)) == "num"){
                    return true;
                }else{
                    return false;
                }
            }else{
                if(typeOf(symbolTable, node.children.get(0)) == typeOf(symbolTable, node.children.get(2))){
                    return true;
                }else{
                    return false;
                }
            }
        }else if(node.identifier.identifier == "BRANCH"){
            for(Node child : node.children){
                if(child.identifier.identifier == "COND"){
                    if(typeOf(symbolTable, child) == "bool"){
                        
                    }else{
                        return false;
                    }
                }
                if(child.identifier.identifier == "ALGO"){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
                else if(child.identifier.identifier == "if" || child.identifier.identifier == "else" || child.identifier.identifier == "then"){
                    childrenTypeChecks[node.children.indexOf(child)] = true;
                }
            }
            for(boolean check : childrenTypeChecks){
                if(check == false){
                    return false;
                }
            }
            return true;
        }else if(node.identifier.identifier == "FUNCTIONS"){
            // epsilon transition
            if(node.children.size() == 0){
                return true;
            }
            for(Node child : node.children){
                if(child.identifier.identifier == "FUNCTIONS" || child.identifier.identifier == "DECL"){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
            }
        }else if(node.identifier.identifier == "DECL"){
            for(Node child: node.children){
                if(child.identifier.identifier == "HEADER" || child.identifier.identifier == "BODY"){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
            }
            for(boolean check : childrenTypeChecks){
                if(check == false){
                    return false;
                }
            }
            return true;
        }else if(node.identifier.identifier == "HEADER"){
            int count = 0;
            for(Node child : node.children){
                if(child.identifier.identifier == "VNAME"){
                    if(typeOf(symbolTable, child) == "n"){
                        count++;
                    }else{
                        return false;
                    }
                }
            }
            // HEADER -> FTYP FNAME(VNAME, VNAME, VNAME)
            if(count == 3){
                return true;
            }else{
                return false;
            }
        }else if(node.identifier.identifier == "BODY"){
            for(Node child : node.children){
                if(child.identifier.identifier == "PROLOG" || child.identifier.identifier == "LOCVARS" || child.identifier.identifier == "ALGO" || child.identifier.identifier == "EPILOG" || child.identifier.identifier == "SUBFUNCS"){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);                    
                }else if(child.token != null && child.token.tokenValue == "end"){
                    childrenTypeChecks[node.children.indexOf(child)] = true;
                }
            }
            for(boolean check : childrenTypeChecks){
                if(check == false){
                    return false;
                }
            }
            return true;
        }else if(node.identifier.identifier == "PROLOG" || node.identifier.identifier == "EPILOG"){
            return true;
        }else if(node.identifier.identifier == "LOCVARS"){

        }else if(node.identifier.identifier == "SUBFUNCS"){
            // only has 1 child aka FUNCTIONS
            return typeCheck(node.children.get(0), symbolTable);
        }
        return true;
    }

    public String typeOf(Symbol_Table symbol_Table, Node node){
        // Passes in nodes and returns the type, type = child of that node
        Node child = node.children.get(0);
        if(child != null){
            if(node.identifier.identifier == "VNAME"){
                if(child.token != null || child.token.tokenValue == "num"){
                    return "n";
                }else if(child.token != null || child.token.tokenValue == "text"){
                    return "t";
                }
            }else if(node.identifier.identifier == "ATOMIC"){
                // child can either be VNAME or CONST
                return typeOf(symbol_Table, child);
            }else if(node.identifier.identifier == "CONST"){
                // token can be of class N/numbers or T/text
                // test if its a num with regex expression
                String numberClassRegex = "^(0|[0-9]*[1-9]|-0[0-9]*[1-9]|[1-9][0-9]*|-[1-9][0-9]*|[1-9][0-9]*.[0-9]*[1-9]|-[1-9][0-9]*.[0-9]*[1-9])$";
                if(child.token != null && child.token.tokenValue.matches(numberClassRegex)){
                    return "n";
                }
                String textClassRegex = "^([A-Z][a-z]{1,8})$";
                if(child.token != null && child.token.tokenValue.matches(textClassRegex)){
                    return "t";
                }
            }else if(node.identifier.identifier == "TERM"){
                return typeOf(symbol_Table, child);
            }else if(node.identifier.identifier == "CALL"){
                for(Node childNode : node.children){
                // check if all 3 ATOMIC nodes are type "n". if it is return the typeOf(FNAME), else return 'u'
                    if(childNode.identifier.identifier == "ATOMIC"){
                        if(typeOf(symbol_Table, childNode) != "n"){
                            return "u";
                        }
                    }
                }
                // return typeOf(FNAME)
                return typeOf(symbol_Table, node.children.get(0));
            }else if(node.identifier.identifier == "OP"){
                if(node.children.get(0).identifier.identifier == "UNOP"){
                    if(typeOf(symbol_Table, node.children.get(0)) == typeOf(symbol_Table, node.children.get(2)) && typeOf(symbol_Table, node.children.get(0)) == "b"){
                        return "b";
                    }else if(typeOf(symbol_Table, node.children.get(0)) == typeOf(symbol_Table, node.children.get(2)) && typeOf(symbol_Table, node.children.get(0)) == "n"){
                        return "n";
                    }else{
                        return "u";
                    }
                }else if(node.children.get(0).identifier.identifier == "BINOP"){
                    if(typeOf(symbol_Table, node.children.get(0)) == typeOf(symbol_Table, node.children.get(2)) && typeOf(symbol_Table, node.children.get(0)) == typeOf(symbol_Table, node.children.get(4)) && typeOf(symbol_Table, node.children.get(0)) == "n"){
                        return "n";
                    }else if(typeOf(symbol_Table, node.children.get(0)) == typeOf(symbol_Table, node.children.get(2)) && typeOf(symbol_Table, node.children.get(0)) == typeOf(symbol_Table, node.children.get(4)) && typeOf(symbol_Table, node.children.get(0)) == "b"){
                        return "b";
                    }else if(typeOf(symbol_Table, node.children.get(0)) == "c" && typeOf(symbol_Table, node.children.get(2)) == "n" && typeOf(symbol_Table, node.children.get(4)) == "n"){
                        return "b";
                    }else{
                        return "u";
                    }
                } 
            }else if(node.identifier.identifier == "ARG"){
                // return typeOf(ATOMIC) || typeOf(OP)
                return typeOf(symbol_Table, child);
            }else if(node.identifier.identifier == "UNOP"){
                if(child.token != null && child.token.tokenValue == "not"){
                    return "b";
                }else if(child.token != null && child.token.tokenValue == "sqrt"){
                    return "n";
                } 
            }
        }

            
        return "";
    }

}
