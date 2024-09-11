package Lexer;

public class Token {
    public int id;
    public String tokenClass; // token class
    public String tokenValue; // * keep string for now, conversion takes place at a later stage

    public Token (int _id, String _tokenClass, String _tokenValue) {
        id = _id;
        tokenClass = _tokenClass;
        tokenValue = _tokenValue;
    }
}
