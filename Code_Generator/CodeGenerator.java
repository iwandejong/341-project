package Code_Generator;
import Parser.*;
import java.util.*;

import Analysis.Symbol_Table;

public class CodeGenerator {
    List<ProductionRule> productionRules = new ArrayList<>();
    Tree syntaxTree = new Tree();
    Symbol_Table symbolTable = new Symbol_Table();
    String newstring = "";
    String newnum = "";
    String newlabel = "";

    public void generateCode(List<ProductionRule> _productionRules, Tree _syntaxTree, Symbol_Table _symbolTable) {
        productionRules = _productionRules;
        syntaxTree = _syntaxTree;
        symbolTable = _symbolTable;

        // find first ALGO node from ROOT and create a tree from it
        List<Node> root = syntaxTree.getNodes("ALGO");
        root.get(0).parent = null;
        Tree ALGO = new Tree(root.get(0));

        String code = PROG(ALGO, null);
        System.out.println(code);
        System.out.println("\u001B[32m" + "Code Generation Successfully Completed." + "\u001B[0m");
    }

    public String newvar(String type) throws RuntimeException {
        if (type == "N") {
            if (newnum == "") {
                newnum = "N0%";
                return " " + newnum + " ";
            }
            int temp = Integer.parseInt(newnum.substring(1, newnum.length() - 1));
            newnum = "V" + (temp + 1) + "%";
            return " " + newnum + " ";
        } else if (type == "T") {
            if (newstring == "") {
                newstring = "T0$";
                return " " + newstring + " ";
            }
            int temp = Integer.parseInt(newstring.substring(1, newstring.length() - 1));
            newstring = "T" + (temp + 1) + "$";
            return " " + newstring + " ";
        }

        throw new RuntimeException("Invalid type.");
    }

    public String newlabel() throws RuntimeException {
        if (newlabel == "") {
            newlabel = "L1";
            return " " + newlabel + " ";
        }
        int temp = Integer.parseInt(newlabel.substring(1));
        newlabel = "L" + (temp + 1);
        return " " + newlabel + " ";
    }

    public Tree newBaseSubTree(Tree tree, String identifier) {
        List<Node> nodes = tree.getNodes(identifier);
        nodes.get(0).parent = null;
        return new Tree(nodes.get(0));
    }

    // * override newBaseSubTree to get a specific node from the list of nodes
    public Tree newBaseSubTree(Tree tree, String identifier, int index) {
        List<Node> nodes = tree.getNodes(identifier);
        nodes.get(index).parent = null;
        return new Tree(nodes.get(index));
    }

    // GLOBVARS ::=
    // GLOBVARS1 ::= VTYP VNAME , GLOBVARS2
    // The variable declarations were needed only for Scope-Analysis and for Type-Checking.
    // They remain un-translated (can be ignored by the translator).
    // VTYP ::= num
    // VTYP ::= text
    // The type declarations were needed only for Scope-Analysis and for Type-Checking.
    // They remain un-translated (can be ignored by the translator).
    // VNAME ::= a token of Token-Class V from the Lexer
    // The user-defined names were already re-named in the foregoing Scope Analysis.
    // The translator function can find their new names in the Symbol Table.
    private String VNAME (Tree VNAME, String place) {
        // x = lookup(vtable, getname(id))
        // [place := x]

        // take the first child of VNAME
        Node node = VNAME.root.children.get(0);

        // get the value of the token
        String token = node.token.tokenValue;

        // get the new name from the symbol table
        String newName = symbolTable.lookupName(token).value;

        return place + " := " + newName;
    }

    // PROG ::= main GLOBVARS ALGO FUNCTIONS
    // The source-word main remains un-translated (can be ignored by the translator).
    // We translate ALGO, and append behind the ALGO-code the translation of FUNCTIONS.
    // Also important is the generation of a stop command behind ALGO, such that the running
    // main-code will not continue to run into the program code of the functions in target code
    // within the same target-code-file!
    // Thus:
    // translation(PROG) must return the target-code-string aCode++" STOP "++fCode
    // whereby aCode = translation(ALGO), and fCode = translation(FUNCTIONS)
    // * PASSING FUNCTION
    private String PROG (Tree ALGO, Tree FUNCTIONS) {
        Tree INSTRUC = newBaseSubTree(ALGO, "INSTRUC");
        String aCode = ALGO(INSTRUC);

        // String fCode = FUNCTIONS(FUNCTIONS);
        return aCode + " STOP ";
    }

