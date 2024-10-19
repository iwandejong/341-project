package Parser;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Lexer.Token;

public class Parser {
    public List<ProductionRule> rules = new ArrayList<ProductionRule>(); // define the list of production rules

    public Tree syntaxTree; // define the syntax tree to be build later on

    public List<Token> tokenList;

    public List<FIRSTFOLLOWtable> firstFollowTable;

    public Parser (List<ProductionRule> _rules, List<Token> _tokens) {
        // for SLR, add a first rule S -> PROG $
        List<Symbol> symbols = new ArrayList<Symbol>();
        symbols.add(new Symbol("PROG", false));
        symbols.add(new Symbol("$", true));
        ProductionRule r = new ProductionRule(new Symbol("S", false), symbols);
        rules.add(r);

        for (ProductionRule rule : _rules) {
            rules.add(rule);
        }

        tokenList = _tokens;
    }

    // * FIRST/FOLLOW table
    public void createFirstFollowTable() {
        firstFollowTable = new ArrayList<FIRSTFOLLOWtable>();
        for (ProductionRule rule : rules) {

            List<Symbol> first = findFirst(rule.lhs);
            List<Symbol> follow = findFollow(rule.lhs);
            boolean nullable = rule.nullable;

            boolean exists = false;
            for (FIRSTFOLLOWtable fft : firstFollowTable) {
                if (fft.nonTerminal.identifier.equals(rule.lhs.identifier)) {
                    exists = true;
                    break;
                }
            }

            if (exists) {
                for (Symbol symbol : first) {
                    for (FIRSTFOLLOWtable table : firstFollowTable) {
                        if (table.nonTerminal.identifier.equals(rule.lhs.identifier)) {
                            table.addFirst(symbol);
                        }
                    }
                }

                for (Symbol symbol : follow) {
                    for (FIRSTFOLLOWtable table : firstFollowTable) {
                        if (table.nonTerminal.identifier.equals(rule.lhs.identifier)) {
                            table.addFollow(symbol);
                        }
                    }
                }
            } else {
                FIRSTFOLLOWtable newTable = new FIRSTFOLLOWtable(rule.lhs, nullable);
                for (Symbol symbol : first) {
                    newTable.addFirst(symbol);
                }
                for (Symbol symbol : follow) {
                    newTable.addFollow(symbol);
                }
                firstFollowTable.add(newTable);
            }
        }
    }

    // print FIRST/FOLLOW table
    public void printFirstFollowTable() {
        System.out.println("FIRST/FOLLOW table:");
        for (FIRSTFOLLOWtable table : firstFollowTable) {
            System.out.println("Non-terminal: " + "\u001B[32m" + table.nonTerminal.identifier + "\u001B[0m");
            String first = " ";
            for (Symbol symbol : table.first) {
                first += symbol.identifier + " ";
            }
            System.out.println("First: {" + first + "}");
            String follow = " ";
            for (Symbol symbol : table.follow) {
                follow += symbol.identifier + " ";
            }
            if (follow.length() > 1) {
                follow = follow.substring(0, follow.length());
            }
            System.out.println("Follow: {" + follow + "}");
            System.out.println();
        }
    }

    // find FIRST set of a non-terminal symbol
    public List<Symbol> findFirst(Symbol nonTerminal) {
        if (nonTerminal.terminal) {
            List<Symbol> temp = new ArrayList<Symbol>();
            temp.add(nonTerminal);
            return temp;
        }
        List<Symbol> first = new ArrayList<Symbol>();
        for (ProductionRule rule : rules) {
            if (rule.lhs.identifier.equals(nonTerminal.identifier)) {
                // if first symbol is terminal, add to FIRST set
                if (rule.rhs.get(0).terminal) {
                    first.add(rule.rhs.get(0));
                } else {
                    // if first symbol is non-terminal, find FIRST set of that non-terminal symbol
                    List<Symbol> temp = findFirst(rule.rhs.get(0));
                    for (Symbol symbol : temp) {
                        first.add(symbol);
                    }
                }
            }
        }
        return first;
    }

