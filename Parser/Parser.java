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

    // create FIRST/FOLLOW table
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
}