    // ALGO ::= begin INSTRUC end
    // The source-words begin and end remain un-translated (can be ignored by the translator).
    // Thus: translate(ALGO) = translate(INSTRUC)
    // * PASSING FUNCTION
    private String ALGO (Tree INSTRUC) {
        if (INSTRUC.root.children.size() == 0) {
            return INSTRUC();
        }
        return INSTRUC(INSTRUC);
    }

    // INSTRUC ::=
    // For this case, the translator function shall return the target-code-string " REM END "
    // Comment: In our Target-Language, REM represents a non-executable remark
    // * RETURNING FUNCTION
    private String INSTRUC () {
        return " REM END ";
    }

    // INSTRUC1 ::= COMMAND ; INSTRUC2
    // Translate this sequence such as Stat1 ; Stat2 in Figure 6.5 of our Textbook.
    // * PASSING FUNCTION
    private String INSTRUC (Tree INSTRUC) {
        if (INSTRUC.root.children.size() == 0) {
            return INSTRUC();
        }

        Tree COMMAND = newBaseSubTree(INSTRUC, "COMMAND");

        // get last child of INSTRUC
        if (INSTRUC.root.children.size() == 3) {
            Node lastChild = INSTRUC.root.children.get(2);
            lastChild.parent = null;
            Tree INSTRUC2 = new Tree(lastChild);
            
            return COMMAND(COMMAND) + INSTRUC(INSTRUC2);
        }
        return COMMAND(COMMAND) + INSTRUC();
    }

    private String COMMAND (Tree COMMAND) throws RuntimeException {
        switch (COMMAND.root.children.get(0).identifier.identifier) {
            case "skip":
                return COMMAND_SKIP();
            case "halt":
                return COMMAND_HALT();
            case "print":
                return COMMAND_PRINT(COMMAND, newvar("T"));
            case "return":
                return COMMAND_ATOMIC(COMMAND, newvar("T"));
            case "ASSIGN":
                return COMMAND_ASSIGN(COMMAND, newvar("T"));
            case "CALL":
                return COMMAND_CALL(COMMAND);
            case "BRANCH":
                return COMMAND_BRANCH(COMMAND);
            default:
                throw new RuntimeException("Invalid COMMAND.");
        }
    }

    // COMMAND ::= skip
    // For this case, the translator function returns the code-string " REM DO NOTHING "
    private String COMMAND_SKIP () {
        return " REM DO NOTHING ";
    }

    // COMMAND ::= halt
    // For this case, the translator function must return the code-string " STOP "
    private String COMMAND_HALT () {
        return " STOP ";
    }

    // COMMAND ::= print ATOMIC
    // codeString = translate(ATOMIC)
    // return( "PRINT"++" "++codeString )
    private String COMMAND_PRINT (Tree COMMAND, String place) {
        Tree ATOMIC = newBaseSubTree(COMMAND, "ATOMIC");
        String codeString = ATOMIC(ATOMIC, place);
        return "PRINT" + " " + codeString;
    }

    // COMMAND ::= return ATOMIC // Only for Project Phase 5b, NOT for Project Phase 5a!
    // The return ATOMIC command must stand 'inside' of a Function-Scope!
    // We assume that Scope-Analysis has checked this already! If the return ATOMIC command
    // was found inside the MAIN program then a semantic error must have already been thrown
    // in the Semantic Analysis phase, such that the translation phase would not even start.
    // Advice: Translation as per Chapter #9 of our Textbook, or per INLINING (as lectured).
    private String COMMAND_ATOMIC (Tree COMMAND, String place) {
        Tree ATOMIC = newBaseSubTree(COMMAND, "ATOMIC");
        String codeString = ATOMIC(ATOMIC, place);
        return "RETURN" + " " + codeString;
    }

