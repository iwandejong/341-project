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

        // use recursive parseHelper
        try {
            // ruleStack.get(0).next means PROG -> main.
            parseHelper(atToken, ruleStack, 0);
        } catch (Exception e) {
            // Handle the exception (e.g., log it or print an error message)
            e.printStackTrace(); // Or use logging
        }        

        System.out.println(ruleStack.peek() == null ? "Parsing passed" : "Parsing failed");
    }

    public void parseHelper(int atToken, Stack<ProductionRule> ruleStack, int currentSymbol) throws Exception {
        // visualize the stack
        System.out.println();
        System.out.println("-----------------");
        System.out.println("Stack: ");
        for (ProductionRule rule : ruleStack) {
            System.out.println(rule.lhs.identifier);
        }
        System.out.println("-----------------");
        System.out.println();

        // didn't run out of tokens yet
        if (atToken < tokenList.size()) {
            Token currentToken = tokenList.get(atToken);
            List<Symbol> rhsSymbols = ruleStack.peek().rhs;
            Symbol curr = null;

            if (currentSymbol < rhsSymbols.size()) {
                curr = rhsSymbols.get(currentSymbol);
            }
            
            if (curr == null || currentSymbol >= rhsSymbols.size()) {
                // Temporarily save current rule
                ProductionRule currentRule = ruleStack.pop();

                System.out.println("Current Rule: " + currentRule.lhs.identifier);

                int atRule = findRuleIndex(ruleStack.peek(), currentRule.lhs);

                System.out.println("At Rule: " + ruleStack.peek().lhs.identifier);
                System.out.println("At Rule: " + atRule);
        
                // Ensure that the stack is still not empty before continuing
                if (!ruleStack.isEmpty()) {
                    // Reset currentSymbol and continue with the next rule in the stack
                    parseHelper(atToken, ruleStack, ++atRule);
                }
                return;
            }

            System.out.println("Current Rule: " + curr.identifier);

            if (curr.terminal) {
                // * a dead end, we've reached a terminal symbol
                // don't change the ruleStack (terminal symbols 'terminates'), instead just traverse to the next symbol
                System.out.println("Terminal symbol: " + currentToken.tokenValue);
                parseHelper(++atToken, ruleStack, ++currentSymbol);
            } else {
                // * essentially this goes to the next "level(s)" of the tree
                // non-terminal symbol, therefore, push to the stack and expand each option
                List<ProductionRule> nextRules = findFIRST(curr, currentToken.tokenValue);

                if (nextRules == null) {
                    // * epsilon transition
                    // don't change the ruleStack, instead just traverse to the next symbol
                    parseHelper(atToken, ruleStack, ++currentSymbol);
                }

                for (ProductionRule nextRule : nextRules) {
                    // do not add if rule is already in stack
                    if (ruleStack.contains(nextRule)) {
                        continue;
                    }
                    ruleStack.push(nextRule);
                    parseHelper(atToken, ruleStack, 0);
                }
            }
        } else {
            // we ran out of tokens, but we need to check if the stack is empty.
            if (ruleStack.peek().lhs.equals("$") && ruleStack.size() == 1) {
                // if the stack is empty, then parsing is successful
                System.out.println("Parsing successful.");
            } else {
                // if the stack is not empty, then parsing is unsuccessful
                System.out.println("Parsing unsuccessful.");
            }
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
        System.out.println("Finding FIRST for: " + symbol.identifier + " with token: " + identifier);

        
    
        // Find symbol that matches lhs of rule and matches the identifier
        for (ProductionRule rule : rules) {

            if (rule.lhs.identifier.equals(symbol.identifier)) {
                // Add the current rule to the trail
                trail.add(rule);

                System.out.println(symbol.identifier + " -> " + rule.rhs.get(0).identifier);
    
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
