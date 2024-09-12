import Lexer.*;
import java.io.*;
import java.util.*;
import CFG.*;

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

        List<Rule> rules = new ArrayList<Rule>();
        for (int i = 0; i < grammar.size(); i++) {
            String[] rule = grammar.get(i).split(" ::= ");
            String identifier = rule[0];
            String[] next = rule[1].split(" ");

            Rule r = new Rule(identifier, null, false);
            Rule temp = r;

            for (int j = 0; j < next.length; j++) {
                // since we know the ruleNames, we know the rules that are non-terminal
                if (ruleNames.contains(next[j])) {
                    temp.next = new Rule(next[j], null, false);
                    temp = temp.next;
                } else {
                    temp.next = new Rule(next[j], null, true);
                    temp = temp.next;
                }
            }

            rules.add(r);
        }
    }
 }
