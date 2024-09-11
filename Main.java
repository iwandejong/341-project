import Lexer.Lexer;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // input file
        File file = new File("inputFile.recspl");
        Scanner sc = new Scanner(file);
        String program = "";
        while (sc.hasNextLine()) {
            program += sc.nextLine() + '\n';
        }
        sc.close();

        // perform lexing
        Lexer l = new Lexer();
        l.performLexing(program);

        l.printTokens();
    }
 }
