package Parser;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Lexer.Token;

public class Parser {
    public List<Symbol> rules;
    public List<List<Symbol>> FIRST; // each rule has its own set (list) of Rules that are terminal and is first to the non-terminal node
    public List<List<Symbol>> FOLLOW; // each rule has its own set (list) of Rules that are terminal and follows the first set
    public List<Symbol> NULLABLE; // each rule has its own set (list) of Rules that are nullable

    public Tree syntaxTree; // define the syntax tree to be build later on

    public List<Token> tokenList;

    public Parser (List<Symbol> _rules, List<Token> _token) {
        rules = _rules;
        FIRST = buildFIRSTSet();
        FOLLOW = buildFOLLOWSet();
        NULLABLE = buildNULLABLESet();
        tokenList = _token;
        
        printFIRSTSet();
        printFOLLOWSet();
        printNULLABLESet(); 
    }

    public List<List<Symbol>> buildFIRSTSet() {
        List<List<Symbol>> firstSet = getUniqueRules();
        for (List<Symbol> subset : firstSet) {
            List<Symbol> f = new ArrayList<Symbol>();
            for (Symbol rule : rules) {
                if (rule.identifier.equals(subset.get(0).identifier)) {
                    if (!rule.terminal) {
                        // recursively divide and conquer
                        List<Symbol> subRules = new ArrayList<Symbol>();
                        buildFIRSTSetHelper(subRules, rule.next);
                        f.addAll(subRules);
                    } else {
                        f.add(rule);
                    }
                }
            }

            subset.addAll(f);
        }

        // remove duplicates
        for (List<Symbol> subset : firstSet) {
            // remove duplicates in the set (that is the same identifier)
            for (int i = 0; i < subset.size(); i++) {
                Symbol r = subset.get(i);
                for (int j = i + 1; j < subset.size(); j++) {
                    if (r.identifier.equals(subset.get(j).identifier)) {
                        subset.remove(j);
                        j--;
                    }
                }
            }
        }

        return firstSet;
    }

    public Symbol buildFIRSTSetHelper(List<Symbol> subRules, Symbol rule) {
        if (rule.terminal) {
            subRules.add(rule);
            return rule;
        }

        // recursively go through until a terminal is found
        for (Symbol subRule : rules) {
            if (rule.identifier.equals(subRule.identifier)) {
                // Recursively build FIRST set for sub-rules
                buildFIRSTSetHelper(subRules, subRule.next);
            }
        }
    
        return null;
    }

    public List<List<Symbol>> buildFOLLOWSet() {
        List<List<Symbol>> followSet = new ArrayList<List<Symbol>>();
        
        // loop through all rules
        for (int i = 0; i < rules.size(); i++) {
            Symbol r = rules.get(i);
            
            // iterate through each rule (e.g. PROG -> main -> VTYP -> VNAME -> , -> ...)
            // first take the next variable in the Rule (since PROG is a LHS-rule and FOLLOW only looks for RHS-rules)
            r = r.next;
            
            while (r != null) {
                // if the rule not terminal, find its FIRST set
                List<Symbol> fS = new ArrayList<Symbol>(); // "resets" fS variable
                if (!r.terminal) {
                    Symbol nextRule = r.next; // GLOBVARS -> ALGO

                    if (nextRule == null) {
                        break;
                    }
                    
                    if (nextRule.terminal) {
                        fS.add(r); // add the LHS rule
                        fS.add(nextRule); // if the FOLLOW is a terminal, add it directly to the list
                    } else {
                        // find its FIRST set
                        List<Symbol> f = findRules(FIRST, nextRule); // get the FIRSTs for the non-terminal
                        
                        List<Symbol> newSet = new ArrayList<Symbol>();
                        
                        newSet.add(r); // add the LHS rule
                        for (int j = 1; j < f.size(); j++) {
                            newSet.add(f.get(j)); // don't add LHS rule again
                        }

                        fS.addAll(newSet);
                    }
                    
                    boolean alreadyInSet = false;
                    for (int k = 0; k < followSet.size(); k++) {
                        if (followSet.get(k).get(0).identifier.equals(fS.get(0).identifier)) {
                            for (int m = 1; m < fS.size(); m++) {
                                if (!followSet.get(k).contains(fS.get(m))) { // Prevent duplicate rules
                                    boolean stringMatchFound = false;
                                    for (int n = 0; n < followSet.get(k).size(); n++) {
                                        if (followSet.get(k).get(n).identifier.equals(fS.get(m).identifier)) {
                                            stringMatchFound = true;
                                        }
                                    }
                                    if (!stringMatchFound) {
                                        followSet.get(k).add(fS.get(m));
                                    }
                                }
                            }
                            alreadyInSet = true;
                        }
                    }
                    if (!alreadyInSet) {
                        followSet.add(fS);
                    }
                }
                r = r.next;
            }
        }

        return followSet;
    }

    public List<Symbol> findRules(List<List<Symbol>> set, Symbol rule) {
        for (List<Symbol> s : set) {
            if (s.get(0).identifier.equals(rule.identifier)) {
                return s;
            }
        }

        return null;
    }

