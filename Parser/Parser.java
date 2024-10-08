package Parser;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import FA.*;
import Lexer.Token;

public class Parser {
    public List<ProductionRule> rules;

    public Tree syntaxTree; // define the syntax tree to be build later on

    public List<Token> tokenList;

    public List<FA> FAs = new ArrayList<FA>();

    public int ruleNumber = 0;

    public Parser (List<ProductionRule> _rules, List<Token> _tokens) {
        rules = _rules;
        tokenList = _tokens;

        initFA();
        connectFAs();
    }

    // initialise the FA for the parser
    public void initFA() {
        for (ProductionRule rule : rules) {
            FA fa = new FA(rule, ruleNumber, rule.lhs);
            ruleNumber += rule.rhs.size();
            FAs.add(fa);
        }
    }

    // connect the FAs with epsilon transitions
    public void connectFAs() {
        for (FA fa : FAs) {
            System.out.println(fa.identifier.identifier + ":" + fa.printFA(fa.root));
        }

        // find all non-terminal symbols
        List<String> nonTerminals = findAllNonTerminals();

        // drop "PROG" as it is the start symbol
        nonTerminals.remove("PROG");

        // process each non-terminal symbol
        for (String s : nonTerminals) {
            // find all states that have the non-terminal symbol as a transition
            System.out.println("Processing non-terminal symbol: " + s);
            for (FA fa : FAs) {
                if (fa.identifier.identifier.equals(s)) {
                    continue;
                }
                
                List<State> toStates = findAllToStates(new Symbol(s, true));
                List<State> fromStates = findFromStates(fa, new Symbol(s, true));

                if (toStates.size() == 0 || fromStates.size() == 0) {
                    continue;
                }
                
                for (State from : fromStates) {
                    for (State to : toStates) {
                        from.addTransition(new Symbol("ε", true), to);
                    }
                }
            }
            break;
        }

        for (FA fa : FAs) {
            for (String s : nonTerminals) {
            // print the from-to transitions for each non-terminal symbol
                List<State> toStates = fa.findTransitions(new Symbol(s, true));
                List<State> fromStates = findFromStates(fa, new Symbol(s, true));

                if (toStates.size() == 0 || fromStates.size() == 0) {
                    continue;
                }

                // print the transitions
                for (State to : toStates) {
                    for (State from : fromStates) {
                        System.out.println(from.printState() + " -[" + s + "]-> " + to.printState());
                    }
                }
                break;
            }
            break;
        }
    }

    // find all left-hand-side symbols
    public List<State> findAllToStates(Symbol identifier) {
        List<State> states = new ArrayList<State>();
        for (FA fa : FAs) {
            states.addAll(fa.findTransitions(identifier));
        }
        return states;
    }

    // find from state in current FA
    public List<State> findFromStates(FA fa, Symbol identifier) {
        return fa.findFromTransitions(identifier);
    }

    // find all non-terminal symbols
    public List<String> findAllNonTerminals() {
        List<String> nonTerminals = new ArrayList<String>();
        for (ProductionRule rule : rules) {
            if (!nonTerminals.contains(rule.lhs.identifier)) {
                nonTerminals.add(rule.lhs.identifier);
            }
        }
        return nonTerminals;
    }

    // TODO: attempt to create a SLR-parser since the grammar is not LL1-parseable :(
}
