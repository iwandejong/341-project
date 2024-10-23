package Lexer;

public class Token {
    public int id;
    public String tokenClass; // token class
    public String tokenValue; // * keep string for now, conversion takes place at a later stage

    public int line = 0;
    public int column = 0;

    public Token (int _id, String _tokenClass, String _tokenValue) {
        id = _id;
        tokenClass = _tokenClass;
        tokenValue = _tokenValue;
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("        <TOKEN>\n");
        xml.append("            <TYPE>").append(escapeXML(tokenClass)).append("</TYPE>\n");
        xml.append("            <VALUE>").append(escapeXML(tokenValue)).append("</VALUE>\n");
        // xml.append("            <LINE>").append(line).append("</LINE>\n");
        // xml.append("            <COLUMN>").append(column).append("</COLUMN>\n");
        xml.append("        </TOKEN>");
        return xml.toString();
    }

    private String escapeXML(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}