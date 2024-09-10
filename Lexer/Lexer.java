package Lexer;
import java.util.*;
import java.io.*;
import Grammar.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public List<Token> tokens;
    // public DFA dfa; // use DFA to perform tokenization
    public List<String> reservedKeywords;

    public Lexer() throws FileNotFoundException {
        tokens = new ArrayList<Token>();
        reservedKeywords = new ArrayList<String>();
        // dfa = _dfa;

        // load reserved keywords
        File file = new File("reservedKeywords.txt");
        Scanner sc = new Scanner(file);
        String fileContent = "";
        while (sc.hasNextLine()) {
            fileContent += sc.nextLine() + '\n';
        }
        
        String[] array = fileContent.split("\n");
        for (int i = 0; i < array.length; i++) {
            reservedKeywords.add(array[i]);
        }

        sc.close();
    }

    public void performLexing(String input) throws Exception {
        // perform lexing while not in an accepting state (if accepting state, then tokenize)
        // State s = dfa.g.S; // set s to start state of DFA
        String temp = "";
        
        for (int i = 0; i < input.length(); i++) {
            // if input at i is ' ' or '\n', reset the DFA.
            if (input.charAt(i) == ' ' || input.charAt(i) == '\n') {
                // verify token
                if (verifyToken(temp)) {
                    tokens.add(new Token(temp));
                    temp = "";
                }
            }

            temp += input.charAt(i);
        }
    }

    // check if token matches the expected language
    public boolean verifyToken(String input) {
        // perform reserved-keyword search first - why?
        for (int i = 0; i < tokens.size(); i++) {
            System.out.println(reservedKeywords.get(i));
        }

        if (reservedKeywords.contains(input)) {
            return true;
        }

        // * TOKEN CLASS V
        // check if input matches any of the regex
        Pattern pattern = Pattern.compile("V_[a-z]([a-z]|[0-9])*");
        Matcher matcher = pattern.matcher(input);
        boolean matchFound = matcher.find();

        if (matchFound) {
            return true;
        } // otherwise remain hopeful

        // * TOKEN CLASS F
        pattern = Pattern.compile("F_[a-z]([a-z]|[0-9])*");
        matcher = pattern.matcher(input);
        matchFound = matcher.find();

        if (matchFound) {
            return true;
        } // otherwise remain hopeful

        // * TOKEN CLASS T
        pattern = Pattern.compile("\"[A-Z]([a-z]){0,7}\"");
        matcher = pattern.matcher(input);
        matchFound = matcher.find();

        if (matchFound) {
            return true;
        } // otherwise remain hopeful

        // * TOKEN CLASS N
        pattern = Pattern.compile("[-]?\\d+[\\.\\d*]?");
        matcher = pattern.matcher(input);
        matchFound = matcher.find();

        if (matchFound) {
            return true;
        }

        // eventually return false
        return false;
    }

    public void printTokens() {
        // print tokens
        for (int i = 0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i).tokenValue);
        }
    }
}