    // find FOLLOW set of a non-terminal symbol
    public List<Symbol> findFollow(Symbol nonTerminal) {
        List<Symbol> follow = new ArrayList<Symbol>();
        if (nonTerminal.identifier.equals("S")) {
            follow.add(new Symbol("$", true));
        }

        for (ProductionRule rule : rules) {
            for (int i = 0; i < rule.rhs.size(); i++) {
                if (rule.rhs.get(i).identifier.equals(nonTerminal.identifier)) {
                    if (i == rule.rhs.size() - 1) {
                        if (!rule.lhs.identifier.equals(nonTerminal.identifier)) {
                            List<Symbol> temp = findFollow(rule.lhs);
                            for (Symbol symbol : temp) {
                                follow.add(symbol);
                            }
                        }
                    } else {
                        if (rule.rhs.get(i + 1).terminal) {
                            follow.add(rule.rhs.get(i + 1));
                        } else {
                            // if next symbol is non-terminal AND nullable, add FOLLOW set of that non-terminal symbol
                            // else add FIRST set of that non-terminal symbol

                            // see if the next symbol is nullable
                            boolean nullable = rule.rhs.get(i + 1).nullable;
                            if (nullable) {
                                List<Symbol> temp = findFirst(rule.rhs.get(i + 1));
                                for (Symbol symbol : temp) {
                                    follow.add(symbol);
                                }
                                List<Symbol> temp2 = findFollow(rule.lhs);
                                for (Symbol symbol : temp2) {
                                    follow.add(symbol);
                                }
                            } else {
                                List<Symbol> temp = findFirst(rule.rhs.get(i + 1));
                                for (Symbol symbol : temp) {
                                    follow.add(symbol);
                                }
                            }

                        }
                    }
                }
            }
        }

        // remove epsilon from FOLLOW set
        for (Symbol symbol : follow) {
            if (symbol.identifier.equals("ε")) {
                follow.remove(symbol);
                break;
            }
        }

        return follow;
    }

    public void parse() {
        // define the stack
        Stack<ProductionRule> ruleStack = new Stack<ProductionRule>();

        // add $ to the stack
        ruleStack.push(new ProductionRule(new Symbol("$", true), new ArrayList<Symbol>()));

        // add first rule to the stack
        ruleStack.push(rules.get(1));

        // define position stack
        Stack<Integer> positionStack = new Stack<Integer>();
        positionStack.push(0);

        // define token "iterator" in stream
        int atToken = 0;
        Token currentToken;
        if (tokenList.size() < atToken) {
            currentToken = tokenList.get(atToken);
        }

        // define the tree root
        syntaxTree = new Tree(new Node(rules.get(1).lhs));

        Stack<Node> nodeStack = new Stack<Node>();
        nodeStack.push(syntaxTree.root);

        // use recursive parseHelper
        try {
            parseHelper(atToken, ruleStack, nodeStack, positionStack);
        } catch (Exception e) {
            // Handle the exception (e.g., log it or print an error message)
            e.printStackTrace(); // Or use logging
        }

        System.out.println();
        System.out.println((ruleStack.peek().lhs.identifier.equals("$") && ruleStack.size() == 1) ? "\u001B[32m" + "Parsing successful." + "\u001B[0m" : "\u001B[31m" + "Parsing unsuccessful." + "\u001B[0m");
        System.out.println();

        // visualise the tree
        syntaxTree.visualiseTree(syntaxTree.root, "", true);
    }

