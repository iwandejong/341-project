import Lexer.*;
import Parser.*;
import Type_Checker.*;
import Code_Generator.*;

import java.io.*;
import java.util.*;

import Analysis.Scope_Analysis;
import Analysis.Symbol_Table;

public class Main {
    static Symbol findSymbol(String identifier, List<Symbol> symbols) {
        for (Symbol s : symbols) {
            if (s.identifier.equals(identifier)) {
                return s;
            }
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        // read each "Test" from a txt file into a list
        List<String> programs = new ArrayList<>();
        try {
            // README
            System.out.println("***********************************");
            System.out.println("*      WELCOME TO THE RecSPL      *");
            System.out.println("*          COMPILER!              *");
            System.out.println("***********************************");
            System.out.println("A compiler by Jacobus Smit (u21489476) and Iwan de Jong (u22498037)");
            System.out.println("Please enter the path of the input file containing the RecSPL programs.");
            System.out.println("Please see how we have structured the input file in the testFile.txt for reference.");
            System.out.println();
            System.out.println("\u001B[34m" + "Expected output files:" + "\u001B[0m");
            System.out.println("1. Lexer Output: lexer_output_n.xml");
            System.out.println("2. Parser Output: parser_output_n.xml");
            System.out.println("3. Intermediate Code: intermediate_code_n.txt");
            System.out.println("4. Target Code: target_code_n.bas");
            System.out.println();
            System.out.println("\u001B[41m" + "Note: The target code runs on QB64, a modern extended BASIC programming language, which is obtainable from https://qb64.com/" + "\u001B[0m");

            Scanner userInput = new Scanner(System.in);
            System.out.print("Enter the path of the input file (e.g., C:\\files\\testFile.txt or testFile.txt (if you're in the right directory)): ");
            String filePath = userInput.nextLine();
            userInput.close();

            File file = new File(filePath);
            Scanner sc1 = new Scanner(file);
            StringBuilder tempString = new StringBuilder();

            while (sc1.hasNextLine()) {
                String line = sc1.nextLine();

                // Check for the delimiter to break out
                if (line.equals("## BREAK ##")) {
                    break;
                }

                // Check for empty line to store the current program
                if (line.trim().isEmpty()) {
                    if (tempString.length() > 0) {
                        programs.add(tempString.toString());
                        tempString.setLength(0); // Reset the StringBuilder
                    }
                } else {
                    // Append the current line to the tempString
                    tempString.append(line).append('\n');
                }
            }

            // To add the last program if file doesn't end with an empty line
            if (tempString.length() > 0) {
                programs.add(tempString.toString());
            }

            sc1.close();

            // Visualize inputs
            // for (String s : programs) {
            //     System.out.println(s);
            //     System.out.println("----- End of Program -----");
            // }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }

        
        // input file
        // File file = new File("inputFile.recspl");
        // Scanner sc1 = new Scanner(file);
        // List<String> programs = new ArrayList<String>();
        // while (sc1.hasNextLine()) {
        //     program += sc1.nextLine() + '\n';
        // }
        // sc1.close();

        // perform lexing
        List<Lexer> lexers = new ArrayList<Lexer>();
        for (int i = 0; i < programs.size(); i++) {
            Lexer l = new Lexer(i);
            try {
                l.performLexing(programs.get(i));
                lexers.add(l);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
        List<Symbol> ruleNames = new ArrayList<Symbol>();
        for (int i = 0; i < grammar.size(); i++) {
            String[] rule = grammar.get(i).split(" ::= ");
            ruleNames.add(new Symbol(rule[0], false));
        }

        // generate all rules
        // the rules follow the format: PROG : main -> GLOBVARS -> ALGO -> FUNCTIONS
        List<ProductionRule> rules = new ArrayList<ProductionRule>();
        for (int i = 0; i < grammar.size(); i++) {
            List<Symbol> symbols = new ArrayList<Symbol>();
            String[] rule = grammar.get(i).split(" ::= ");
            String identifier = rule[0];
            String[] next = rule[1].split(" ");

            Symbol lhs = findSymbol(identifier, ruleNames);

            boolean nullable = false;

            for (int j = 0; j < next.length; j++) {
                Symbol s = findSymbol(next[j], ruleNames);
                if (s != null) {
                    symbols.add(s);
                } else {
                    s = new Symbol(next[j], true);
                    if (next[j].equals("ε")) {
                        lhs.nullable = true;
                        s.nullable = true;
                        nullable = true;
                    }
                    symbols.add(s);
                }
            }

            ProductionRule r = new ProductionRule(lhs, symbols, nullable);
            rules.add(r);
        }

        // perform OR operation: if one production rule is nullable, then so is the other production rule with the same LHS symbol
        for (ProductionRule rule : rules) {
            for (ProductionRule r : rules) {
                if (rule.lhs.identifier.equals(r.lhs.identifier)) {
                    if (r.nullable) {
                        rule.nullable = true;
                        rule.lhs.nullable = true;
                    }
                }
            }
        }

        // iterate again through all the production rules, then perform AND operation on the nullable property of the symbols
        // if all symbols are nullable, then the lhs symbol is nullable
        for (ProductionRule rule : rules) {
            boolean nullable = true;
            if (rule.nullable) {
                continue;
            }
            for (Symbol s : rule.rhs) {
                if (!s.nullable) {
                    nullable = false;
                    break;
                }
            }
            rule.lhs.nullable = nullable;
            rule.nullable = nullable;
        }

        // Print grammar
        // System.out.println();
        // System.out.println("\u001B[33m" + "CFG Rules:");
        // System.out.println("----------" + "\u001B[0m");
        // String r = "";
        // for (ProductionRule rule : rules) {
        //     if (rule.nullable) {
        //         r += "\u001B[33m" + rule.lhs.identifier + " -> " + "\u001B[0m";
        //     } else {
        //         r += rule.lhs.identifier + " -> ";
        //     }
        //     for (Symbol s : rule.rhs) {
        //         if (s.terminal) {
        //             if (s.nullable) {
        //                 r += "\u001B[33m" + s.identifier + "\u001B[0m";
        //             } else {
        //                 r += "\u001B[32m" + s.identifier + "\u001B[0m";
        //             }
        //         } else {
        //             // r += s.identifier;
        //             if (s.nullable) {
        //                 r += "\u001B[33m" + s.identifier + "\u001B[0m";
        //             } else {
        //                 r += s.identifier;
        //             }
        //         }
        //         r += " ";
        //     }
        //     System.out.println(r);
        //     r = "";
        // }
        // System.out.println();

        List<Parser> parsers = new ArrayList<Parser>();
        for (Lexer l : lexers) {
            Parser p = new Parser(rules, l.tokens);
            parsers.add(p);
        }
        
        // parse
        Hashtable<Integer, String> symbolTable = new Hashtable<Integer, String>();
        Scope_Analysis sa = new Scope_Analysis();
        Type_Checker tc = new Type_Checker();
        CodeGenerator cg = new CodeGenerator();
        TargetCodeGenerator tcg = new TargetCodeGenerator();
        int i = 0;
        for (Parser p : parsers) {
            try {
                // * Project Phase 2
                p.parse(i);

                // * Project Phase 3
                Symbol_Table st = sa.start(p.syntaxTree);

                // * Project Phase 4
                tc.check(p.syntaxTree.root, sa.symbolTable);
                // Print symbol table after type checking thus contains type information
                System.out.println();
                System.out.println("\u001B[33m" + "Symbol Table:" + "\u001B[0m");
                sa.printSymbolTable();

                // * Project Phase 5a
                cg.generateCode(rules, p.syntaxTree, st, i);

                // * Project Phase 5b
                tcg.generateCode(rules, p.syntaxTree, st, i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
    }
}
