package edu.ufl.cise.plc;

import edu.ufl.cise.plc.Token;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.text.Position;

import java.util.List;

public class Lexer implements ILexer {

    private Token.Kind kind;
    private String input;

    // we can create a 2d array and save the value of every token inside on of the
    // cells with the "string name" of the token
    // and the value of the token
    // or we can use a hashmap
    int lexemmelength;
    boolean done;
    int pos = 0;
    int line = 0;
    int col = 0;
    int startPos = 0;

    @Override
    public IToken next() throws LexicalException {
        if (pos >= sourceCode.length()) {
            Token nextToken = new Token(IToken.Kind.EOF, null, null, 0);
            tokens.add(nextToken);
            return nextToken;
        } else {
            Token nextToken = getToken();
            tokens.add(nextToken);
            return nextToken;

        }

    }

    @Override
    public IToken peek() throws LexicalException {
        if (pos >= sourceCode.length()) {
            Token nextToken = new Token(IToken.Kind.EOF, null, null, 0);

            // to do: figure out how to track the position of the token

            return nextToken;
        } else {
            Token newToken = getToken();
            return newToken;
        }
    }

    public enum STATE {
        START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_STRING,
        IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS, HAVE_PLUS, WHITE_SPACE, TIMES, HAVE_LT, HAVE_BANG, HAVE_EX, HAVE_GT,
        HAVE_SLASH;
    }

    STATE State;

    ArrayList<Token> tokens = new ArrayList<Token>();

    String sourceCode;

    HashMap<Token.Kind, State> rsrvd = new HashMap<>(); // hashmap for the reserved words of the programming language,
                                                        // we
    // will use this after we have the identifier and we will compare it
    // with the hashmap.

    // rsrvd.put(TYPE, 'int'), somehow create a hashmap of all the reserved words
    // like this

    public Lexer(String sourceCode) {
        this.sourceCode = sourceCode;
        State = STATE.START;
        initializeMap();

    }

    private void initializeMap() {

    }