    public List<Symbol> buildNULLABLESet() {
        List<Symbol> nullableSet = new ArrayList<Symbol>();
        for (Symbol rule : rules) {
            if (rule.next.terminal && rule.next.identifier.equals("ε")) {
                nullableSet.add(rule);
            }
        }

        return nullableSet;
    }

    public List<List<Symbol>> getUniqueRules() {
        List<List<Symbol>> uniqueRules = new ArrayList<>();
    
        for (Symbol r : rules) {
            boolean exists = false;
    
            for (List<Symbol> roleGroup : uniqueRules) {
                if (roleGroup.get(0).identifier.equals(r.identifier)) {
                    exists = true;
                    break;
                }
            }
    
            if (!exists) {
                List<Symbol> newGroup = new ArrayList<>();
                // Add all rules with the same identifier
                for (Symbol rule : rules) {
                    if (r.identifier.equals(rule.identifier)) {
                        newGroup.add(rule);
                    }
                }
                uniqueRules.add(newGroup);
            }
        }
    
        // // Print unique rules
        // for (List<Rule> group : uniqueRules) {
        //     System.out.println(group.get(0).identifier);
        // }
    
        return uniqueRules;
    }    

    public void printAllTerminals() {
        for (int i = 0; i < rules.size(); i++) {
            Symbol r = rules.get(i);
            while (r != null) {
                if (r.terminal) {
                    System.out.println(r.identifier);
                }
                r = r.next;
            }
        }
    }

    public void printFIRSTSet() {
        System.out.println();
        System.out.println("\u001B[33m" + "FIRST Rules:");
        System.out.println("----------" + "\u001B[0m");
        for (int i = 0; i < FIRST.size(); i++) {
            List<Symbol> f = FIRST.get(i);
            String r = "";
            for (int j = 0; j < f.size(); j++) {
                r += f.get(j).identifier;
                if (j != f.size() - 1 && j > 0) {
                    r += ", ";
                }

                if (j == 0) {
                    r += " = { ";
                }
            }
            r += " }";
            System.out.println(r);
        }
    }

    public void printFOLLOWSet() {
        System.out.println();
        System.out.println("\u001B[33m" + "FOLLOW Rules:");
        System.out.println("----------" + "\u001B[0m");
        for (int i = 0; i < FOLLOW.size(); i++) {
            List<Symbol> f = FOLLOW.get(i);
            String r = "";
            for (int j = 0; j < f.size(); j++) {
                r += f.get(j).identifier;
                if (j != f.size() - 1 && j > 0) {
                    r += ", ";
                }

                if (j == 0) {
                    r += " = { ";
                }
            }
            r += " }";
            System.out.println(r);
        }
    }

    public void printNULLABLESet() {
        System.out.println();
        System.out.println("\u001B[33m" + "NULLABLE Rules:");
        System.out.println("----------" + "\u001B[0m");
        for (int i = 0; i < NULLABLE.size(); i++) {
            Symbol r = NULLABLE.get(i);
            System.out.println(r.identifier);
        }
    }

    public List<Symbol> findFirstSet (Symbol r) {
        for (List<Symbol> f : FIRST) {
            if (f.getFirst().identifier.equals(r.identifier)) {
                return f;
            }
        }
        return null;
    }

    public List<Symbol> findFollowSet (Symbol r) {
        for (List<Symbol> f : FOLLOW) {
            if (f.getFirst().identifier.equals(r.identifier)) {
                return f;
            }
        }
        return null;
    }

