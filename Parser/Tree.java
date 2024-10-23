package Parser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public String generateXML() {
        if (root == null) {
            return "<SYNTREE></SYNTREE>";
        }

        StringBuilder xml = new StringBuilder();
        Map<Node, Integer> nodeIds = new HashMap<>();
        int nextId = 1;
        
        // First pass: assign unique IDs to all nodes
        assignNodeIds(root, nodeIds, nextId);
        
        xml.append("<SYNTREE>\n");
        
        // Generate ROOT section
        generateRootXML(xml, nodeIds);
        
        // Generate INNERNODES section
        generateInnerNodesXML(xml, nodeIds);
        
        // Generate LEAFNODES section
        generateLeafNodesXML(xml, nodeIds);
        
        xml.append("</SYNTREE>");
        return xml.toString();
    }

    public void saveXMLToFile(String filename) throws IOException {
        String xmlContent = generateXML();
        
        try {
            Files.writeString(Paths.get(filename), xmlContent);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            throw e;
        }
    }

    public void saveXMLToFile(String filename, boolean pretty) throws IOException {
        String xmlContent = generateXML();
        
        if (pretty) {
            // Simple pretty printing (you might want to use a proper XML formatter for more complex cases)
            xmlContent = xmlContent.replaceAll(">>", ">\n>")
                                 .replaceAll("><", ">\n<");
        }
        
        try {
            Files.writeString(Paths.get(filename), xmlContent);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            throw e;
        }
    }
    
    private int assignNodeIds(Node node, Map<Node, Integer> nodeIds, int nextId) {
        if (node == null) return nextId;
        
        nodeIds.put(node, nextId++);
        
        for (Node child : node.children) {
            nextId = assignNodeIds(child, nodeIds, nextId);
        }
        
        return nextId;
    }
    
    private void generateRootXML(StringBuilder xml, Map<Node, Integer> nodeIds) {
        xml.append("  <ROOT>\n");
        xml.append("    <UNID>").append(nodeIds.get(root)).append("</UNID>\n");
        // Use the Symbol's identifier field
        xml.append("    <SYMB>").append(escapedXML(root.identifier.identifier)).append("</SYMB>\n");
        
        if (!root.children.isEmpty()) {
            xml.append("    <CHILDREN>\n");
            for (Node child : root.children) {
                xml.append("      <ID>").append(nodeIds.get(child)).append("</ID>\n");
            }
            xml.append("    </CHILDREN>\n");
        }
        
        xml.append("  </ROOT>\n");
    }
    
    private void generateInnerNodesXML(StringBuilder xml, Map<Node, Integer> nodeIds) {
        List<Node> innerNodes = new ArrayList<>();
        collectInnerNodes(root, innerNodes);
        
        if (!innerNodes.isEmpty()) {
            xml.append("  <INNERNODES>\n");
            
            for (Node node : innerNodes) {
                // Only include nodes that are non-terminals
                if (!node.terminal && !node.children.isEmpty()) {
                    xml.append("    <IN>\n");
                    xml.append("      <PARENT>").append(nodeIds.get(node.parent)).append("</PARENT>\n");
                    xml.append("      <UNID>").append(nodeIds.get(node)).append("</UNID>\n");
                    xml.append("      <SYMB>").append(escapedXML(node.identifier.identifier)).append("</SYMB>\n");
                    
                    xml.append("      <CHILDREN>\n");
                    for (Node child : node.children) {
                        xml.append("        <ID>").append(nodeIds.get(child)).append("</ID>\n");
                    }
                    xml.append("      </CHILDREN>\n");
                    xml.append("    </IN>\n");
                }
            }
            
            xml.append("  </INNERNODES>\n");
        }
    }

    private void collectInnerNodes(Node node, List<Node> innerNodes) {
        if (node == null) return;
        
        // If it's not the root and not a terminal, it's an inner node
        if (node != root && !node.terminal) {
            innerNodes.add(node);
        }
        
        for (Node child : node.children) {
            collectInnerNodes(child, innerNodes);
        }
    }
    
    private void generateLeafNodesXML(StringBuilder xml, Map<Node, Integer> nodeIds) {
        List<Node> leafNodes = new ArrayList<>();
        collectLeafNodes(root, leafNodes);
        
        if (!leafNodes.isEmpty()) {
            xml.append("  <LEAFNODES>\n");
            
            for (Node leaf : leafNodes) {
                // Only include nodes that are terminals
                if (leaf.terminal) {
                    xml.append("    <LEAF>\n");
                    xml.append("      <PARENT>").append(nodeIds.get(leaf.parent)).append("</PARENT>\n");
                    xml.append("      <UNID>").append(nodeIds.get(leaf)).append("</UNID>\n");
                    xml.append("      <TERMINAL>\n");
                    if (leaf.token != null) {
                        // Assuming Token class has a toXML() method or similar
                        xml.append(leaf.token.toXML()).append("\n");
                    }
                    xml.append("      </TERMINAL>\n");
                    xml.append("    </LEAF>\n");
                }
            }
            
            xml.append("  </LEAFNODES>\n");
        }
    }
    
    private void collectLeafNodes(Node node, List<Node> leafNodes) {
        if (node == null) return;

        // print node
        System.out.println(node.identifier.identifier);
        System.out.println(node.terminal);
        
        if (node.terminal) {
            leafNodes.add(node);
        } else {
            for (Node child : node.children) {
                collectLeafNodes(child, leafNodes);
            }
        }
    }

    // Helper method to escape special XML characters
    private String escapedXML(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
