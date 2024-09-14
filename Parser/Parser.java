package Parser;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Lexer.Token;

public class Parser {
    public List<Rule> rules;
    public List<List<Rule>> FIRST; // each rule has its own set (list) of Rules that are terminal and is first to the non-terminal node
    public List<List<Rule>> FOLLOW; // each rule has its own set (list) of Rules that are terminal and follows the first set
    public List<Rule> NULLABLE; // each rule has its own set (list) of Rules that are nullable

    public Tree syntaxTree; // define the syntax tree to be build later on

    public Parser (List<Rule> _rules) {
        rules = _rules;
        FIRST = buildFIRSTSet();
        FOLLOW = buildFOLLOWSet();
        NULLABLE = buildNULLABLESet();
        
        printFIRSTSet();
        printFOLLOWSet();
        printNULLABLESet(); 
    }

    public List<List<Rule>> buildFIRSTSet() {
        List<List<Rule>> firstSet = getUniqueRules();
        for (List<Rule> subset : firstSet) {
            List<Rule> f = new ArrayList<Rule>();
            for (Rule rule : rules) {
                if (rule.identifier.equals(subset.get(0).identifier)) {
                    if (!rule.terminal) {
                        // recursively divide and conquer
                        List<Rule> subRules = new ArrayList<Rule>();
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
        for (List<Rule> subset : firstSet) {
            // remove duplicates in the set (that is the same identifier)
            for (int i = 0; i < subset.size(); i++) {
                Rule r = subset.get(i);
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

    public Rule buildFIRSTSetHelper(List<Rule> subRules, Rule rule) {
        if (rule.terminal) {
            subRules.add(rule);
            return rule;
        }

        // recursively go through until a terminal is found
        for (Rule subRule : rules) {
            if (rule.identifier.equals(subRule.identifier)) {
                // Recursively build FIRST set for sub-rules
                buildFIRSTSetHelper(subRules, subRule.next);
            }
        }
    
        return null;
    }

    public List<List<Rule>> buildFOLLOWSet() {
        List<List<Rule>> followSet = new ArrayList<List<Rule>>();
        
        // loop through all rules
        for (int i = 0; i < rules.size(); i++) {
            Rule r = rules.get(i);
            
            // iterate through each rule (e.g. PROG -> main -> VTYP -> VNAME -> , -> ...)
            // first take the next variable in the Rule (since PROG is a LHS-rule and FOLLOW only looks for RHS-rules)
            r = r.next;
            
            while (r != null) {
                // if the rule not terminal, find its FIRST set
                List<Rule> fS = new ArrayList<Rule>(); // "resets" fS variable
                if (!r.terminal) {
                    Rule nextRule = r.next; // GLOBVARS -> ALGO

                    if (nextRule == null) {
                        break;
                    }
                    
                    if (nextRule.terminal) {
                        fS.add(r); // add the LHS rule
                        fS.add(nextRule); // if the FOLLOW is a terminal, add it directly to the list
                    } else {
                        // find its FIRST set
                        List<Rule> f = findRules(FIRST, nextRule); // get the FIRSTs for the non-terminal
                        
                        List<Rule> newSet = new ArrayList<Rule>();
                        
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

    public List<Rule> findRules(List<List<Rule>> set, Rule rule) {
        for (List<Rule> s : set) {
            if (s.get(0).identifier.equals(rule.identifier)) {
                return s;
            }
        }

        return null;
    }

    public List<Rule> buildNULLABLESet() {
        List<Rule> nullableSet = new ArrayList<Rule>();
        for (Rule rule : rules) {
            if (rule.next.terminal && rule.next.identifier.equals("ε")) {
                nullableSet.add(rule);
            }
        }

        return nullableSet;
    }

    public List<List<Rule>> getUniqueRules() {
        List<List<Rule>> uniqueRules = new ArrayList<>();
    
        for (Rule r : rules) {
            boolean exists = false;
    
            for (List<Rule> roleGroup : uniqueRules) {
                if (roleGroup.get(0).identifier.equals(r.identifier)) {
                    exists = true;
                    break;
                }
            }
    
            if (!exists) {
                List<Rule> newGroup = new ArrayList<>();
                // Add all rules with the same identifier
                for (Rule rule : rules) {
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
            Rule r = rules.get(i);
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
            List<Rule> f = FIRST.get(i);
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
            List<Rule> f = FOLLOW.get(i);
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
            Rule r = NULLABLE.get(i);
            System.out.println(r.identifier);
        }
    }

    public List<Rule> findFirstSet (Rule r) {
        for (List<Rule> f : FIRST) {
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
        Stack<Rule> ruleStack = new Stack<Rule>();

        // add $ to the stack
        ruleStack.add(new Rule("$", null, true)); // it is a terminal symbol, no need to have a next.

        // define rule "iterator" as the start rule (which is PROG -> ...)
        Rule currentRule = rules.get(0);

        System.out.println();
        System.out.println(currentRule.identifier);

        // define token "iterator" in stream
        int atToken = 0;
        Token currentToken = tS.get(atToken);

        // define if rule is nullable
        boolean isNullable = false;

        // define the syntax tree
        Node root = new Node(currentRule);
        syntaxTree = new Tree(root);

        // add first rule to the stack
        ruleStack.add(currentRule);

        currentRule = currentRule.next;

        while (currentRule != null) {
            Node child = new Node(currentRule);
            if (currentRule.terminal) {
                // add it to the tree's current node as a child
                root.addChild(child);
            } else {
                // account for nullable rules (e.g. ε)
                List<Rule> firstSet = findFirstSet(currentRule);
                for (Rule fR : firstSet) {
                    Node childNode = new Node(fR);

                    if (fR.identifier.equals("ε")) {
                        isNullable = true;
                    }

                    if (fR.identifier.equals(currentToken.tokenValue)) {
                        // pass to syntax tree, match found, no need to look further
                        child.addChild(childNode);
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
                            child.addChild(childNode);
                        }
                    }
                }

                // push the rule to the stack
                ruleStack.add(currentRule);
            }
            // atToken++;
            // currentToken = tS.get(atToken);
            currentRule = ruleStack.peek();
        }
    }
}