    // COMMAND ::= ASSIGN translate(COMMAND) = translate(ASSIGN)
    private String COMMAND_ASSIGN (Tree COMMAND, String place) {
        Tree ASSIGN = newBaseSubTree(COMMAND, "ASSIGN");

        if (ASSIGN.root.children.get(2).identifier.identifier.equals("input")) {
            return ASSIGN_INPUT(ASSIGN, place);
        } else {
            return ASSIGN_TERM(ASSIGN);
        }
    }

    // COMMAND ::= CALL translate(COMMAND) = translate(CALL)
    private String COMMAND_CALL (Tree COMMAND) {
        return "";
    }

    // COMMAND ::= BRANCH translate(COMMAND) = translate(BRANCH)
    private String COMMAND_BRANCH (Tree COMMAND) {
        Tree BRANCH = newBaseSubTree(COMMAND, "BRANCH");
        return BRANCH(BRANCH);
    }

    // ATOMIC ::= VNAME
    // translate(ATOMIC) → returns as code-string the new name of VNAME as found in the Symbol Table
    // ATOMIC ::= CONST translate(ATOMIC) = translate(CONST)
    private String ATOMIC (Tree ATOMIC, String place) throws RuntimeException {

        if (ATOMIC.root.children.get(0).identifier.identifier.equals("VNAME")) {
            Tree VNAME = newBaseSubTree(ATOMIC, "VNAME");

            return VNAME(VNAME, place);
        } else if (ATOMIC.root.children.get(0).identifier.identifier.equals("CONST")) {
            Tree CONST = newBaseSubTree(ATOMIC, "CONST");

            return CONST(CONST, place);
        }
        throw new RuntimeException("Invalid ATOMIC.");
    }

    // CONST ::= a token of Token-Class N from the Lexer
    // CONST ::= a token of Token-Class T from the Lexer
    // Constants are translated to themselves.
    // Example for a number constant: translate(235) → return " 235 "
    // Example for a text constant: translate("hello") → return " "hello" "
    // ! Note that the returned code-string must also contain these "quotation marks" !
    private String CONST (Tree CONST, String place) throws RuntimeException {
        // v = getvalue(num)
        // [place := v]
        String token = CONST.root.children.get(0).token.tokenValue;
        if (CONST.root.children.get(0).token.tokenClass.equals("N")) {
            return place + " := " + token + " ";
        } else if (CONST.root.children.get(0).token.tokenClass.equals("T")) {
            return place + " := \"" + token + "\" ";
        }

        throw new RuntimeException("Invalid CONST.");
    }

    // ASSIGN ::= VNAME < input // The symbol < remains un-translated
    // codeString = translate(VNAME)
    // return( "INPUT"++" "++codeString )
    private String ASSIGN_INPUT (Tree ASSIGN, String place) {
        Tree VNAME = newBaseSubTree(ASSIGN, "VNAME");
        String codeString = ATOMIC(VNAME, place);
        return "INPUT" + " " + codeString;
    }

    // ASSIGN ::= VNAME = TERM
    // Translate this case such as id := Exp in Figure 6.5 of our Textbook.
    private String ASSIGN_TERM (Tree ASSIGN) {
        Tree VNAME = newBaseSubTree(ASSIGN, "VNAME");
        Tree TERM = newBaseSubTree(ASSIGN, "TERM");

        // place = newvar()
        // x = lookup(vtable, getname(id))
        // TransExp(Exp,vtable,ftable,place)++[x := place]

        String place = newvar("T");
        String x = VNAME(VNAME, place);
        String code = TERM(TERM);

        return code + x;
    }
    
