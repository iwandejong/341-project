package Lexer;
import java.util.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Lexer {
    public List<Token> tokens;
    public List<String> reservedKeywords;

    public Lexer() throws FileNotFoundException {
        tokens = new ArrayList<Token>();
        reservedKeywords = new ArrayList<String>();

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
        
        // TODO: Test for indentation as well
        for (int i = 0; i < input.length(); i++) {
            // if input at i is ' ' or '\n', reset the DFA.
            if (input.charAt(i) == ' ' || input.charAt(i) == '\n') {
                // verify token
                String tokenClass = verifyToken(temp);
                if (tokenClass != null) {
                    int id = 0;
                    if (tokens.size() > 0) {
                        id = tokens.get(tokens.size()-1).id + 1;
                    }

                    tokens.add(new Token(id, tokenClass, temp));
                    temp = "";
                    continue; // prevent ' ' or '\n' from being added to the temp string
                } else {
                    throw new Exception("Lexical analysis failed.");
                }
            }

            temp += input.charAt(i);
        }

        printTokens();

        // finally output as XML
        generateXML();
    }

    // check if token matches the expected language, returns the class
    public String verifyToken(String input) {
        // ? perform reserved-keyword search first - why?
        if (reservedKeywords.contains(input)) {
            return "reserved_keyword";
        }

        // * TOKEN CLASS V
        // check if input matches any of the regex
        Pattern pattern = Pattern.compile("V_[a-z]([a-z]|[0-9])*");
        Matcher matcher = pattern.matcher(input);
        boolean matchFound = matcher.find();

        if (matchFound) {
            return "V";
        } // otherwise remain hopeful

        // * TOKEN CLASS F
        pattern = Pattern.compile("F_[a-z]([a-z]|[0-9])*");
        matcher = pattern.matcher(input);
        matchFound = matcher.find();

        if (matchFound) {
            return "F";
        } // otherwise remain hopeful

        // * TOKEN CLASS T
        pattern = Pattern.compile("\"[A-Z]([a-z]){0,7}\"");
        matcher = pattern.matcher(input);
        matchFound = matcher.find();

        if (matchFound) {
            return "T";
        } // otherwise remain hopeful

        // * TOKEN CLASS N
        pattern = Pattern.compile("[-]?\\d+[\\.\\d*]?");
        matcher = pattern.matcher(input);
        matchFound = matcher.find();

        if (matchFound) {
            return "N";
        }

        // eventually return false
        return null;
    }

    public void printTokens() {
        // print tokens
        for (int i = 0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i).id);
            System.out.println(tokens.get(i).tokenClass);
            System.out.println(tokens.get(i).tokenValue);
        }
    }

    public void generateXML() throws Exception {
        if (tokens == null || tokens.isEmpty()) {
            throw new Exception("Token list is empty or not initialized.");
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element tokenStream = null;
        tokenStream = doc.createElement("TOKENSTREAM");

        for (int i = 0; i < tokens.size(); i++) {
            Element token = doc.createElement("TOK");

            Element tokenId = doc.createElement("ID");
            tokenId.setTextContent(Integer.toString(tokens.get(i).id));

            Element tokenClass = doc.createElement("CLASS");
            tokenClass.setTextContent(tokens.get(i).tokenClass);

            Element tokenValue = doc.createElement("WORD");
            tokenClass.setTextContent(tokens.get(i).tokenValue);

            token.appendChild(tokenId);
            token.appendChild(tokenClass);
            token.appendChild(tokenValue);

            tokenStream.appendChild(token);
        }

        doc.appendChild(tokenStream);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("lexer_output.xml"));
        transformer.transform(source, result);
    }
}
