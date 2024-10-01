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

        System.out.println();
        System.out.println(ruleStack.peek().lhs.identifier);

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
        // select the current rule at the top of the stack
        // Rule currentRule = ruleStack.peek();
        
        if (ruleStack.peek().rhs.get(currentSymbol) == null) {
            // reached end of ruleset, pop
            ruleStack.pop();
            System.out.println(ruleStack.peek().rhs.get(0).identifier);
            parseHelper(atToken, ruleStack, 0);
        }
        
        System.out.println("Current Symbol: " + ruleStack.peek().rhs.get(currentSymbol).identifier);

        // didn't run out of tokens yet
        if (atToken < tokenList.size()) {
            Token currentToken = tokenList.get(atToken);
            Symbol curr = ruleStack.peek().rhs.get(currentSymbol);

            if (curr.terminal) {
                
                // try to convert to regex if no matches up to this point...
                if (curr.identifier.startsWith("RGX_")) {
                    String regexString = curr.identifier.substring(4);
                    Pattern pattern;
                    Matcher matcher;
                    boolean matchFound;

                    pattern = Pattern.compile(regexString);
                    matcher = pattern.matcher(currentToken.tokenValue);
                    matchFound = matcher.matches();

                    if (!matchFound) {
                        throw new Exception("No regex match was found, therefore abort parsing");
                    }
                }

                // try to match a raw value, if not, return false and abort parsing
                if (!curr.identifier.equals(currentToken.tokenValue)) {
                    throw new Exception("Tried to match a raw value, but failed, therefore return false and abort parsing");
                }

                // don't change the ruleStack (terminal symbols 'terminates'), instead just traverse to the next symbol
                parseHelper(++atToken, ruleStack, ++currentSymbol);
            } else {
                // non-terminal symbol, therefore, push to the stack and expand
                for (ProductionRule rule : rules) {
                    if (rule.lhs.identifier.equals(curr.identifier)) {
                        // ! it will push the first rule to the stack, and then the next rule will not be pushed to the stack
                        ruleStack.push(rule);
                        break;
                    }
                }

                parseHelper(atToken, ruleStack, 0);
            }
        } else {
            // we ran out of tokens, therefore, check if currentRule has FIRST/FOLLOW (?) rules
            throw new Exception("Ran out of tokens.");
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
}
