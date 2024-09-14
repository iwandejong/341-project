package Parser;
import java.util.*;
public class Node {
    public List<Node> children;
    public Rule identifier;
    public boolean terminal;

    public Node(Rule _identifier) {
        children = new ArrayList<Node>();
        identifier = _identifier;
    }

    public void addChild(Node child) {
        children.add(child);
    }
}
