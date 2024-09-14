package Parser;

public class Tree {
    public Node root;
    public int depth;

    public Tree (Node _root) {
        root = _root;
        depth = 0;
    }

    public void visualiseTree(Node node, int depth) {
        if (node == null) return;
        for (int i = 0; i < depth; i++) {
            System.out.print("  ");
        }
        System.out.println(node.identifier.identifier);
        for (int i = 0; i < node.children.size(); i++) {
            visualiseTree(node.children.get(i), depth+1);
        }
    }
}