    public void parseHelper(int atToken, Stack<ProductionRule> ruleStack, Stack<Node> nodeStack, Stack<Integer> positionStack) throws Exception {
        // break if ruleStack is empty
        if ((ruleStack.peek().lhs.identifier.equals("$") && ruleStack.size() == 1)) {
            return;
        }

        // break if nodeStack is empty
        if (nodeStack.empty()) {
            throw new Exception("Node stack is empty.");
        }

        // get all the right hand symbols
        List<Symbol> rhsSymbols = ruleStack.peek().rhs;
        Symbol curr = null;
        int currentSymbol = positionStack.peek();

        // ! we get the lookahead token to help us decide which rule to choose
        Token lookaheadToken = null;
        if (atToken + 1 < tokenList.size()) {
            lookaheadToken = tokenList.get(atToken + 1);
            // System.out.println("\u001B[45m" + "Lookahead Token: " + lookaheadToken.tokenValue + "\u001B[0m");
        }
        
        // * check if we've reached the end of the rule
        if (currentSymbol < rhsSymbols.size()) {
            // * if not, get the current symbol
            curr = rhsSymbols.get(currentSymbol);
        } else {
            // * we've reached the end of the rule, pop the rule from the stack
            // Temporarily save current rule
            ProductionRule currentRule = ruleStack.pop();
            // pop from node stack
            nodeStack.pop();
            // pop from position stack
            positionStack.pop();

            // increment parent rule's current symbol
            positionStack = incrementTop(positionStack);
            
            // Ensure that the stack is still not empty before continuing
            if (!ruleStack.isEmpty()) {
                // Reset currentSymbol and continue with the next rule in the stack
                parseHelper(atToken, ruleStack, nodeStack, positionStack);
            }
            
            return; // Prevent any further execution
        }

        // print stack
        // System.out.println();
        // System.out.println("\u001B[33m" + "Rule Stack:");
        // System.out.println("\u001B[34m" + "CurrentSymbol: " + ruleStack.peek().rhs.get(currentSymbol).identifier + "\u001B[0m");
        // if (atToken < tokenList.size()) {
        //     System.out.println("\u001B[34m" + "CurrentToken: " + tokenList.get(atToken).tokenValue + "\u001B[0m");
        // }
        // System.out.println("----------" + "\u001B[0m");
        // String r = "";
        // for (ProductionRule rule : ruleStack) {
        //     r += rule.lhs.identifier + " -> ";
        //     for (Symbol s : rule.rhs) {
        //         if (rule.rhs.indexOf(s) == currentSymbol) {
        //             r += "\u001B[41m" + s.identifier + "\u001B[0m";
        //         } else if (s.terminal) {
        //             r += "\u001B[32m" + s.identifier + "\u001B[0m";
        //         } else {
        //             r += s.identifier;
        //         }
        //         r += " ";
        //     }
        //     System.out.println(r);
        //     r = "";
        // }
        // System.out.println();

        // didn't run out of tokens yet
        if (atToken < tokenList.size()) {
            Token currentToken = tokenList.get(atToken);

            if (curr.terminal) {
                // * a dead end, we've reached a terminal symbol
                // check if the current token matches the terminal symbol
                if (checkRegex(currentToken.tokenValue, curr.identifier) || checkDirectMatch(currentToken.tokenValue, curr.identifier)) {
                    // add to the tree
                    Node newNode = new Node(new Symbol(currentToken.tokenValue, false), currentToken);
                    nodeStack.peek().addChild(newNode);

                    // increment the current symbol
                    positionStack = incrementTop(positionStack);

                    parseHelper(++atToken, ruleStack, nodeStack, positionStack);
                } else {
                    // * no match found, but there are still remaining rules to check
                    if (currentSymbol < rhsSymbols.size() - 1) {
                        throw new Exception("Syntax error: " + currentToken.tokenValue + " does not match " + curr.identifier);
                    } else {
                        // pop from stack, maybe the parent's "next" rule will match
                        nodeStack.pop();

                        // Temporarily save current rule
                        ProductionRule currentRule = ruleStack.pop();

                        // pop from position stack
                        positionStack.pop();

                        // increment parent rule's current symbol
                        positionStack = incrementTop(positionStack);

                        parseHelper(atToken, ruleStack, nodeStack, positionStack);
                    }
                }
            } else {
                // * essentially this goes to the next "level(s)" of the tree
                // non-terminal symbol, therefore, push to the stack and expand each option
                // System.out.println("\u001B[33m" + "Expanding non-terminal symbol: " + curr.identifier + "\u001B[0m");

                // this produces a trail of rules that match the current token
                // we also pass in the lookahead token to help us decide which rule to choose
                List<ProductionRule> nextRules = findNext(curr, currentToken.tokenValue, lookaheadToken);

                // if (nextRules != null) {
                //     for (ProductionRule nextRule : nextRules) {
                //         System.out.print("\u001B[43m" + nextRule.lhs.identifier + " ->");
                //         for (Symbol s : nextRule.rhs) {
                //             System.out.print(" " + s.identifier);
                //         }
                //         System.out.println("\u001B[0m");
                //     }
                // }

                if (nextRules == null) {
                    // * epsilon transition
                    // print the nullable rule
                    // System.out.println("\u001B[106m" + "Epsilon transition: " + curr.identifier + " -> ε" + "\u001B[0m");
                    // don't change the ruleStack, instead just traverse to the next symbol
                    // increment the current symbol
                    positionStack = incrementTop(positionStack);

                    parseHelper(atToken, ruleStack, nodeStack, positionStack);
                } else {
                    for (ProductionRule nextRule : nextRules) {
                        // print these rules
                        // System.out.print("\u001B[43m" + nextRule.lhs.identifier + " ->");
                        // for (Symbol s : nextRule.rhs) {
                        //     System.out.print(" " + s.identifier);
                        // }
                        // System.out.println("\u001B[0m");

                        // add to the tree
                        if (!nodeStack.empty()) {
                            Node newNode = new Node(nextRule.lhs);
                            nodeStack.peek().addChild(newNode);
                            nodeStack.push(newNode);
                            ruleStack.push(nextRule);
                            positionStack.push(0);
                        }
                    }
                    // ? after adding all the rules to the stack, we continue with the first rule on top of the stack
                    parseHelper(atToken, ruleStack, nodeStack, positionStack);
                }
            }
        } else {
            // we ran out of tokens, but there are still rules to check
            // if those rules are nullable, we can continue
            // otherwise, we need to backtrack

            // check if current rule has any epsilon transitions
            List<ProductionRule> nextRules = findNext(curr, "ε", null);
            boolean isEpsilon = false;

            for (ProductionRule nextRule : nextRules) {
                if (nextRule.rhs.get(0).identifier.equals("ε")) {
                    // * epsilon transition allowed
                    isEpsilon = true;
                }
            }

            if (isEpsilon) {
                nodeStack.pop();
                ruleStack.pop(); // pop the current rule if epsilon transition is allowed
            }

            // check if the stack is in an acceptable state
            if ((ruleStack.peek().lhs.identifier.equals("$") && ruleStack.size() == 1)) {
                return;
            }

            throw new Exception("Syntax error: ran out of tokens.");
        }
    }

