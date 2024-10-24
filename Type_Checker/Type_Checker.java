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
            throw new RuntimeException("Type Checking Failed");
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
        if(node.identifier.identifier.equals("PROG")){
            // check children
            // System.out.println("size of children:" + node.children.size());
            // int count = 0;
            for(Node child : node.children){
                // check if each child returns true
                // System.out.println("Checking child:" + count++ + "name:" + child.identifier.identifier);
                // if(child != null && child.identifier != null && child.identifier.identifier.equals("GLOBVARS")){
                //     System.out.println("Checking Globvars in PROG returns:" + typeCheck(child, symbolTable));
                //     // childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                // }else if(child != null && child.token != null && child.token.tokenValue.equals("ALGO")){
                //     System.out.println("Checking Algo in PROG returns:" + typeCheck(child, symbolTable));

                // }else if(child != null && child.token != null && child.token.tokenValue.equals("FUNCTIONS")){
                //     System.out.println("Checking Functions in PROG returns:" + typeCheck(child, symbolTable));
                // }

                if(child != null && child.identifier.identifier.equals("GLOBVARS") || child.identifier.identifier.equals("FUNCTIONS") || child.identifier.identifier.equals("ALGO")){
                    if(child.identifier.identifier.equals("GLOBVARS")){
                        // System.out.println("Checking GLOBVARS in PROG returns:" + typeCheck(child, symbolTable));
                    }else if(child.identifier.identifier.equals("FUNCTIONS")){
                        // System.out.println("Checking FUNCTIONS in PROG returns:" + typeCheck(child, symbolTable));
                    }else if(child.identifier.identifier.equals("ALGO")){
                        // System.out.println("Checking ALGO in PROG returns:" + typeCheck(child, symbolTable));
                    }

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
                if(child.identifier.identifier.equals("INSTRUC")){
                    // System.out.println("Chekcing INSTRUC in ALGO returns:" + typeCheck(child, symbolTable));
                    return typeCheck(child, symbolTable);
                }
            }
        }else if(node.identifier.identifier.equals("INSTRUC")){
            // epsilon transition
            if(node.children.size() == 0){
                return true;
            }
            for(Node child : node.children){
                if(child.identifier.identifier.equals("INSTRUC") || child.identifier.identifier.equals("COMMAND")){
                    // if(child.identifier.identifier.equals("COMMAND") && !typeCheck(child, symbolTable)){
                    //     System.out.println("Checking COMMAND in INSTRUC returns:" + typeCheck(child, symbolTable));
                    // }else if(child.identifier.identifier.equals("INSTRUC") && !typeCheck(child, symbolTable)){
                    //     System.out.println("Checking INSTRUC in INSTRUC returns:" + typeCheck(child, symbolTable));
                    // }
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
                if (child != null && child.token != null && child.token.tokenValue != null && (child.token.tokenValue.equals("skip") || child.token.tokenValue.equals("halt"))){
                    return true;
                }
                if(child.identifier.identifier.equals("ATOMIC")){
                    if(typeOf(symbolTable, child).equals("n") || typeOf(symbolTable, child).equals("t")){
                        return true;
                    }else{
                        // System.out.println("ATOMIC returns false");
                        return false;
                    }
                }
                if(child.identifier.identifier.equals("ASSIGN") || child.identifier.identifier.equals("BRANCH")){
                    if(child.identifier.identifier.equals("ASSIGN") && !typeCheck(child, symbolTable)){
                        // System.out.println("Checking ASSIGN in COMMAND returns:" + typeCheck(child, symbolTable));
                    }else if(child.identifier.identifier.equals("BRANCH") && !typeCheck(child, symbolTable)){
                        // System.out.println("Checking BRANCH in COMMAND returns:" + typeCheck(child, symbolTable));
                    }
                
                    return typeCheck(child, symbolTable);
                }
                if(child.identifier.identifier.equals("CALL")){
                    if(typeOf(symbolTable, child).equals("v")){
                        // System.out.println("CALL returns true");
                        return true;
                    }else{
                        System.out.println("typeOf(symbolTable, CALL) returns:" + typeOf(symbolTable, child));
                        System.out.println("CALL returns false");
                        return false;
                    }
                }
                // System.out.println("COMMAND returns true");
                return true;
                // TODO: return ATOMIC for FUNCTIONS
            }
        }else if(node.identifier.identifier.equals("ASSIGN")){
            // 2 types of assign: VNAME < input, VNAME = TERM. 
            // VNAME < input. 0 = VNAME, 1 = <, 2 = input
            if(node.children.get(2).token != null && node.children.get(2).token.tokenValue.equals("input")){
                if(typeOf(symbolTable, node.children.get(0)).equals("n")){
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
                    if(typeOf(symbolTable, child).equals("b")){
                        childrenTypeChecks[node.children.indexOf(child)] = true;
                    }else{
                        // System.out.println("COND returns false in BRANCH");
                        // System.out.println("typeOf(symbolTable, COND) returns:" + typeOf(symbolTable, child));
                        return false;
                    }
                }
                if(child.identifier.identifier.equals("ALGO")){
                    if(typeCheck(child, symbolTable) == false){
                        // System.out.println("ALGO returns false in BRANCH");
                        return false;
                    }
                        
                    childrenTypeChecks[node.children.indexOf(child)] = typeCheck(child, symbolTable);
                }
                else if(child.identifier.identifier.equals("if") || child.identifier.identifier.equals("else") || child.identifier.identifier.equals("then")){
                    childrenTypeChecks[node.children.indexOf(child)] = true;
                }
            }
            int count = 0;
            for(boolean check : childrenTypeChecks){
                // System.out.println("position of check: " + count++ + " has " + check);
                if(check == false){
                    // System.out.println("BRANCH returns false with check loop");
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
            for(boolean check : childrenTypeChecks){
                if(check == false){
                    return false;
                }
            }
            return true;
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
            // find all children with fname
            String Fname = node.children.get(1).children.get(0).token.tokenValue;
            // check if Fname is in symbol table
            if(symbolTable.lookupName(Fname) != null){
                // change the declaration type of the function to the type of the function
                
                String type = typeOf(symbolTable, node.children.get(0));
                // System.out.println("Type of function: " + type);
                if(type.equals("v")){
                    type = "void";
                }else if(type.equals("n")){
                    type = "num";
                }
                // go through all function declarations and change the declaration type
                // System.out.println("Fname: " + Fname + " type: " + type + "count: " + symbolTable.findIds(Fname).length);
                // System.out.println("Ids to change:" + symbolTable.findIds(Fname)[0] + "," + symbolTable.findIds(Fname)[1] );
                for(String innerSymbolTable : symbolTable.findIds(Fname)){
                    symbolTable.table.get(innerSymbolTable).declarationType = type;
                }
            }
            
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
            return true;
        }else if(node.identifier.identifier.equals("SUBFUNCS")){
            // only has 1 child aka FUNCTIONS
            return typeCheck(node.children.get(0), symbolTable);
        }
        return true;
    }

    public String typeOf(Symbol_Table symbol_Table, Node node){
        // Passes in nodes and returns the type, type = child of that node
        Node child = node.children.get(0);
        if(child != null){
            if(node.identifier.identifier.equals("VTYP")){
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
                            // System.out.println("ATOMIC lookup returns:" + typeOf(symbol_Table, childNode));
                            return "u";
                        }
                    }
                }
                // return typeOf(FNAME)
                // System.out.println("typeOf(FNAME) returns:" + typeOf(symbol_Table, node.children.get(0)));
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
            }else if(node.identifier.identifier.equals("BINOP")){
                if(child.token != null && child.token.tokenValue.equals("and") || child.token.tokenValue.equals("or")){
                    return "b";
                }else if(child.token != null && child.token.tokenValue.equals("eq") || child.token.tokenValue.equals("grt")){
                    return "c";
                }else if(child.token != null && child.token.tokenValue.equals("add") || child.token.tokenValue.equals("sub") || child.token.tokenValue.equals("mul") || child.token.tokenValue.equals("div")){
                    return "n";
                }
            }else if(node.identifier.identifier.equals("COND")){
                return typeOf(symbol_Table, node.children.get(0));
            }else if(node.identifier.identifier.equals("SIMPLE")){
                // if typeof(BINOP) == typeof(ATOMIC1) == typeof(ATOMIC2) == 'b' then  return 'b'
                if(typeOf(symbol_Table, node.children.get(0)).equals("b") && typeOf(symbol_Table, node.children.get(2)).equals("b") && typeOf(symbol_Table, node.children.get(4)).equals("b")){
                    return "b";
                }else if(typeOf(symbol_Table, node.children.get(0)).equals("c") && typeOf(symbol_Table, node.children.get(2)).equals("n") && typeOf(symbol_Table, node.children.get(4)).equals("n")){
                    return "b";
                }else{
                    return "u";
                }              
            }else if(node.identifier.identifier.equals("COMPOSIT")){
                // if typeof(BINOP) == typeof(ATOMIC1) == typeof(ATOMIC2) == 'b' then  return 'b'
                if(node.children.get(0).identifier.identifier.equals("BINOP") && typeOf(symbol_Table, node.children.get(0)).equals("b") && typeOf(symbol_Table, node.children.get(2)).equals("b") && typeOf(symbol_Table, node.children.get(4)).equals("b")){
                    return "b";
                }else if(node.children.get(0).identifier.identifier.equals("UNOP") && typeOf(symbol_Table, node.children.get(0)).equals("b") && typeOf(symbol_Table, node.children.get(2)).equals("b")){
                    return "b";
                }else{
                    return "u";
                }
            }else if(node.identifier.identifier.equals("FTYP")){
                if(child.token != null && child.token.tokenValue.equals("num")){
                    return "n";
                }else if(child.token != null && child.token.tokenValue.equals("void")){
                    return "v";
                }
            }else if(node.identifier.identifier.equals("VNAME")){
                // check if VNAME is in symbol table
                if(symbol_Table.lookupName(child.token.tokenValue) != null){
                    if(symbol_Table.lookupName(child.token.tokenValue).declarationType.equals("num")){
                        return "n";
                    }else if(symbol_Table.lookupName(child.token.tokenValue).declarationType.equals("text")){
                        return "t";
                    }
                    else{
                        
                        return "u";
                    }
                }  
            }else if(node.identifier.identifier.equals("FNAME")){
                // check if FNAME is in symbol table
                if(symbol_Table.lookupName(child.token.tokenValue) != null){
                    if(symbol_Table.lookupName(child.token.tokenValue).declarationType.equals("num")){
                        return "n";
                    }
                    else if(symbol_Table.lookupName(child.token.tokenValue).declarationType.equals("void")){
                        return "v";
                    }else{
                        return "u";
                    }
                }
            }
                     
        }
        return "u";
    }

}
