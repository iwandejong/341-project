package Code_Generator;
import Parser.*;
import java.util.*;

public class CodeGenerator {
    List<ProductionRule> productionRules = new ArrayList<>();
    Tree syntaxTree = new Tree();

    public void generateCode(List<ProductionRule> _productionRules, Tree _syntaxTree) {
        productionRules = _productionRules;
        syntaxTree = _syntaxTree;
        // find first ALGO node from ROOT and create a tree from it
        List<Node> root = syntaxTree.getNodes("ALGO");
        root.get(0).parent = null;
        Tree ALGO = new Tree(root.get(0));

        ALGO.visualiseTree(ALGO.root, "", true);

        String code = PROG(ALGO, null);
        System.out.println(code);
        System.out.println("\u001B[32m" + "Code Generation Successfully Completed." + "\u001B[0m");
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
    // PROG ::= main GLOBVARS ALGO FUNCTIONS
    // The source-word main remains un-translated (can be ignored by the translator).
    // We translate ALGO, and append behind the ALGO-code the translation of FUNCTIONS.
    // Also important is the generation of a stop command behind ALGO, such that the running
    // main-code will not continue to run into the program code of the functions in target code
    // within the same target-code-file!
    // Thus:
    // translation(PROG) must return the target-code-string aCode++" STOP "++fCode
    // whereby aCode = translation(ALGO), and fCode = translation(FUNCTIONS)
    private String PROG (Tree ALGO, Tree FUNCTIONS) {
        List<Node> INSTRUC_nodes = ALGO.getNodes("INSTRUC");
        INSTRUC_nodes.get(0).parent = null;
        Tree INSTRUC = new Tree(INSTRUC_nodes.get(0));
        String aCode = ALGO(INSTRUC);

        INSTRUC.visualiseTree(INSTRUC.root, "", true);

        // String fCode = FUNCTIONS(FUNCTIONS);
        return aCode + " STOP ";
    }

    // ALGO ::= begin INSTRUC end
    // The source-words begin and end remain un-translated (can be ignored by the translator).
    // Thus: translate(ALGO) = translate(INSTRUC)
    private String ALGO (Tree INSTRUC) {
        if (INSTRUC.root.children.size() == 0) {
            return INSTRUC();
        }
        return INSTRUC(INSTRUC);
    }

    // INSTRUC ::=
    // For this case, the translator function shall return the target-code-string " REM END "
    // // Comment: In our Target-Language, REM represents a non-executable remark
    private String INSTRUC () {
        return " REM END ";
    }

    // INSTRUC1 ::= COMMAND ; INSTRUC2
    // Translate this sequence such as Stat1 ; Stat2 in Figure 6.5 of our Textbook.
    private String INSTRUC (Tree INSTRUC) {
        // get last child of INSTRUC
        // List<Node> lastChild = INSTRUC.getNodes("INSTRUC");
        // lastChild.get(0).parent = null;
        // Tree INSTRUC2 = new Tree(lastChild.get(0));

        // check if INSTRUC2 has children
        // if (INSTRUC2.root.children.size() == 0) {
        //     return COMMAND(INSTRUC) + INSTRUC();
        // } else {
        //     return COMMAND(INSTRUC) + INSTRUC(INSTRUC2);
        // }
        return COMMAND(INSTRUC);
    }

    private String COMMAND (Tree INSTRUC) {
        List<Node> COMMAND_nodes = INSTRUC.getNodes("COMMAND");
        COMMAND_nodes.get(0).parent = null;
        Tree COMMAND = new Tree(COMMAND_nodes.get(0));

        COMMAND.visualiseTree(COMMAND.root, "", true);

        switch (COMMAND.root.children.get(0).identifier.identifier) {
            case "skip":
                return COMMAND_SKIP();
            case "halt":
                return COMMAND_HALT();
            case "print":
                return COMMAND_PRINT(COMMAND);
            case "return":
                return COMMAND_ATOMIC(COMMAND);
            case "ASSIGN":
                return COMMAND_ASSIGN(COMMAND);
            case "CALL":
                return COMMAND_CALL(COMMAND);
            case "BRANCH":
                return COMMAND_BRANCH(COMMAND);
            default:
                return "";
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
    private String COMMAND_PRINT (Tree ATOMIC) {
        String codeString = ATOMIC(ATOMIC);
        return "PRINT" + " " + codeString;
    }

    // COMMAND ::= return ATOMIC // Only for Project Phase 5b, NOT for Project Phase 5a!
    // The return ATOMIC command must stand 'inside' of a Function-Scope!
    // We assume that Scope-Analysis has checked this already! If the return ATOMIC command
    // was found inside the MAIN program then a semantic error must have already been thrown
    // in the Semantic Analysis phase, such that the translation phase would not even start.
    // Advice: Translation as per Chapter #9 of our Textbook, or per INLINING (as lectured).
    private String COMMAND_ATOMIC (Tree COMMAND) {
        return "";
    }

    // COMMAND ::= ASSIGN translate(COMMAND) = translate(ASSIGN)
    private String COMMAND_ASSIGN (Tree COMMAND) {
        List<Node> ASSIGN_nodes = COMMAND.getNodes("ASSIGN");
        ASSIGN_nodes.get(0).parent = null;
        Tree ASSIGN = new Tree(ASSIGN_nodes.get(0));

        ASSIGN.visualiseTree(ASSIGN.root, "", true);
        return "";
    }

    // COMMAND ::= CALL translate(COMMAND) = translate(CALL)
    private String COMMAND_CALL (Tree COMMAND) {
        return "";
    }

    // COMMAND ::= BRANCH translate(COMMAND) = translate(BRANCH)
    private String COMMAND_BRANCH (Tree COMMAND) {
        return "";
    }

    // ATOMIC ::= VNAME
    // translate(ATOMIC) → returns as code-string the new name of VNAME as found in the Symbol Table
    // ATOMIC ::= CONST translate(ATOMIC) = translate(CONST)
    private String ATOMIC (Tree ATOMIC) {
        return "";
    }

    // CONST ::= a token of Token-Class N from the Lexer
    // CONST ::= a token of Token-Class T from the Lexer
    // Constants are translated to themselves.
    // Example for a number constant: translate(235) → return " 235 "
    // Example for a text constant: translate("hello") → return " "hello" "
    // ! Note that the returned code-string must also contain these "quotation marks" !
    private String CONST (String token) {
        return " " + token + " ";
    }

    // ASSIGN ::= VNAME < input // The symbol < remains un-translated
    // codeString = translate(VNAME)
    // return( "INPUT"++" "++codeString )
    private String ASSIGN_INPUT () {
        String codeString = ATOMIC(new Tree());
        return "INPUT" + " " + codeString;
    }

    // ASSIGN ::= VNAME = TERM
    // Translate this case such as id := Exp in Figure 6.5 of our Textbook.
    private String ASSIGN_TERM () {
        return "";
    }
    
    // TERM ::= ATOMIC translate(TERM) = translate(ATOMIC)
    private String TERM_ATOMIC () {
        return "";
    }

    // TERM ::= CALL translate(TERM) = translate(CALL)
    private String TERM_CALL () {
        return "";
    }

    // TERM ::= OP translate(TERM) = translate(OP)
    private String TERM_OP () {
        return "";
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
    private String CALL() {
        String p1 = ATOMIC(new Tree());
        String p2 = ATOMIC(new Tree());
        String p3 = ATOMIC(new Tree());
        return "CALL_" + "(" + p1 + "," + p2 + "," + p3 + ")";
    }

    // OP ::= UNOP( ARG ) Translate this case such as unop Exp1 in Figure 6.3 of our Textbook,
    // however with the brackets!
    // In other words:
    // return code1++place++":="++opName++"("++place1++")"
    private String OP_UNOP () {
        return "";
    }

    // ARG ::= ATOMIC translate(ARG) = translate(ATOMIC)
    // ARG ::= OP translate(ARG) = translate(OP)
    private String ARG () {
        return "";
    }

    // UNOP ::= not
    // Important! Our Target-Language does not include in its own syntax any symbolic representation of
    // the Boolean negation operator not ! Wherever such a not occurs in a COMPOSIT COND of any
    // BRANCH statement, such a BRANCH statement must be translated as described in case ! Cond1
    // of Figure 6.8 of our Textbook whereby the then-code and the else-code of the if-then-else command
    // are getting swapped.
    private String UNOP_NOT () {
        return "";
    }

    // UNOP ::= sqrt // Numeric operation which yields a number's square root
    // translate(sqrt) → return "SQR" // That is the operator's syntax in our Target Language
    private String UNOP_SQRT () {
        return "SQR";
    }

    // OP ::= BINOP( ARG1 , ARG2 )
    // Translate this case such as Exp1 binop Exp2 in Figure 6.3 of our Textbook.
    private String OP_BINOP () {
        return "";
    }

    // BINOP ::= or
    // Important! Our Target-Language does not include in its own syntax any symbolic representation of
    // the Boolean disjunction operator or ! Wherever such an or occurs in a COMPOSIT COND of any
    // BRANCH statement, such a BRANCH statement must be translated such as described in case
    // Cond1 || Cond2 of Figure 6.8 of our Textbook, whereby cascading jumps to different labels will be
    // generated by the translator function.
    private String BINOP_OR () {
        return "";
    }

    // BINOP ::= and
    // Important! Our Target-Language does not include in its own syntax any symbolic representation of
    // the Boolean conjunction operator and ! Wherever such an and occurs in a COMPOSIT COND of
    // any BRANCH statement, such a BRANCH statement must be translated such as described in case
    // Cond1 && Cond2 of Figure 6.8 of our Textbook, whereby cascading jumps to different labels will
    // be generated by the translator function.
    private String BINOP_AND () {
        return "";
    }

    // BINOP ::= eq translate(eq) → return " = "
    private String BINOP_EQ () {
        return " = ";
    }

    // BINOP ::= grt translate(grt) → return " > "
    private String BINOP_GRT () {
        return " > ";
    }

    // BINOP ::= add translate(add) → return " + "
    private String BINOP_ADD () {
        return " + ";
    }

    // BINOP ::= sub translate(sub) → return " ‒ "
    private String BINOP_SUB () {
        return " - ";
    }

    // BINOP ::= mul translate(mul)→ return " * "
    private String BINOP_MUL () {
        return " * ";
    }

    // BINOP ::= div translate(div) → return " / "
    private String BINOP_DIV () {
        return " / ";
    }

    // BRANCH ::= if COND then ALGO1 else ALGO2
    // If the COND is a COMPOSIT,
    // then translate the whole BRANCH command as in Figure 6.8 in the Textbook.
    // If the COND is SIMPLE,
    // then translate the whole BRANCH command as in Figure 6.5 of the Textbook,
    // case: if COND then Stat1 else Stat2
    private String BRANCH (String COND) {
        if (COND.equals("COMPOSIT")) {
            return "";
        } else {
            return "";
        }
    }
    
    // COND ::= SIMPLE Translation as explained above
    // COND ::= COMPOSIT Translation as explained above
    // SIMPLE ::= BINOP( ATOMIC1 , ATOMIC2 ) Translation as explained above
    // COMPOSIT ::= BINOP( SIMPLE1 , SIMPLE2 ) Translation as explained above
    // COMPOSIT ::= UNOP ( SIMPLE ) Translation as explained above
    // FNAME ::= a token of Token-Class F from the Lexer
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
