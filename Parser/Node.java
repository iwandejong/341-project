package Parser;
import java.util.*;

// * Defines a node for a parse tree
public class Node {
    public List<Node> children; // defines node's children (if non-terminal)
    public String value; // terminal node
    public String operator; // ! need to change later on (specifically for non-terminal node)

    // Non-terminal node constructor
    public Node (List<Node> _children, String _operator) {
        children = _children;
        operator = _operator;
    }

    // Terminal node constructor
    public Node (String _value) {
        value = _value;
    }
}
