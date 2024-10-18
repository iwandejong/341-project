PROG -> main GLOBVARS ALGO FUNCTIONS
GLOBVARS -> ''
GLOBVARS -> VTYP VNAME , GLOBVARS
VTYP -> num
VTYP -> text
ALGO -> begin INSTRUC end
INSTRUC -> ''
INSTRUC -> COMMAND ; INSTRUC
COMMAND -> skip
COMMAND -> halt
COMMAND -> print ATOMIC
COMMAND -> ASSIGN
COMMAND -> CALL
COMMAND -> BRANCH
ATOMIC -> VNAME
ATOMIC -> CONST
ASSIGN -> VNAME < input
ASSIGN -> VNAME = TERM
CALL -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
BRANCH -> if COND then ALGO else ALGO
TERM -> ATOMIC
TERM -> CALL
TERM -> OP
OP -> UNOP ( ARG )
OP -> BINOP ( ARG , ARG )
ARG -> ATOMIC
ARG -> OP
COND -> SIMPLE
COND -> COMPOSIT
SIMPLE -> BINOP ( ATOMIC , ATOMIC )
COMPOSIT -> BINOP ( SIMPLE , SIMPLE )
COMPOSIT -> UNOP ( SIMPLE )
UNOP -> not
UNOP -> sqrt
BINOP -> or
BINOP -> and
BINOP -> eq
BINOP -> grt
BINOP -> add
BINOP -> sub
BINOP -> mul
BINOP -> div
FUNCTIONS -> ''
FUNCTIONS -> DECL FUNCTIONS
DECL -> HEADER BODY
HEADER -> FTYP FNAME ( VNAME , VNAME , VNAME )
FTYP -> num
FTYP -> void
BODY -> PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
PROLOG -> {
EPILOG -> }
LOCVARS -> VTYP VNAME , VTYP VNAME , VTYP VNAME ,
SUBFUNCS -> FUNCTIONS
VNAME -> RGX_V_[a-z]([a-z]|[0-9])*
FNAME -> RGX_F_[a-z]([a-z]|[0-9])*
CONST -> RGX_^-?\d+(\.\d+)?$
CONST -> RGX_\"[A-Z]([a-z]){0,7}\"


if (top.identifier.equals(token.tokenValue)) {
                stack.pop();
                syntaxTree.current = syntaxTree.current.parent;
                i++;
            } else {
                boolean found = false;
                for (ProductionRule rule : rules) {
                    if (rule.lhs.identifier.equals(top.identifier)) {
                        List<Symbol> first = findFirst(rule.lhs);
                        for (Symbol s : first) {
                            System.out.println(s.identifier + " ##### " + token.tokenValue);

                            boolean isRegex = checkRegex(token.tokenValue, s.identifier);
                            if (s.identifier.equals(token.tokenValue) || isRegex) {
                                found = true;
                                if (s.identifier.equals("ε")) {
                                    syntaxTree.current = syntaxTree.current.parent;
                                    break;
                                }
                                syntaxTree.current = syntaxTree.addNode(syntaxTree.current, new Symbol(token.tokenValue, true));
                                stack.pop();
                                for (int j = rule.rhs.size() - 1; j >= 0; j--) {
                                    stack.push(rule.rhs.get(j));
                                }
                                System.out.println("Token: " + token.tokenValue + " First: " + s.identifier);
                                syntaxTree.visualiseTree(syntaxTree.root, "", true);
                                break;
                            }
                        }
                    }
                    if (found) {
                        break;
                    }
                }
                if (!found) {
                    System.out.println("Syntax error: unexpected token " + token.tokenClass);
                    break;
                }
            }