package Parser;
import java.util.*;

public class Tree {
    public Node root;
    public int depth;
    public Node current = root;

    public Tree () {
        depth = 0;
    }

    public Tree (Node _root) {
        root = _root;
        depth = 0;
    }

    public List<Node> getNodes (String identifier) {
        List<Node> nodes = new ArrayList<>();
        return getNodesHelper(root, identifier, nodes);
    }

    public List<Node> getNodesHelper (Node node, String identifier, List<Node> nodes) {
        if (node == null) return nodes;
        if (node.identifier.identifier.equals(identifier)) {
            nodes.add(node);
        }
        for (int i = 0; i < node.children.size(); i++) {
            getNodesHelper(node.children.get(i), identifier, nodes);
        }
        return nodes;
    }

    public void visualiseTree(Node node, String prefix, boolean isTail) {
        if (node == null) return;
    
        // Print the current node's identifier
        System.out.println(prefix + (isTail ? "└── " : "├── ") + node.identifier.identifier);
    
        // Calculate the new prefix for the next level
        prefix += (isTail ? "    " : "│   ");
    
        // Iterate over the children of the current node
        for (int i = 0; i < node.children.size(); i++) {
            // Recursively call for children, marking if it is the last child
            visualiseTree(node.children.get(i), prefix, i == node.children.size() - 1);
        }
    }

    public Node addNode(Node parent, Symbol symbol) {
        Node node = new Node(symbol);
        node.parent = parent;
        parent.addChild(node);
        return node;
    }
}