    public ProductionRule findProductionRule(String identifier) {
        for (ProductionRule rule : rules) {
            if (rule.lhs.identifier.equals(identifier)) {
                return rule;
            }
        }

        return null;
    }

    public Stack<Integer> incrementTop(Stack<Integer> stack) {
        int top = stack.pop();
        stack.push(top + 1);
        return stack;
    }

    public List<ProductionRule> findNext(Symbol symbol, String identifier, Token lookahead) {
        List<ProductionRule> trail = new ArrayList<>();
        boolean matchFound = findNextHelper(symbol, identifier, lookahead, trail);

        // if no match is found, but there is an epsilon-transition, then continue building the tree
        if (!matchFound) {
            for (ProductionRule rule : rules) {
                if (rule.lhs.identifier.equals(symbol.identifier)) {
                    if (rule.rhs.get(0).identifier.equals("ε")) {
                        return null;
                    }
                }
            }
        }

        return trail;
    }
    
    private boolean findNextHelper(Symbol symbol, String identifier, Token lookahead, List<ProductionRule> trail) {
        // Find symbol that matches lhs of rule and matches the identifier
        for (ProductionRule rule : rules) {
            int lhscount = 0;
            for (ProductionRule r : rules) {
                if (r.lhs.identifier.equals(rule.lhs.identifier)) {
                    lhscount++;
                }
            }

            
            if (rule.lhs.identifier.equals(symbol.identifier)) {
                // Check if the lookahead matches the FIRST set of the rule's RHS
                // if (lookahead != null) {
                //     System.out.println("\u001B[45m" + "Lookahead Token: " + lookahead.tokenValue + "\u001B[0m");
                // }

                // get all rules with the same LHS
                List<ProductionRule> possibleRules = new ArrayList<>();
                for (ProductionRule r : rules) {
                    if (r.lhs.identifier.equals(rule.lhs.identifier)) {
                        possibleRules.add(r);
                    }
                }

                // for (ProductionRule rule2 : possibleRules) {
                //     System.out.print("\u001B[43m" + rule2.lhs.identifier + " ->");
                //     for (Symbol s : rule2.rhs) {
                //         System.out.print(" " + s.identifier);
                //     }
                //     System.out.println("\u001B[0m");
                // }

                // * Example:
                // b c d
                // * X -> C
                // * C -> A
                // * A -> b c x
                // * A -> b c d

                boolean correctRule = false;
                if (rule.rhs.size() < 2) {
                    correctRule = true;
                }
                
                List<List<List<Symbol>>> firstSets = new ArrayList<>();
                List<List<Boolean>> matches = new ArrayList<>();
                for (ProductionRule pr : possibleRules) {
                    List<List<Symbol>> firstSet = new ArrayList<>();
                    for (int i = 0; i < pr.rhs.size(); i++) {
                        firstSet.add(findFirst(pr.rhs.get(i)));
                    }
                    firstSets.add(firstSet);

                    // print first sets
                    // for (List<Symbol> fS : firstSet) {
                    //     String first = " ";
                    //     for (Symbol sym : fS) {
                    //         first += sym.identifier + " ";
                    //     }
                    //     System.out.println("First: {" + first + "}");
                    // }

                    // check if the lookahead token is in the FIRST set of the rule's RHS
                    List<Boolean> subMatches = new ArrayList<>();
                    for (int i = 0; i < pr.rhs.size(); i++) {
                        if (firstSet.get(i).size() > 0) {
                            for (Symbol sym : firstSet.get(i)) {
                                if (lookahead != null && sym.identifier.equals(lookahead.tokenValue)) {
                                    subMatches.add(true);
                                }
                            }
                        }
                    }
                    matches.add(subMatches);
                }

                // print matches
                // for (List<Boolean> match : matches) {
                //     String m = " ";
                //     for (Boolean b : match) {
                //         m += b + " ";
                //     }
                //     System.out.println("Matches: {" + m + "}");
                // }

                // pick the rule with the most matches
                int maxMatches = 0;
                int maxIndex = 0;
                for (int i = 0; i < matches.size(); i++) {
                    if (matches.get(i).size() > maxMatches) {
                        maxMatches = matches.get(i).size();
                        maxIndex = i;
                    }
                }

                if (maxMatches > 0) {
                    rule = possibleRules.get(maxIndex);
                }

                // print rule
                // System.out.print("\u001B[44m" + rule.lhs.identifier + " ->");
                // for (Symbol s : rule.rhs) {
                //     System.out.print(" " + s.identifier);
                // }
                // System.out.println("\u001B[0m");

                // Add the current rule to the trail
                trail.add(rule);
    
                // Check if the first symbol in RHS is a terminal (regex or exact match)
                boolean match = false;
                if (rule.rhs.get(0).terminal) {
                    if (checkRegex(identifier, rule.rhs.get(0).identifier) || checkDirectMatch(identifier, rule.rhs.get(0).identifier)) {
                        match = true;
                    }
                } else {
                    // Recursive check on the next symbol in rhs
                    boolean found = findNextHelper(rule.rhs.get(0), identifier, lookahead, trail);
                    if (found) {
                        match = true;
                    }
                }

                if (match) {
                    return true;
                } else {
                    // If no match was found, remove the last rule added to the trail
                    trail.remove(trail.size() - 1);
                }
            }
        }
        return false; // No match found
    }

    public boolean checkRegex(String input, String regex) {
        if (!regex.startsWith("RGX_")) {
            return false;
        }
        String regexString = regex.substring(4);
        Pattern pattern = Pattern.compile(regexString);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public boolean checkDirectMatch(String input, String match) {
        return input.equals(match);
    }
}