    // TERM ::= ATOMIC translate(TERM) = translate(ATOMIC)
    // TERM ::= CALL translate(TERM) = translate(CALL)
    // TERM ::= OP translate(TERM) = translate(OP)
    private String TERM (Tree TERM) throws RuntimeException {
        if (TERM.root.children.get(0).identifier.identifier.equals("ATOMIC")) {
            Tree ATOMIC = newBaseSubTree(TERM, "ATOMIC");

            String place = newvar("T");

            return ATOMIC(ATOMIC, place);
        } else if (TERM.root.children.get(0).identifier.identifier.equals("CALL")) {
            // get all atomics
            Tree CALL = newBaseSubTree(TERM, "CALL");
            Tree ATOMIC1 = newBaseSubTree(CALL, "ATOMIC");
            Tree ATOMIC2 = newBaseSubTree(CALL, "ATOMIC", 1);
            Tree ATOMIC3 = newBaseSubTree(CALL, "ATOMIC", 2);
            return CALL(CALL, ATOMIC1, ATOMIC2, ATOMIC3);
        } else if (TERM.root.children.get(0).identifier.identifier.equals("OP")) {
            Tree OP = newBaseSubTree(TERM, "OP");

            String place = newvar("T");

            return OP(OP, place);
        }
        throw new RuntimeException("Invalid TERM.");
    }

    // CALL ::= FNAME( ATOMIC1 , ATOMIC2 , ATOMIC3 )
    // The internally generated new name for FNAME can already be found in the Symbol Table.
    // For non-executable intermediate code (Semester-Project Phase 5a),
    // you can translate function calls such as case id(Exps) in Figure 6.3 of our Textbook.
    // In our project, the translation is indeed much easier than in the Textbook, because we
    // know that we have exactly three parameters [not an indefinitely long list of parameters],
    // and we also know that our parameters are atomic [not the Textbook's parameters which
    // are possibly composite terms].
    // In other words:
    // translation( FNAME( ATOMIC1 , ATOMIC2 , ATOMIC3 ) )
    // returns simply the non-executable intermediate code-string:
    // "CALL_"++newNameforFNAME++"("++p1++","++p2++","++p3++")"
    // whereby
    // p1 = translation( ATOMIC1 )
    // p2 = translation( ATOMIC2 )
    // p3 = translation( ATOMIC3 )
    // all of which are either simple// For executable target code for the Function-Call (in Semester-Project Phase 5b),
    // the final translation will continue from there either by way of INLINING (as lectured),
    // or by the code generation method described in Chapter #9 of our Textbook (with stack).
    private String CALL(Tree CALL, Tree ATOMIC1, Tree ATOMIC2, Tree ATOMIC3) {
        String place1 = newvar("N");
        String place2 = newvar("N");
        String place3 = newvar("N");

        String p1 = ATOMIC(ATOMIC1, place1);
        String p2 = ATOMIC(ATOMIC2, place2);
        String p3 = ATOMIC(ATOMIC3, place3);
        String newName = FNAME(CALL);
        return "CALL_" + newName + "(" + p1 + "," + p2 + "," + p3 + ")";
    }

    // OP ::= UNOP( ARG ) Translate this case such as unop Exp1 in Figure 6.3 of our Textbook,
    // however with the brackets!
    // In other words:
    // return code1++place++":="++opName++"("++place1++")"
    // OP ::= BINOP( ARG1 , ARG2 )
    // Translate this case such as Exp1 binop Exp2 in Figure 6.3 of our Textbook.
    private String OP (Tree OP, String place) throws RuntimeException {
        if (OP.root.children.get(0).identifier.identifier.equals("UNOP")) {
            Tree ARG = newBaseSubTree(OP, "ARG");

            // place1 = newvar()
            // code1 = TransExp(Exp1,vtable,ftable,place1)
            // op = transop(getopname(unop))
            // code1++[place := op place1]
            String place1 = newvar("N");
            String code1 = ARG(ARG);
            String op = UNOP(OP);
            return code1 + place + " := " + op + place1;

        } else if (OP.root.children.get(0).identifier.identifier.equals("BINOP")) {
            Tree ARG1 = newBaseSubTree(OP, "ATOMIC");
            Tree ARG2 = newBaseSubTree(OP, "ATOMIC", 1);

            // place1 = newvar()
            // place2 = newvar()
            // code1 = TransExp(Exp1,vtable,ftable,place1)
            // code2 = TransExp(Exp2,vtable,ftable,place2)
            // op = transop(getopname(binop))
            // code1++code2++[place := place1 op place2]
            String place1 = newvar("N");
            String place2 = newvar("N");
            String code1 = ARG(ARG1);
            String code2 = ARG(ARG2);
            String op = BINOP(OP);
            return code1 + code2 + place + " := " + place1 + op + place2;
        }

        throw new RuntimeException("Invalid OP.");
    }

