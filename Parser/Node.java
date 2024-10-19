package Parser;
import java.util.*;

import Lexer.Token;
public class Node {
    public List<Node> children;
    public Symbol identifier;
    public boolean terminal;
    public Token token;
    public Node parent = null;

    public Node(Symbol _identifier) {
        children = new ArrayList<Node>();
        identifier = _identifier;
    }

    public Node(Symbol _identifier, Token _token) {
        children = new ArrayList<Node>();
        identifier = _identifier;
        token = _token;
    }

    public void addChild(Node child) {
        children.add(child);
    }
}
