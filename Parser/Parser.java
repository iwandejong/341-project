package Parser;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;

import Lexer.Token;

public class Parser {
    public List<ProductionRule> rules = new ArrayList<ProductionRule>(); // define the list of production rules

    public Tree syntaxTree; // define the syntax tree to be build later on

    public List<Token> tokenList;

    public List<FIRSTFOLLOWtable> firstFollowTable;

    private List<LRItem> canonicalCollection;
    private Map<Integer, Map<Symbol, Action>> actionTable;
    private Map<Integer, Map<Symbol, Integer>> gotoTable;

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

        createFirstFollowTable();
        buildCanonicalCollection();
        buildParsingTables();
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

    public boolean checkRegex(String input, String regex) {
        if (!regex.startsWith("RGX_")) {
            return false;
        }
        String regexString = regex.substring(4);
        Pattern pattern = Pattern.compile(regexString);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

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
            throw new Exception("Node stack is empty.");
        }

        List<Symbol> rhsSymbols = ruleStack.peek().rhs;
        Symbol curr = null;
        Symbol next = null;
        
        if (currentSymbol < rhsSymbols.size()) {
            curr = rhsSymbols.get(currentSymbol);
        } else {
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

        if (currentSymbol + 1 < rhsSymbols.size()) {
            next = rhsSymbols.get(currentSymbol + 1);
            System.out.println("\u001B[45m" + "Next Symbol: " + next.identifier + "\u001B[0m");
        }

        // print stack
        System.out.println();
        System.out.println("\u001B[33m" + "Rule Stack:");
        System.out.println("\u001B[34m" + "CurrentSymbol: " + ruleStack.peek().rhs.get(currentSymbol).identifier + "\u001B[0m");
        if (atToken < tokenList.size()) {
            System.out.println("\u001B[34m" + "CurrentToken: " + tokenList.get(atToken).tokenValue + "\u001B[0m");
        }
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
                        Node newNode = new Node(new Symbol(currentToken.tokenValue, false),currentToken);
                        nodeStack.peek().addChild(newNode);

                        parseHelper(++atToken, ruleStack, ++currentSymbol, nodeStack);
                    }
                } else if (ruleStack.peek().rhs.get(currentSymbol).identifier.equals(currentToken.tokenValue)) {
                    // add to the tree
                    Node newNode = new Node(curr, currentToken);
                    nodeStack.peek().addChild(newNode);

                    parseHelper(++atToken, ruleStack, ++currentSymbol, nodeStack);
                } else {
                    // * no match found, but there are still remaining rules to check
                    if (currentSymbol < rhsSymbols.size() - 1) {
                        throw new Exception("Syntax error: " + currentToken.tokenValue + " does not match " + curr.identifier);
                    } else {
                        // pop from stack, maybe the parent's "next" rule will match
                        nodeStack.pop();

                        ProductionRule currentRule = ruleStack.pop();
                
                        int atRule = findRuleIndex(ruleStack.peek(), currentRule.lhs);

                        parseHelper(atToken, ruleStack, ++atRule, nodeStack);
                    }
                }
            } else {
                // * essentially this goes to the next "level(s)" of the tree
                // non-terminal symbol, therefore, push to the stack and expand each option
                System.out.println("\u001B[33m" + "Expanding non-terminal symbol: " + curr.identifier + "\u001B[0m");
                if (atToken + 1 < tokenList.size()) {
                    System.out.println("Current Token: " + currentToken.tokenValue + " following symbol: " + tokenList.get(atToken + 1).tokenValue);
                }
                List<ProductionRule> nextRules = findFIRST(curr, currentToken.tokenValue);

                if (nextRules != null) {
                    for (ProductionRule nextRule : nextRules) {
                        System.out.print("\u001B[43m" + nextRule.lhs.identifier + " ->");
                        for (Symbol s : nextRule.rhs) {
                            System.out.print(" " + s.identifier);
                        }
                        System.out.println("\u001B[0m");
                    }
                }

                if (nextRules == null) {
                    // * epsilon transition
                    // print the nullable rule
                    System.out.println("\u001B[106m" + "Epsilon transition: " + curr.identifier + " -> ε" + "\u001B[0m");
                    // don't change the ruleStack, instead just traverse to the next symbol
                    parseHelper(atToken, ruleStack, ++currentSymbol, nodeStack);
                } else {
                    for (ProductionRule nextRule : nextRules) {
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
        // Find symbol that matches lhs of rule and matches the identifier
        for (ProductionRule rule : rules) {

            if (rule.lhs.identifier.equals(symbol.identifier)) {
                // Add the current rule to the trail
                trail.add(rule);
    
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

    private void buildCanonicalCollection() {
        canonicalCollection = new ArrayList<>();
        Set<LRItem> initialSet = closure(new LRItem(rules.get(0), 0));
        canonicalCollection.add(new ArrayList<>(initialSet));

        boolean changed;
        do {
            changed = false;
            List<LRItem> newItems = new ArrayList<>();

            for (List<LRItem> itemSet : canonicalCollection) {
                for (Symbol symbol : getAllSymbols()) {
                    Set<LRItem> gotoSet = gotoOperation(itemSet, symbol);
                    if (!gotoSet.isEmpty() && !canonicalCollection.contains(gotoSet)) {
                        newItems.addAll(gotoSet);
                        changed = true;
                    }
                }
            }

            if (changed) {
                canonicalCollection.add(newItems);
            }
        } while (changed);
    }

    private Set<LRItem> closure(LRItem item) {
        Set<LRItem> closure = new HashSet<>();
        closure.add(item);

        boolean changed;
        do {
            changed = false;
            Set<LRItem> newItems = new HashSet<>();

            for (LRItem currentItem : closure) {
                if (currentItem.dotPosition < currentItem.rule.rhs.size()) {
                    Symbol nextSymbol = currentItem.rule.rhs.get(currentItem.dotPosition);
                    if (!nextSymbol.terminal) {
                        for (ProductionRule rule : rules) {
                            if (rule.lhs.equals(nextSymbol)) {
                                LRItem newItem = new LRItem(rule, 0);
                                if (!closure.contains(newItem)) {
                                    newItems.add(newItem);
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }

            closure.addAll(newItems);
        } while (changed);

        return closure;
    }

    private Set<LRItem> gotoOperation(List<LRItem> itemSet, Symbol symbol) {
        Set<LRItem> gotoSet = new HashSet<>();

        for (LRItem item : itemSet) {
            if (item.dotPosition < item.rule.rhs.size() && item.rule.rhs.get(item.dotPosition).equals(symbol)) {
                gotoSet.addAll(closure(new LRItem(item.rule, item.dotPosition + 1)));
            }
        }

        return gotoSet;
    }

    private void buildParsingTables() {
        actionTable = new HashMap<>();
        gotoTable = new HashMap<>();

        for (int i = 0; i < canonicalCollection.size(); i++) {
            List<LRItem> itemSet = canonicalCollection.get(i);
            actionTable.put(i, new HashMap<>());
            gotoTable.put(i, new HashMap<>());

            for (LRItem item : itemSet) {
                if (item.dotPosition == item.rule.rhs.size()) {
                    // Reduce action
                    if (item.rule.lhs.identifier.equals("S")) {
                        actionTable.get(i).put(new Symbol("$", true), new Action(ActionType.ACCEPT, 0));
                    } else {
                        for (Symbol symbol : findFollow(item.rule.lhs)) {
                            actionTable.get(i).put(symbol, new Action(ActionType.REDUCE, rules.indexOf(item.rule)));
                        }
                    }
                } else {
                    Symbol nextSymbol = item.rule.rhs.get(item.dotPosition);
                    Set<LRItem> gotoSet = gotoOperation(itemSet, nextSymbol);
                    int gotoIndex = canonicalCollection.indexOf(new ArrayList<>(gotoSet));

                    if (gotoIndex != -1) {
                        if (nextSymbol.terminal) {
                            // Shift action
                            actionTable.get(i).put(nextSymbol, new Action(ActionType.SHIFT, gotoIndex));
                        } else {
                            // Goto action
                            gotoTable.get(i).put(nextSymbol, gotoIndex);
                        }
                    }
                }
            }
        }
    }

    public void parse() {
        Stack<Integer> stateStack = new Stack<>();
        Stack<Symbol> symbolStack = new Stack<>();
        stateStack.push(0);
        symbolStack.push(new Symbol("$", true));

        int inputIndex = 0;
        Token currentToken = tokenList.get(inputIndex);

        while (true) {
            int currentState = stateStack.peek();
            Symbol currentSymbol = new Symbol(currentToken.tokenValue, true);

            Action action = actionTable.get(currentState).get(currentSymbol);

            if (action == null) {
                throw new RuntimeException("Syntax error: Unexpected token " + currentToken.tokenValue);
            }

            switch (action.type) {
                case SHIFT:
                    stateStack.push(action.value);
                    symbolStack.push(currentSymbol);
                    inputIndex++;
                    if (inputIndex < tokenList.size()) {
                        currentToken = tokenList.get(inputIndex);
                    }
                    break;

                case REDUCE:
                    ProductionRule rule = rules.get(action.value);
                    for (int i = 0; i < rule.rhs.size(); i++) {
                        stateStack.pop();
                        symbolStack.pop();
                    }
                    int gotoState = gotoTable.get(stateStack.peek()).get(rule.lhs);
                    stateStack.push(gotoState);
                    symbolStack.push(rule.lhs);
                    System.out.println("Reduce: " + rule);
                    break;

                case ACCEPT:
                    System.out.println("Parsing completed successfully.");
                    return;

                default:
                    throw new RuntimeException("Invalid action type");
            }
        }
    }

    private Set<Symbol> getAllSymbols() {
        Set<Symbol> symbols = new HashSet<>();
        for (ProductionRule rule : rules) {
            symbols.add(rule.lhs);
            symbols.addAll(rule.rhs);
        }
        return symbols;
    }

    private class LRItem {
        ProductionRule rule;
        int dotPosition;

        LRItem(ProductionRule rule, int dotPosition) {
            this.rule = rule;
            this.dotPosition = dotPosition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LRItem lrItem = (LRItem) o;
            return dotPosition == lrItem.dotPosition && Objects.equals(rule, lrItem.rule);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rule, dotPosition);
        }
    }

    private enum ActionType {
        SHIFT, REDUCE, ACCEPT
    }

    private class Action {
        ActionType type;
        int value;

        Action(ActionType type, int value) {
            this.type = type;
            this.value = value;
        }
    }
}