    // ARG ::= ATOMIC translate(ARG) = translate(ATOMIC)
    // ARG ::= OP translate(ARG) = translate(OP)
    private String ARG (Tree ARG) {
        if (ARG.root.children.get(0).identifier.identifier.equals("ATOMIC")) {
            Tree ATOMIC = newBaseSubTree(ARG, "ATOMIC");

            String place = newvar("N");

            return ATOMIC(ATOMIC, place);
        } else if (ARG.root.children.get(0).identifier.identifier.equals("OP")) {
            Tree OP = newBaseSubTree(ARG, "OP");

            String place = newvar("N");

            return OP(OP, place);
        }

        throw new RuntimeException("Invalid ARG.");
    }

    // UNOP ::= not
    // Important! Our Target-Language does not include in its own syntax any symbolic representation of
    // the Boolean negation operator not ! Wherever such a not occurs in a COMPOSIT COND of any
    // BRANCH statement, such a BRANCH statement must be translated as described in case ! Cond1
    // of Figure 6.8 of our Textbook whereby the then-code and the else-code of the if-then-else command
    // are getting swapped.
    // UNOP ::= sqrt // Numeric operation which yields a number's square root
    // translate(sqrt) → return "SQR" // That is the operator's syntax in our Target Language
    private String UNOP (Tree UNOP) throws RuntimeException {
        if (UNOP.root.children.get(0).identifier.identifier.equals("not")) {
            return "! ";
        } else if (UNOP.root.children.get(0).identifier.identifier.equals("sqrt")) {
            return "SQR";
        }

        throw new RuntimeException("Invalid UNOP.");
    }

    // BINOP ::= or
    // Important! Our Target-Language does not include in its own syntax any symbolic representation of
    // the Boolean disjunction operator or ! Wherever such an or occurs in a COMPOSIT COND of any
    // BRANCH statement, such a BRANCH statement must be translated such as described in case
    // Cond1 || Cond2 of Figure 6.8 of our Textbook, whereby cascading jumps to different labels will be
    // generated by the translator function.
    // BINOP ::= and
    // Important! Our Target-Language does not include in its own syntax any symbolic representation of
    // the Boolean conjunction operator and ! Wherever such an and occurs in a COMPOSIT COND of
    // any BRANCH statement, such a BRANCH statement must be translated such as described in case
    // Cond1 && Cond2 of Figure 6.8 of our Textbook, whereby cascading jumps to different labels will
    // be generated by the translator function.
    // BINOP ::= eq translate(eq) → return " = "
    // BINOP ::= grt translate(grt) → return " > "
    // BINOP ::= add translate(add) → return " + "
    // BINOP ::= sub translate(sub) → return " ‒ "
    // BINOP ::= mul translate(mul)→ return " * "
    // BINOP ::= div translate(div) → return " / "
    private String BINOP (Tree BINOP) throws RuntimeException {
        BINOP.visualiseTree(BINOP.root, "", true);
        if (BINOP.root.children.get(0).identifier.identifier.equals("or")) {
            return " || ";
        } else if (BINOP.root.children.get(0).identifier.identifier.equals("and")) {
            return " && ";
        } else if (BINOP.root.children.get(0).identifier.identifier.equals("eq")) {
            return " = ";
        } else if (BINOP.root.children.get(0).identifier.identifier.equals("grt")) {
            return " > ";
        } else if (BINOP.root.children.get(0).identifier.identifier.equals("add")) {
            return " + ";
        } else if (BINOP.root.children.get(0).identifier.identifier.equals("sub")) {
            return " - ";
        } else if (BINOP.root.children.get(0).identifier.identifier.equals("mul")) {
            return " * ";
        } else if (BINOP.root.children.get(0).identifier.identifier.equals("div")) {
            return " / ";
        }

        throw new RuntimeException("Invalid BINOP.");
    }