    private Token getToken() throws LexicalException {

        IToken.SourceLocation tokenPosition = null;

        char ch = sourceCode.charAt(pos); // Can i find some other way to iterate over the complete code, for loop ? -
                                          // too
        // tacky plus conditions will make it difficult
        // while loop?
        switch (State) { // implementing the DFA, this switch Statement is to define the States of the
                         // DFA

            case START:

                switch (ch) { // this switch case works on the characters of the string.
                    case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z':
                    case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z':
                    case '$':
                    case '_': {
                        pos++;
                        State = STATE.IN_IDENT;
                        // Save these values in tokens list
                        // array list TOKENS<String "identfier",string "p">
                    }
                    case ' ', '\t', '\r': {
                        col++;
                        pos++;
                    }
                    case '\n': {
                        // line?
                        pos++;
                        col = 0;
                    }
                    case '+': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        kind = Token.Kind.PLUS;
                        startPos = pos;
                        State = STATE.HAVE_PLUS; // can i remove this?
                        Token newToken = new Token(kind, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '=': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        kind = IToken.Kind.EQUALS;
                        startPos = pos;
                        State = STATE.HAVE_EQ;
                        col++;
                        pos++;

                    }
                    case '*': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        kind = Token.Kind.TIMES;
                        startPos = pos;
                        Token newToken = new Token(kind, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '/': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.DIV, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '%': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.MOD, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '(': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.LPAREN, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case ')': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.RPAREN, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '[': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.LSQUARE, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case ']': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.RSQUARE, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '&': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.AND, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '|': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.OR, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case ';': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.SEMI, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case ',': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.COMMA, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '^': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        Token newToken = new Token(IToken.Kind.RETURN, String.valueOf(ch), tokenPosition, 1);
                        tokens.add(newToken);
                        col++;
                        pos++;
                        return newToken;
                    }
                    // MINUS
                    case '-': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        State = STATE.HAVE_MINUS;
                        col++;
                        pos++;
                    }
                    // BANG
                    case '!': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        State = STATE.HAVE_EX;
                        col++;
                        pos++;
                    }
                    // LT
                    case '<': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        State = STATE.HAVE_LT;
                        col++;
                        pos++;
                    }
                    // GT
                    case '>': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        State = STATE.HAVE_GT;
                        col++;
                        pos++;
                    }
                    // Start of String
                    case '"': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        State = STATE.IN_STRING;
                        col++;
                        pos++;
                    }
                    case '0': {
                        tokenPosition = new IToken.SourceLocation(line, col);
                        startPos = pos;
                        State = STATE.HAVE_ZERO;
                        col++;
                        pos++;
                    }

                    // to do : Move to some different State
                    // State = have
                    // return identifier or save it somewhere to let the program know, save it in
                    // array list ? umm how about
                    // we save it like ('string','token') but this will have to be done in the end
                    // of the execution of the dfa
                    // let's do it like this, create an array list(string name, token Token.kind)
                    // where as soon as we hit the final State we add it into the array list. and
                    // array list can be named tokens?

                }
            case IN_IDENT:
                switch (ch) {
                    case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z':
                    case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z':
                    case '$':
                    case '_':
                    case '1', '2', '3', '4', '5', '6', '7', '8', '9': {
                        State = STATE.IN_IDENT;
                        if (ch == ' ') {
                            // check reserved words, have to understand how the scanning in reserved words
                            // will work.
                            // compare the strings directly is the first guess.
                            // check the github repo you found to understand how to scan the hash map.
                            // reserved r = reserves.get(newTokenString);
                            /*
                             * if(r == ident){ this is a string in java probably we might have to use
                             * somethign like compare or something
                             * we will have to report an error here
                             * }
                             * else{
                             * //creating a token
                             * Token.Kind = Token.kind.IDENT;
                             * length = pos;
                             * Pos = startPOS;
                             * 
                             * }
                             */

                        }
                    }
                }
            case HAVE_ZERO:
                switch (ch) {
                    case '.': {
                        State = STATE.HAVE_DOT;
                        pos++;
                        col++;
                    }
                    default: {
                        Token newToken = new Token(IToken.Kind.INT_LIT, sourceCode.substring(startPos, pos),
                                tokenPosition, (pos - startPos));
                    }
                }
            case IN_NUM:
                switch (ch) {
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9': {
                        pos++;
                        col++;
                    }
                    case '.': {
                        State = STATE.HAVE_DOT;
                        col++;
                        pos++;
                    }
                    default: {
                        Token newToken = new Token(IToken.Kind.INT_LIT, sourceCode, tokenPosition, (pos - startPos));
                    }
                }
            case IN_STRING: {

                switch (ch) {

                    case '\\': {
                        State = STATE.HAVE_SLASH;
                        col++;
                        pos++;
                    }
                    case '"': {
                        Token newToken = new Token(IToken.Kind.STRING_LIT, sourceCode.substring(startPos, pos + 1),
                                tokenPosition, (pos + 1) - startPos);
                        State = STATE.START;
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '\n': {
                        col = 0;
                        line++;
                        pos++;
                    }
                    default: {
                        col++;
                        pos++;
                    }

                }

            }

            case HAVE_DOT: {
                switch (ch) {
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9': {
                        pos++;
                        col++;
                        State = STATE.IN_FLOAT;
                    }
                    default: {
                        throw new LexicalException("found error with input number(check number again)",
                                tokenPosition.line(), tokenPosition.column());
                    }
                }
            }
            case IN_FLOAT: {
                switch (ch) {
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9': {
                        kind = Token.Kind.FLOAT_LIT;
                        pos++;
                        col++;
                        // length = pos;
                    }
                    default: {
                        Token newToken = new Token(IToken.Kind.FLOAT_LIT, sourceCode.substring(startPos, pos),
                                tokenPosition, (pos - startPos));

                        return newToken;
                    }
                }
            }
            case HAVE_GT: {

                switch (ch) {

                    case '=': {
                        Token newToken = new Token(IToken.Kind.GE, sourceCode.substring(startPos, pos + 1),
                                tokenPosition, (pos + 1) - startPos);
                        State = STATE.START;
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '>': {
                        Token newToken = new Token(IToken.Kind.RANGLE, sourceCode.substring(startPos, pos + 1),
                                tokenPosition, (pos + 1) - startPos);
                        State = STATE.START;
                        col++;
                        pos++;
                        return newToken;
                    }
                    default: {
                        Token newToken = new Token(IToken.Kind.GT, sourceCode.substring(startPos, pos), tokenPosition,
                                1);
                        State = STATE.START;
                        return newToken;
                    }

                }

            }
            case HAVE_LT: {

                switch (ch) {

                    case '=': {
                        Token newToken = new Token(IToken.Kind.LE, sourceCode.substring(startPos, pos + 1),
                                tokenPosition, (pos + 1) - startPos);
                        State = STATE.START;
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '-': {
                        Token newToken = new Token(IToken.Kind.LARROW, sourceCode.substring(startPos, pos + 1),
                                tokenPosition, (pos + 1) - startPos);
                        State = STATE.START;
                        col++;
                        pos++;
                        return newToken;
                    }
                    case '<': {
                        Token newToken = new Token(IToken.Kind.LANGLE, sourceCode.substring(startPos, pos + 1),
                                tokenPosition, (pos + 1) - startPos);
                        State = STATE.START;
                        col++;
                        pos++;
                        return newToken;
                    }
                    default: {
                        Token newToken = new Token(IToken.Kind.LT, sourceCode.substring(startPos, pos), tokenPosition,
                                1);
                        State = STATE.START;
                        return newToken;
                    }

                }

            }
            case HAVE_EQ: {

                switch (ch) {

                    case '=': {
                        Token newToken = new Token(IToken.Kind.EQUALS, sourceCode.substring(startPos, pos + 1),
                                tokenPosition, (pos + 1) - startPos);
                        State = STATE.START;
                        col++;
                        pos++;
                        return newToken;
                    }
                    default: {
                        Token newToken = new Token(IToken.Kind.ASSIGN, sourceCode.substring(startPos, pos),
                                tokenPosition, 1);
                        State = STATE.START;
                        return newToken;
                    }

                }

            }
            case HAVE_BANG: {

                switch (ch) {

                    case '=': {
                        Token newToken = new Token(IToken.Kind.NOT_EQUALS, sourceCode.substring(startPos, pos + 1),
                                tokenPosition, (pos + 1) - startPos);
                        State = STATE.START;
                        col++;
                        pos++;
                        return newToken;
                    }
                    default: {
                        Token newToken = new Token(IToken.Kind.BANG, sourceCode.substring(startPos, pos), tokenPosition,
                                1);
                        State = STATE.START;
                        return newToken;
                    }

                }

            }
            case HAVE_MINUS: {

                switch (ch) {

                    case '>': {
                        Token newToken = new Token(IToken.Kind.RARROW, sourceCode.substring(startPos, pos + 1),
                                tokenPosition, (pos + 1) - startPos);
                        State = STATE.START;
                        col++;
                        pos++;
                        return newToken;
                    }
                    default: {
                        Token newToken = new Token(IToken.Kind.MINUS, sourceCode.substring(startPos, pos),
                                tokenPosition, 1);
                        State = STATE.START;
                        return newToken;
                    }

                }

            }

            default:
                throw new IllegalStateException("lexer bug");

        }

    }

    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

    }

}
