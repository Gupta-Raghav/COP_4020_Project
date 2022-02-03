package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import edu.ufl.cise.plc.Token;

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

    public enum State {
        START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_STRING,
        IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS, HAVE_PLUS, WHITE_SPACE, TIMES;
    }

    State state;

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
        state = State.START;
        initializeMap();

    }

    private void initializeMap() {

    }

    public Token getToken() throws LexicalException {

        char ch = sourceCode.charAt(pos); // Can i find some other way to iterate over the complete code, for loop ? -
                                          // too
        // tacky plus conditions will make it difficult
        // while loop?
        int startPOS = pos;
        switch (state) { // implementing the DFA, this switch statement is to define the states of the
                         // DFA

            case START:

                switch (ch) { // this switch case works on the characters of the string.
                    case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z':
                    case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z':
                    case '$':
                    case '_': {
                        pos++;
                        state = State.IN_IDENT;
                        // Save these values in tokens list
                        // array list TOKENS<String "identfier",string "p">
                    }
                    case ' ', '\t', '\r': {
                        col++;
                        pos++;
                    }
                    case '\n': {
                        pos++;
                        col = 0;
                    }
                    case '+': {
                        kind = Token.Kind.PLUS;
                        pos = startPOS;
                        state = State.HAVE_PLUS;
                    }
                    case '=': {
                        pos++;
                        state = State.HAVE_EQ;
                    }
                    case '*': {
                        pos = startPOS;
                        state = State.TIMES;
                    }
                    case '"': {
                        pos++;
                        state = State.IN_STRING;
                    }
                    case 0: {
                        // have to add an EOF token in the tokens file in the end

                    }
                    // to do : Move to some different state
                    // state = have
                    // return identifier or save it somewhere to let the program know, save it in
                    // array list ? umm how about
                    // we save it like ('string','token') but this will have to be done in the end
                    // of the execution of the dfa
                    // let's do it like this, create an array list(string name, token Token.kind)
                    // where as soon as we hit the final state we add it into the array list. and
                    // array list can be named tokens?

                }
            case IN_IDENT:
                switch (ch) {
                    case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z':
                    case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z':
                    case '$':
                    case '_':
                    case '1', '2', '3', '4', '5', '6', '7', '8', '9': {
                        state = State.IN_IDENT;
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
                        state = State.HAVE_DOT;
                        pos++;
                    }
                }
            case IN_NUM:
                switch (ch) {
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9': {
                        if (ch + 1 == ' ') {
                            pos++;
                            kind = Token.Kind.INT_LIT;
                            pos = startPOS;
                        } else if (ch == '.') {
                            pos++;
                            state = State.HAVE_DOT;
                        } else {
                            pos++;
                            state = State.IN_NUM;
                        }
                    }
                }
            case IN_STRING:
                switch (ch) {
                    case '\\': {

                    }
                }

            case HAVE_DOT: {
                switch (ch) {
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9': {
                        pos++;
                        state = State.IN_FLOAT;
                    }
                }
            }
            case IN_FLOAT: {
                switch (ch) {
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9': {
                        kind = Token.Kind.FLOAT_LIT;
                        pos++;
                        // length = pos;
                        pos = startPOS;
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

}