    // BRANCH ::= if COND then ALGO1 else ALGO2
    // If the COND is a COMPOSIT,
    // then translate the whole BRANCH command as in Figure 6.8 in the Textbook.
    // If the COND is SIMPLE,
    // then translate the whole BRANCH command as in Figure 6.5 of the Textbook,
    // case: if COND then Stat1 else Stat2
    private String BRANCH (Tree BRANCH) throws RuntimeException {
        Tree COND = newBaseSubTree(BRANCH, "COND");
        
        if (COND.root.children.get(0).identifier.identifier.equals("COMPOSIT")) {
            // COMPOSIT ::= BINOP ( SIMPLE , SIMPLE )
            // COMPOSIT ::= UNOP ( SIMPLE )
            if (BRANCH.root.children.size() == 5) {
                // label1 = newlabel()
                // label2 = newlabel()
                // label3 = newlabel()
                // code1 = TransCond(Cond,label1,label2,vtable,ftable)
                // code2 = TransStat(Stat1,vtable,ftable)
                // code3 = TransStat(Stat2,vtable,ftable)
                // code1++[LABEL label1]++code2++[GOTO label3, LABEL label2] ++code3++[LABEL label3]
                String label1 = newlabel();
                String label2 = newlabel();
                String label3 = newlabel();
                String code1 = COND(COND, label1, label2);
                String code2 = ALGO(newBaseSubTree(BRANCH, "ALGO"));
                String code3 = ALGO(newBaseSubTree(BRANCH, "ALGO", 1));
                return code1 + "LABEL " + label1 + code2 + "GOTO " + label3 + "LABEL " + label2 + code3 + "LABEL " + label3;
            } else if (BRANCH.root.children.size() == 3) {
                // label1 = newlabel()
                // label2 = newlabel()
                // code1 = TransCond(Cond,label1,label2,vtable,ftable)
                // code2 = TransStat(Stat1,vtable,ftable)
                // code1++[LABEL label1]++code2++[LABEL label2]
                String label1 = newlabel();
                String label2 = newlabel();
                String code1 = COND(COND, label1, label2);
                String code2 = ALGO(newBaseSubTree(BRANCH, "ALGO"));
                return code1 + "LABEL " + label1 + code2 + "LABEL " + label2;
            }
            
            throw new RuntimeException("Invalid BRANCH.");
        } else if (COND.root.children.get(0).identifier.identifier.equals("SIMPLE")) {

            // label1 = newlabel()
            // label2 = newlabel()
            // label3 = newlabel()
            // code1 = TransCond(Cond,label1,label2,vtable,ftable)
            // code2 = TransStat(Stat1,vtable,ftable)
            // code3 = TransStat(Stat2,vtable,ftable)
            // code1++[LABEL label1]++code2++[GOTO label3, LABEL label2] ++code3++[LABEL label3]
            String label1 = newlabel();
            String label2 = newlabel();
            String label3 = newlabel();
            String code1 = COND(COND, label1, label2);
            String code2 = ALGO(newBaseSubTree(BRANCH, "ALGO"));
            String code3 = ALGO(newBaseSubTree(BRANCH, "ALGO", 1));
            return code1 + "LABEL " + label1 + code2 + "GOTO " + label3 + "LABEL " + label2 + code3 + "LABEL " + label3;
        }

        throw new RuntimeException("Invalid BRANCH.");
    }
    
