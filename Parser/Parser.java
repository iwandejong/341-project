package Parser;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Lexer.Token;

public class Parser {
    public List<ProductionRule> rules;

    public Tree syntaxTree; // define the syntax tree to be build later on

    public List<Token> tokenList;

    public Parser (List<ProductionRule> _rules, List<Token> _tokens) {
        rules = _rules;
        tokenList = _tokens;
    }

    // TODO: traverse the tokens sequentially
    // when you run into a non-terminal symbol such as GLOBVARS, you "enter" the non-terminal symbol by "expanding" the symbol.
    // in the case of GLOBVARS, you'd enter the symbol and expand to get PROG -> main -> VTYP -> VNAME -> , -> ...
    // if no match is found BUT there is an epsilon-transition (e.g. GLOBVARS -> ε), then continue building the tree. Do not nullify the transition if the nullable set is invalid (e.g. it has length > 0, but is invalid) - this would instead result in a syntax error
    // traverse the rules with a token stream read from the Lexer XML file
    public void parse() {
        // define the stack
        Stack<ProductionRule> ruleStack = new Stack<ProductionRule>();

        // add $ to the stack
        ruleStack.push(new ProductionRule(new Symbol("$", true), new ArrayList<Symbol>()));

        // add first rule to the stack
        ruleStack.push(rules.get(0));

        // define token "iterator" in stream
        int atToken = 0;
        Token currentToken;
        if (tokenList.size() < atToken) {
            currentToken = tokenList.get(atToken);
        }

        // define the tree root
        syntaxTree = new Tree(new Node(rules.get(0).lhs));

        Stack<Node> nodeStack = new Stack<Node>();
        nodeStack.push(syntaxTree.root);

        // use recursive parseHelper
        try {
            parseHelper(atToken, ruleStack, 0, nodeStack);
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

    public void parseHelper(int atToken, Stack<ProductionRule> ruleStack, int currentSymbol, Stack<Node> nodeStack) throws Exception {
        // break if ruleStack is empty
        if ((ruleStack.peek().lhs.identifier.equals("$") && ruleStack.size() == 1)) {
            return;
        }

        // break if nodeStack is empty
        if (nodeStack.empty()) {
            return;
        }

        // print stack
        System.out.println();
        System.out.println("\u001B[33m" + "Rule Stack:");
        System.out.println("----------" + "\u001B[0m");
        String r = "";
        for (ProductionRule rule : ruleStack) {
            r += rule.lhs.identifier + " -> ";
            for (Symbol s : rule.rhs) {
                if (s.terminal) {
                    r += "\u001B[32m" + s.identifier + "\u001B[0m";
                } else {
                    r += s.identifier;
                }
                r += " ";
            }
            System.out.println(r);
            r = "";
        }
        System.out.println();

        List<Symbol> rhsSymbols = ruleStack.peek().rhs;
        Symbol curr = null;
        
        if (currentSymbol < rhsSymbols.size()) {
            curr = rhsSymbols.get(currentSymbol);
        }
        
        if (curr == null || currentSymbol >= rhsSymbols.size()) {
            // Temporarily save current rule
            ProductionRule currentRule = ruleStack.pop();
            // pop from node stack
            nodeStack.pop();
            
            int atRule = findRuleIndex(ruleStack.peek(), currentRule.lhs);
            
            // Ensure that the stack is still not empty before continuing
            if (!ruleStack.isEmpty()) {
                // Reset currentSymbol and continue with the next rule in the stack
                parseHelper(atToken, ruleStack, ++atRule, nodeStack);
            }
            
            return; // Prevent any further execution
        }
        
        // didn't run out of tokens yet
        if (atToken < tokenList.size()) {
            Token currentToken = tokenList.get(atToken);

            if (curr.terminal) {
                // * a dead end, we've reached a terminal symbol
                // check if the current token matches the terminal symbol
                if (ruleStack.peek().rhs.get(currentSymbol).identifier.startsWith("RGX_")) {
                    String regexString = ruleStack.peek().rhs.get(currentSymbol).identifier.substring(4);
                    Pattern pattern = Pattern.compile(regexString);
                    Matcher matcher = pattern.matcher(currentToken.tokenValue);
    
                    if (matcher.matches()) {
                        // add to the tree
                        Node newNode = new Node(curr);
                        nodeStack.peek().addChild(newNode);

                        parseHelper(++atToken, ruleStack, ++currentSymbol, nodeStack);
                    }
                } else if (ruleStack.peek().rhs.get(currentSymbol).identifier.equals(currentToken.tokenValue)) {
                    // add to the tree
                    Node newNode = new Node(curr);
                    nodeStack.peek().addChild(newNode);

                    parseHelper(++atToken, ruleStack, ++currentSymbol, nodeStack);
                } else {
                    // pop from stack, maybe the parent's "next" rule will match
                    nodeStack.pop();

                    ProductionRule currentRule = ruleStack.pop();
            
                    int atRule = findRuleIndex(ruleStack.peek(), currentRule.lhs);

                    parseHelper(atToken, ruleStack, ++atRule, nodeStack);
                }
            } else {
                // * essentially this goes to the next "level(s)" of the tree
                // non-terminal symbol, therefore, push to the stack and expand each option
                List<ProductionRule> nextRules = findFIRST(curr, currentToken.tokenValue);

                if (nextRules == null || nextRules.isEmpty()) {
                    // * epsilon transition
                    // don't change the ruleStack, instead just traverse to the next symbol
                    parseHelper(atToken, ruleStack, ++currentSymbol, nodeStack);
                } else {
                    for (ProductionRule nextRule : nextRules) {
                        // do not add if rule is already in stack
                        if (ruleStack.contains(nextRule)) {
                            continue;
                        }

                        // add to the tree
                        if (!nodeStack.empty()) {
                            Node newNode = new Node(curr);
                            nodeStack.peek().addChild(newNode);
                            nodeStack.push(newNode);
    
                            ruleStack.push(nextRule);
                            parseHelper(atToken, ruleStack, 0, nodeStack);
                        }
                    }
                }
            }
        } else {
            // check if current rule has any epsilon transitions
            List<ProductionRule> nextRules = findFIRST(curr, "ε");
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

            return; // Prevent any further execution
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

    public List<ProductionRule> findFIRST(Symbol symbol, String identifier) {
        List<ProductionRule> trail = new ArrayList<>();
        boolean matchFound = findFIRSTHelper(symbol, identifier, trail);

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
    
    private boolean findFIRSTHelper(Symbol symbol, String identifier, List<ProductionRule> trail) {
        // System.out.println("Finding FIRST for: " + symbol.identifier + " with token: " + identifier);

        
    
        // Find symbol that matches lhs of rule and matches the identifier
        for (ProductionRule rule : rules) {

            if (rule.lhs.identifier.equals(symbol.identifier)) {
                // Add the current rule to the trail
                trail.add(rule);

                // System.out.println(symbol.identifier + " -> " + rule.rhs.get(0).identifier);
    
                // Recursively enter the rule, if it's a non-terminal symbol
                if (rule.rhs.get(0).identifier.startsWith("RGX_")) {
                    String regexString = rule.rhs.get(0).identifier.substring(4);
                    Pattern pattern = Pattern.compile(regexString);
                    Matcher matcher = pattern.matcher(identifier);
    
                    if (matcher.matches()) {
                        return true; // Match found
                    }
                } else if (rule.rhs.get(0).identifier.equals(identifier)) {
                    return true; // Exact match found
                } else {
                    // Recursive check on the next symbol in rhs
                    boolean found = findFIRSTHelper(rule.rhs.get(0), identifier, trail);
                    if (found) {
                        return true; // Propagate match found
                    }
                }
                // If no match was found, remove the last rule added to the trail
                trail.remove(trail.size() - 1);
            }
        }
    
        return false; // No match found
    }

    public int findRuleIndex(ProductionRule rule, Symbol symbol) {
        for (ProductionRule r : rules) {
            if (r.lhs.identifier.equals(rule.lhs.identifier)) {
                for (int i = 0; i < r.rhs.size(); i++) {
                    if (r.rhs.get(i).identifier.equals(symbol.identifier)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }
}
