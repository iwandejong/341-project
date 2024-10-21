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
        System.out.println("Current node:" + node.identifier.identifier);
        if(node.children.size() == 0){
            System.out.println("No children");
            return true;
        }
        boolean[] childrenTypeChecks = new boolean[node.children.size()];
        System.out.println("Children size: " + node.children.size());
        if(node.identifier.identifier.equals("PROG")){
            System.out.println("in PROG");
            // check children
            for(Node child : node.children){
                System.out.println("Current child: " + child.identifier.identifier);
                if(child != null && child.token != null && !child.token.tokenValue.equals("main")){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }else if(child != null && child.token != null && child.token.tokenValue.equals("main")){
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
        }else if(node.identifier.identifier.equals("GLOBVARS")){
            System.out.println("In GLOBVARS");
            // epsilon transition
            if(node.children.size() == 0){
                return true;
            }

            for(Node child : node.children){
                if(child.identifier.identifier.equals("GLOBVARS")){
                    return childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
            }
        }else if(node.identifier.identifier.equals("ALGO")){
            // epsilon transition
            for(Node child : node.children){
                if(child.identifier.identifier.equals("ALGO")){
                    return childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
            }
        }else if(node.identifier.identifier.equals("INSTRUC")){
            // epsilon transition
            if(node.children.size() == 0){
                return true;
            }
            for(Node child : node.children){
                if(child.identifier.identifier.equals("INSTRUC") || child.identifier.identifier.equals("COMMAND")){
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
        }else if(node.identifier.identifier.equals("COMMAND")){
            for(Node child: node.children){
                if(child.token != null && child.token.tokenValue.equals("skip") || child.token.tokenValue.equals("halt")){
                    return true;
                }
                if(child.identifier.identifier.equals("ATOMIC")){
                    if(typeOf(symbolTable, child).equals("num") || typeOf(symbolTable, child).equals("text")){
                        return true;
                    }else{
                        return false;
                    }
                }
                if(child.identifier.identifier.equals("ASSIGN") || child.identifier.identifier.equals("BRANCH")){
                    return typeCheck(child, symbolTable);
                }
                if(child.identifier.identifier.equals("CALL")){
                    if(typeOf(symbolTable, child).equals("void")){
                        return true;
                    }else{
                        return false;
                    }
                }
                // TODO: return ATOMIC for FUNCTIONS
            }
        }else if(node.identifier.identifier.equals("ASSIGN")){
            // 2 types of assign: VNAME < input, VNAME = TERM. 
            // VNAME < input. 0 = VNAME, 1 = <, 2 = input
            if(node.children.get(2).token != null && node.children.get(2).token.tokenValue.equals("input")){
                if(typeOf(symbolTable, node.children.get(0)).equals("num")){
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
        }else if(node.identifier.identifier.equals("BRANCH")){
            for(Node child : node.children){
                if(child.identifier.identifier.equals("COND")){
                    if(typeOf(symbolTable, child).equals("bool")){
                        
                    }else{
                        return false;
                    }
                }
                if(child.identifier.identifier.equals("ALGO")){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
                else if(child.identifier.identifier.equals("if") || child.identifier.identifier.equals("else") || child.identifier.identifier.equals("then")){
                    childrenTypeChecks[node.children.indexOf(child)] = true;
                }
            }
            for(boolean check : childrenTypeChecks){
                if(check == false){
                    return false;
                }
            }
            return true;
        }else if(node.identifier.identifier.equals("FUNCTIONS")){
            // epsilon transition
            if(node.children.size() == 0){
                return true;
            }
            for(Node child : node.children){
                if(child.identifier.identifier.equals("FUNCTIONS") || child.identifier.identifier.equals("DECL")){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
            }
        }else if(node.identifier.identifier.equals("DECL")){
            for(Node child: node.children){
                if(child.identifier.identifier.equals("HEADER") || child.identifier.identifier.equals("BODY")){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
            }
            for(boolean check : childrenTypeChecks){
                if(check == false){
                    return false;
                }
            }
            return true;
        }else if(node.identifier.identifier.equals("HEADER")){
            int count = 0;
            for(Node child : node.children){
                if(child.identifier.identifier.equals("VNAME")){
                    if(typeOf(symbolTable, child).equals("n")){
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
        }else if(node.identifier.identifier.equals("BODY")){
            for(Node child : node.children){
                if(child.identifier.identifier.equals("PROLOG") || child.identifier.identifier.equals("LOCVARS") || child.identifier.identifier.equals("ALGO") || child.identifier.identifier.equals("EPILOG") || child.identifier.identifier.equals("SUBFUNCS")){
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);                    
                }else if(child.token != null && child.token.tokenValue.equals("end")){
                    childrenTypeChecks[node.children.indexOf(child)] = true;
                }
            }
            for(boolean check : childrenTypeChecks){
                if(check == false){
                    return false;
                }
            }
            return true;
        }else if(node.identifier.identifier.equals("PROLOG") || node.identifier.identifier.equals("EPILOG")){
            return true;
        }else if(node.identifier.identifier.equals("LOCVARS")){

        }else if(node.identifier.identifier.equals("SUBFUNCS")){
            // only has 1 child aka FUNCTIONS
            return typeCheck(node.children.get(0), symbolTable);
        }
        System.out.println("returning true");
        return true;
    }

    public String typeOf(Symbol_Table symbol_Table, Node node){
        // Passes in nodes and returns the type, type = child of that node
        Node child = node.children.get(0);
        if(child != null){
            if(node.identifier.identifier.equals("VNAME")){
                if(child.token != null || child.token.tokenValue.equals("num")){
                    return "n";
                }else if(child.token != null || child.token.tokenValue.equals("text")){
                    return "t";
                }
            }else if(node.identifier.identifier.equals("ATOMIC")){
                // child can either be VNAME or CONST
                return typeOf(symbol_Table, child);
            }else if(node.identifier.identifier.equals("CONST")){
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
            }else if(node.identifier.identifier.equals("TERM")){
                return typeOf(symbol_Table, child);
            }else if(node.identifier.identifier.equals("CALL")){
                for(Node childNode : node.children){
                // check if all 3 ATOMIC nodes are type "n". if it is return the typeOf(FNAME), else return 'u'
                    if(childNode.identifier.identifier.equals("ATOMIC")){
                        if(!typeOf(symbol_Table, childNode).equals("n")){
                            return "u";
                        }
                    }
                }
                // return typeOf(FNAME)
                return typeOf(symbol_Table, node.children.get(0));
            }else if(node.identifier.identifier.equals("OP")){
                if(node.children.get(0).identifier.identifier.equals("UNOP")){
                    if(typeOf(symbol_Table, node.children.get(0)).equals(typeOf(symbol_Table, node.children.get(2))) && typeOf(symbol_Table, node.children.get(0)).equals("b")){
                        return "b";
                    }else if(typeOf(symbol_Table, node.children.get(0)).equals(typeOf(symbol_Table, node.children.get(2))) && typeOf(symbol_Table, node.children.get(0)).equals("n")){
                        return "n";
                    }else{
                        return "u";
                    }
                }else if(node.children.get(0).identifier.identifier.equals("BINOP")){
                    if(typeOf(symbol_Table, node.children.get(0)) == typeOf(symbol_Table, node.children.get(2)) && typeOf(symbol_Table, node.children.get(0)) == typeOf(symbol_Table, node.children.get(4)) && typeOf(symbol_Table, node.children.get(0)).equals("n")){
                        return "n";
                    }else if(typeOf(symbol_Table, node.children.get(0)) == typeOf(symbol_Table, node.children.get(2)) && typeOf(symbol_Table, node.children.get(0)) == typeOf(symbol_Table, node.children.get(4)) && typeOf(symbol_Table, node.children.get(0)).equals("b")){
                        return "b";
                    }else if(typeOf(symbol_Table, node.children.get(0)).equals("c") && typeOf(symbol_Table, node.children.get(2)).equals("n") && typeOf(symbol_Table, node.children.get(4)).equals("n")){
                        return "b";
                    }else{
                        return "u";
                    }
                } 
            }else if(node.identifier.identifier.equals("ARG")){
                // return typeOf(ATOMIC) || typeOf(OP)
                return typeOf(symbol_Table, child);
            }else if(node.identifier.identifier.equals("UNOP")){
                if(child.token != null && child.token.tokenValue.equals("not")){
                    return "b";
                }else if(child.token != null && child.token.tokenValue.equals("sqrt")){
                    return "n";
                } 
            }
        }

            
        return "";
    }

}