    // COND ::= SIMPLE Translation as explained above
    // COND ::= COMPOSIT Translation as explained above
    private String COND (Tree COND, String labelT, String labelF) throws RuntimeException {
        if (COND.root.children.get(0).identifier.identifier.equals("SIMPLE")) {
            Tree SIMPLE = newBaseSubTree(COND, "SIMPLE");
            Tree BINOP = newBaseSubTree(SIMPLE, "BINOP");
            Tree ATOMIC1 = newBaseSubTree(SIMPLE, "ATOMIC");
            Tree ATOMIC2 = newBaseSubTree(SIMPLE, "ATOMIC", 1);

            // t1 = newvar()
            // t2 = newvar()
            // code1 = TransExp(Exp1,vtable,ftable,t1)
            // code2 = TransExp(Exp2,vtable,ftable,t2)
            // op = transop(getopname(relop))
            // code1++code2++[IF t1 op t2 THEN labelt ELSE labelf ]
            String t1 = newvar("N");
            String t2 = newvar("N");
            String code1 = ATOMIC(ATOMIC1, t1);
            String code2 = ATOMIC(ATOMIC2, t2);
            String op = BINOP(BINOP);
            return code1 + code2 + "IF " + t1 + op + t2 + " THEN " + labelT + " ELSE " + labelF;
        } else if (COND.root.children.get(0).identifier.identifier.equals("COMPOSIT")) {
            Tree COMPOSIT = newBaseSubTree(COND, "COMPOSIT");

            return COMPOSIT(COMPOSIT, labelT, labelF);
        }

        throw new RuntimeException("Invalid COND.");
    }

    // SIMPLE ::= BINOP( ATOMIC1 , ATOMIC2 ) Translation as explained above
    // COMPOSIT ::= BINOP( SIMPLE1 , SIMPLE2 ) Translation as explained above
    // COMPOSIT ::= UNOP ( SIMPLE ) Translation as explained above
    private String SIMPLE (Tree SIMPLE, String place) throws RuntimeException {
        if (SIMPLE.root.children.get(0).identifier.identifier.equals("BINOP")) {
            Tree ATOMIC1 = newBaseSubTree(SIMPLE, "ATOMIC");
            Tree ATOMIC2 = newBaseSubTree(SIMPLE, "ATOMIC", 1);
            Tree BINOP = newBaseSubTree(SIMPLE, "BINOP");

            // place1 = newvar()
            // place2 = newvar()
            // code1 = TransExp(Exp1,vtable,ftable,place1)
            // code2 = TransExp(Exp2,vtable,ftable,place2)
            // op = transop(getopname(binop))
            // code1++code2++[place := place1 op place2]
            String place1 = newvar("N");
            String place2 = newvar("N");
            String code1 = ATOMIC(ATOMIC1, place1);
            String code2 = ATOMIC(ATOMIC2, place2);
            String op = BINOP(BINOP);
            return code1 + code2 + place + " := " + place1 + op + place2;
        }

        throw new RuntimeException("Invalid SIMPLE.");
    }

    // COMPOSIT ::= BINOP( SIMPLE1 , SIMPLE2 ) Translation as explained above
    // COMPOSIT ::= UNOP ( SIMPLE ) Translation as explained above
    private String COMPOSIT (Tree COMPOSIT, String labelT, String labelF) throws RuntimeException {
        if (COMPOSIT.root.children.get(0).identifier.identifier.equals("BINOP")) {
            Tree SIMPLE1 = newBaseSubTree(COMPOSIT, "SIMPLE");
            Tree SIMPLE2 = newBaseSubTree(COMPOSIT, "SIMPLE", 1);

            String code1 = SIMPLE(SIMPLE1, labelT);
            String code2 = SIMPLE(SIMPLE2, labelF);
            return code1 + code2;
        } else if (COMPOSIT.root.children.get(0).identifier.identifier.equals("UNOP")) {
            Tree SIMPLE = newBaseSubTree(COMPOSIT, "SIMPLE");
            return SIMPLE(SIMPLE, labelT);
        }

        throw new RuntimeException("Invalid COMPOSIT.");
    }