    // TODO: traverse the tokens sequentially
    // when you run into a non-terminal symbol such as GLOBVARS, you "enter" the non-terminal symbol by "expanding" the symbol.
    // in the case of GLOBVARS, you'd enter the symbol and expand to get PROG -> main -> VTYP -> VNAME -> , -> ...
    // if no match is found BUT there is an epsilon-transition (e.g. GLOBVARS -> ε), then continue building the tree. Do not nullify the transition if the nullable set is invalid (e.g. it has length > 0, but is invalid) - this would instead result in a syntax error
    // traverse the rules with a token stream read from the Lexer XML file
    public void parseSyntaxTree(List<Token> tS) {
        // define the stack
        Stack<Symbol> ruleStack = new Stack<Symbol>();

        // add $ to the stack
        ruleStack.add(new Symbol("$", null, true)); // it is a terminal symbol, no need to have a next.

        // define rule "iterator" as the start rule (which is PROG -> ...)
        Symbol currentRule = rules.get(0);

        System.out.println();
        System.out.println(currentRule.identifier);

        // define token "iterator" in stream
        int atToken = 0;
        Token currentToken = tS.get(atToken);

        // define if rule is nullable
        boolean isNullable = false;

        // define the syntax tree
        // Node root = new Node(currentRule); // * ignore syntax tree itself for now
        // syntaxTree = new Tree(root); // * ignore syntax tree itself for now

        // add first rule to the stack
        ruleStack.add(currentRule);

        // first rule is PROG, move onto the next rule to allow parsing to start
        currentRule = currentRule.next;

        System.out.println(currentRule.identifier);

        while (currentRule != null) {
            // Node child = new Node(currentRule); // * ignore syntax tree itself for now
            if (currentRule.terminal) {
                // add it to the tree's current node as a child
                // root.addChild(child); // * ignore syntax tree itself for now
            } else {
                // account for nullable rules (e.g. ε)
                List<Symbol> firstSet = findFirstSet(currentRule);
                for (Symbol fR : firstSet) {
                    // Node childNode = new Node(fR); // * ignore syntax tree itself for now

                    if (fR.identifier.equals("ε")) {
                        isNullable = true;
                    }

                    if (fR.identifier.equals(currentToken.tokenValue)) {
                        // pass to syntax tree, match found, no need to look further
                        // child.addChild(childNode); // * ignore syntax tree itself for now
                    }

                    // try to convert to regex if no matches up to this point...
                    if (fR.identifier.startsWith("RGX_")) {
                        String regexString = fR.identifier.substring(4);
                        Pattern pattern;
                        Matcher matcher;
                        boolean matchFound;

                        pattern = Pattern.compile(regexString);
                        matcher = pattern.matcher(currentToken.tokenValue);
                        matchFound = matcher.matches();

                        if (matchFound) {
                            // pass to syntax tree, match found, no need to look further
                            // child.addChild(childNode); // * ignore syntax tree itself for now
                        }
                    }
                }

                // push the rule to the stack
                ruleStack.add(currentRule);
            }

            // select next token
            atToken++;
            currentToken = tS.get(atToken);

            currentRule = ruleStack.peek();

            break;
        }
    }

    public void parse() {
        // define the stack
        Stack<Symbol> ruleStack = new Stack<Symbol>();

        // add $ to the stack
        ruleStack.push(new Symbol("$", null, true)); // it is a terminal symbol, no need to have a next.

        // add first rule to the stack
        ruleStack.push(rules.get(0).next);

        System.out.println();
        System.out.println(ruleStack.peek().identifier);

        // define token "iterator" in stream
        int atToken = 0;
        Token currentToken;
        if (tokenList.size() < atToken) {
            currentToken = tokenList.get(atToken);
        } 

        // use recursive parseHelper
        try {
            // ruleStack.get(0).next means PROG -> main.
            parseHelper(atToken, ruleStack, ruleStack.peek());
        } catch (Exception e) {
            // Handle the exception (e.g., log it or print an error message)
            e.printStackTrace(); // Or use logging
        }        

        System.out.println(ruleStack.peek() == null ? "Parsing passed" : "Parsing failed");
    }

    public void parseHelper(int atToken, Stack<Symbol> ruleStack, Symbol currentSymbol) throws Exception {
        // select the current rule at the top of the stack
        // Rule currentRule = ruleStack.peek();
        
        if (currentSymbol == null) {
            // reached end of ruleset, pop
            ruleStack.pop();
            List<Symbol> followSet = findFollowSet(ruleStack.peek());
            for (Symbol fR : followSet) {
                System.out.println(fR.identifier);
            }

            parseHelper(atToken, ruleStack, ruleStack.peek().next);
        }
        
        System.out.println("Current Symbol: " + currentSymbol.identifier);

        // didn't run out of tokens yet
        if (atToken < tokenList.size()) {
            Token currentToken = tokenList.get(atToken);

            if (currentSymbol.terminal) {
                
                // try to convert to regex if no matches up to this point...
                if (currentSymbol.identifier.startsWith("RGX_")) {
                    String regexString = currentSymbol.identifier.substring(4);
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
                if (!currentSymbol.identifier.equals(currentToken.tokenValue)) {
                    throw new Exception("Tried to match a raw value, but failed, therefore return false and abort parsing");
                }

                // don't change the ruleStack (terminal symbols 'terminates'), instead just traverse to the next symbol
                parseHelper(++atToken, ruleStack, currentSymbol.next);
            } else {
                // get FIRST set
                List<Symbol> firstSet = findFirstSet(currentSymbol);
                
                // find the first one that matches
                Symbol temp = currentSymbol;
                for (Symbol fR : firstSet) {
                    if (fR.identifier.equals(currentToken.tokenValue)) {
                        currentSymbol = fR;
                        break;
                    }
                }

                // if currentRule didn't change, no rule was found, therefore abort parsing
                if (temp.equals(currentSymbol)) {
                    throw new Exception("currentRule didn't change, no rule was found in FIRST set, therefore abort parsing");
                }

                // there is such a rule, therefore proceed.
                currentSymbol = temp.next;

                ruleStack.push(currentSymbol);
                parseHelper(atToken, ruleStack, ruleStack.peek());
            }
        } else {
            if (atToken == (tokenList.size() - 1) && currentSymbol == null) {
                return;
            }
            // we ran out of tokens, therefore, check if currentRule has FIRST/FOLLOW (?) rules
            throw new Exception("Ran out of tokens.");
        }
    }
}
