package Parser;

public class Tree {
    public Node root;
    public int depth;
    public Node current = root;

    public Tree () {
        depth = 0;
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

    //  syntaxTree.current = syntaxTree.addNode(syntaxTree.current, s);
    public Node addNode(Node parent, Symbol symbol) {
        Node node = new Node(symbol);
        node.parent = parent;
        parent.addChild(node);
        return node;
    }
}