    // FNAME ::= a token of Token-Class F from the Lexer
    private String FNAME (Tree FNAME) {
        // take the first child of FNAME
        Node node = FNAME.root.children.get(0);

        // get the value of the token
        String token = node.token.tokenValue;

        // get the new name from the symbol table
        String newName = symbolTable.lookupName(token).value;

        return newName;
    }

    // The user-defined names were already re-named in the foregoing Scope Analysis.
    // The translator function can find their new names in the Symbol Table.
    // Here ends the work-task for those students who only wish to carry out Phase 5a of the Project,
    // i.e.: the generation of non-executable Intermediate-Code. For Phase 5a (only) you do not need
    // to generate code for FUNCTIONS: these remain un-translated in Project Phase 5a. Generated
    // code must be written out into a legible *.txt file, which our Tutors can read for assessment and
    // marking.
    // ________________________________________________________________________________
    // ________________________________________________________________________________
    // Here begins the work-tasks for those students who also want to accomplish Project Phase 5b,
    // in which executable target code shall ultimately be generated. For this purpose it is of course
    // also necessary to generate target-code for the FUNCTIONS to which the Main-Program can
    // make calls. Project Phase 5b can be fully accomplished as soon as Textbook Chapter #9 and
    // Textbook Chapter #7 have been discussed in the lectures.
    // FUNCTIONS ::=
    // For this case, the translator function shall return the target-code-string " REM END "
    // FUNCTIONS1 ::= DECL FUNCTIONS2
    // We translate DECL, and append behind the DECL-code the translation of FUNCTIONS2.
    // Also important is the generation of a stop command behind DECL, such that the running
    // DECL code will not continue to run into the program code of the subsequent functions in
    // the same target-code-file!
    // Thus we must return the target-code-string dCode++" STOP "++fCode
    // where dCode = translation(DECL), and fCode = translation(FUNCTIONS2)
    // DECL ::= HEADER BODY
    // The HEADER will be treated either by the method of INLINING (as explained in lecture),
    // or by the method explained in Chapter #9 of our Textbook. Ultimately the HEADER will
    // vanish, as it does not contain any do-able algorithm. Only the BODY contains a do-able
    // algorithm, and thus only the BODY will eventually appear in the generated target code.
    // HEADER ::= FTYP FNAME( VNAME1 , VNAME2 , VNAME3 )
    // The HEADER will be treated either by the method of INLINING (as explained in lecture),
    // or by the method explained in Chapter #9 of our Textbook. Ultimately the HEADER will
    // vanish, as explained above.
    // FTYP ::= num
    // FTYP ::= void
    // The type declarations were needed only for Scope-Analysis and for Type-Checking.
    // They remainLOCVARS ::= VTYP1 VNAME1 , VTYP2 VNAME2 , VTYP3 VNAME3 ,
    // The variable declarations were needed only for Scope-Analysis and for Type-Checking.
    // They remain un-translated (can be ignored by the translator).
    // As usual, their new names are kept in the Symbol Table.
    // BODY ::= PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
    // translate(BODY) → return the code-string pCode++aCode++eCode++sCode
    // whereby:
    // pCode = translate(PROLOG)
    // aCode = translate(ALGO)
    // eCode = translate(EPILOG)
    // sCode = translate(SUBFUNCS)
    // PROLOG ::= {
    // If the code-generation-method for its corresponding function is INLINING (as lectured),
    // then translate(PROLOG) → return " REM BEGIN "
    // If the code-generation-method for its corresponding function is the method from Chapter #9,
    // then translate(PROLOG) will generate the boiler-plate-code (with runtime-Stack) as
    // explained in Chapter #9.
    // EPILOG ::= }
    // If the code-generation-method for its corresponding function is INLINING (as lectured),
    // then translate(EPILOG) → return " REM END "
    // If the code-generation-method for its corresponding function is the method from Chapter #9,
    // then translate(EPILOG) will generate the boiler-plate-code (with runtime-Stack) as
    // explained in Chapter #9.
    // SUBFUNCS ::= FUNCTIONS translate(SUBFUNCS) = translate(FUNCTIONS)
    // ______
}
