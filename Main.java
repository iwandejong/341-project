import Lexer.*;
import Parser.*;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // input file
        File file = new File("inputFile.recspl");
        Scanner sc1 = new Scanner(file);
        String program = "";
        while (sc1.hasNextLine()) {
            program += sc1.nextLine() + '\n';
        }
        sc1.close();

        // perform lexing
        Lexer l = new Lexer();
        l.performLexing(program);

        // load grammar
        File grammarFile = new File("CFG.txt");
        Scanner sc2 = new Scanner(grammarFile);
        List<String> grammar = new ArrayList<String>(); // read each rule of the CFG separately into a list
        while (sc2.hasNextLine()) {
            grammar.add(sc2.nextLine());
        }
        sc2.close();

        // define CFG
        // first get all rules
        List<String> ruleNames = new ArrayList<String>();
        for (int i = 0; i < grammar.size(); i++) {
            String[] rule = grammar.get(i).split(" ::= ");
            ruleNames.add(rule[0]);
        }

        // generate all rules
        // the rules follow the format: PROG : main -> GLOBVARS -> ALGO -> FUNCTIONS
        List<ProductionRule> rules = new ArrayList<ProductionRule>();
        for (int i = 0; i < grammar.size(); i++) {
            List<Symbol> symbols = new ArrayList<Symbol>();
            String[] rule = grammar.get(i).split(" ::= ");
            String identifier = rule[0];
            String[] next = rule[1].split(" ");

            Symbol lhs = new Symbol(identifier, false);

            for (int j = 0; j < next.length; j++) {
                if (ruleNames.contains(next[j])) {
                    symbols.add(new Symbol(next[j], false));
                } else {
                    symbols.add(new Symbol(next[j], true));
                }
            }

            ProductionRule r = new ProductionRule(lhs, symbols);
            rules.add(r);
        }

        // Print grammar
        System.out.println();
        System.out.println("\u001B[33m" + "CFG Rules:");
        System.out.println("----------" + "\u001B[0m");
        String r = "";
        for (ProductionRule rule : rules) {
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

        // TODO: traverse the tokens sequentially
        // when you run into a non-terminal symbol such as GLOBVARS, you "enter" the non-terminal symbol by "expanding" the symbol.
        // in the case of GLOBVARS, you'd enter the symbol and expand to get PROG -> main -> VTYP -> VNAME -> , -> ...
        // if no match is found BUT there is an epsilon-transition (e.g. GLOBVARS -> ε), then continue building the tree.

        Parser parser = new Parser(rules, l.tokens);
        // parser.parseSyntaxTree(l.tokens);
        parser.parse();
    }
}
