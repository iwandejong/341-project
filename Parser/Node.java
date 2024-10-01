package Parser;
import java.util.*;
public class Node {
    public List<Node> children;
    public Symbol identifier;
    public boolean terminal;

    public Node(Symbol _identifier) {
        children = new ArrayList<Node>();
        identifier = _identifier;
    }

    public void addChild(Node child) {
        children.add(child);
    }
}
