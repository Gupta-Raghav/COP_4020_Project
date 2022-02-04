package edu.ufl.cise.plc;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.ufl.cise.plc.IToken.Kind;

public class Lexer implements ILexer {

    int pos = 0;
    int line = 0;
    int col = 0;
    int startPos = 0;

    @Override
    public IToken next() throws LexicalException {
        if (pos >= sourceCode.length()) {
            Token newToken = new Token(IToken.Kind.EOF, null, null, 0);
            tokens.add(newToken);
            return newToken;
        }

        Token newToken = getToken();
        tokens.add(newToken);
        return newToken;

    }

    @Override
    public IToken peek() throws LexicalException {
        if (pos >= sourceCode.length()) {
            Token newToken = new Token(IToken.Kind.EOF, null, null, 0);
            col -= (pos - startPos) + 1;
            pos = startPos;
            // to do-> figure out how to track the position of the token

            return newToken;
        }
        Token newToken = getToken();

        if (newToken.getKind() == IToken.Kind.EOF) {
            col--;
            pos--;
        } else {
            col -= (pos - startPos);
            pos = startPos;
        }
        return newToken;
    }

    private enum STATE {
        START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_STRING,
        IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS, HAVE_PLUS, WHITE_SPACE, TIMES, HAVE_LT, HAVE_EX, HAVE_GT,
        HAVE_SLASH, IN_COMMENT;
    }

    private STATE currState;

    ArrayList<Token> tokens = new ArrayList<Token>();

    String sourceCode;

    HashMap<String, IToken.Kind> rsrvd = new HashMap<>(); // hashmap for the reserved words of the programming language,
                                                          // we
    // will use this after we have the identifier and we will compare it
    // with the hashmap.

    // rsrvd.put(TYPE, 'int'), somehow create a hashmap of all the reserved words
    // like this

    public Lexer(String sourceCode) {
        this.sourceCode = sourceCode;
        currState = STATE.START;
        HashMap();

    }

    private Token getToken() throws LexicalException {

        IToken.SourceLocation tokenPosition = null;

        // Can i find some other way to iterate over the complete code, for loop ? -
        // too
        // tacky plus conditions will make it difficult
        // while loop?
        while (true) {

            if (pos >= sourceCode.length()) {
                Token newToken = new Token(IToken.Kind.EOF, null, null, 0);
                return newToken;
            }

            char ch = sourceCode.charAt(pos);
            switch (currState) { // implementing the DFA, this switch Statement is to define the States of the
                // DFA
                case START -> {
                    switch (ch) { // this switch case works on the characters of the string.
                        case ' ', '\t', '\r' -> {
                            col++;
                            pos++;
                        }
                        case '\n' -> {
                            line++; // line
                            pos++;
                            col = 0;
                        }
                        case '+' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            // can i remove this?
                            Token newToken = new Token(IToken.Kind.PLUS, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '=' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            currState = STATE.HAVE_EQ;
                            col++;
                            pos++;

                        }
                        case '*' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.TIMES, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '#' -> {
                            currState = STATE.IN_COMMENT;
                            col++;
                            pos++;
                        }
                        case '/' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.DIV, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '%' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.MOD, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '(' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.LPAREN, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case ')' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.RPAREN, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '[' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.LSQUARE, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case ']' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.RSQUARE, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '&' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.AND, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '|' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.OR, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case ';' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.SEMI, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case ',' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.COMMA, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '^' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            Token newToken = new Token(IToken.Kind.RETURN, String.valueOf(ch), tokenPosition, 1);
                            tokens.add(newToken);
                            col++;
                            pos++;
                            return newToken;
                        }

                        case '-' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            currState = STATE.HAVE_MINUS;
                            col++;
                            pos++;
                        }

                        case '!' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            currState = STATE.HAVE_EX;
                            col++;
                            pos++;
                        }

                        case '<' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            currState = STATE.HAVE_LT;
                            col++;
                            pos++;
                        }
                        case '>' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            currState = STATE.HAVE_GT;
                            col++;
                            pos++;
                        }
                        case '"' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            currState = STATE.IN_STRING;
                            col++;
                            pos++;
                        }
                        case '0' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            currState = STATE.HAVE_ZERO;
                            col++;
                            pos++;
                        }
                        case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            tokenPosition = new IToken.SourceLocation(line, col);
                            startPos = pos;
                            currState = STATE.IN_NUM;
                            col++;
                            pos++;
                        }
                        // case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                        // 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'-> {
                        // tokenPosition = new IToken.SourceLocation(line, col);
                        // startPos = pos;
                        // currState = STATE.IN_IDENT;
                        // col++;
                        // pos++;
                        // }
                        // case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                        // 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'-> {
                        // tokenPosition = new IToken.SourceLocation(line, col);
                        // startPos = pos;
                        // currState = STATE.IN_IDENT;
                        // col++;
                        // pos++;
                        // }
                        // case '$'-> {
                        // tokenPosition = new IToken.SourceLocation(line, col);
                        // startPos = pos;
                        // currState = STATE.IN_IDENT;
                        // col++;
                        // pos++;
                        // }
                        // case '_'-> {
                        // tokenPosition = new IToken.SourceLocation(line, col);
                        // startPos = pos;
                        // currState = STATE.IN_IDENT;
                        // col++;
                        // pos++;
                        // }
                        default -> {
                            if (Character.isJavaIdentifierStart(ch)) {
                                tokenPosition = new IToken.SourceLocation(line, col);
                                startPos = pos;
                                currState = STATE.IN_IDENT;
                                col++;
                                pos++;

                            } else {
                                tokenPosition = new IToken.SourceLocation(line, col);
                                throw new LexicalException("Invalid character", tokenPosition.line(),
                                        tokenPosition.column());
                            }
                        }

                    }
                }
                case IN_IDENT -> {
                    if (Character.isJavaIdentifierPart(ch)) {
                        col++;
                        pos++;

                    } else {
                        String ident = sourceCode.substring(startPos, pos);
                        for (Map.Entry<String, IToken.Kind> entry : rsrvd.entrySet()) {
                            if (ident.equals(entry.getKey())) {
                                Token newToken = new Token(entry.getValue(), ident, tokenPosition,
                                        (pos - startPos));
                                currState = STATE.START;
                                return newToken;
                            }
                        }
                        Token newToken = new Token(IToken.Kind.IDENT, ident, tokenPosition, pos - startPos);
                        currState = STATE.START;
                        return newToken;
                    }
                }
                case HAVE_ZERO -> {
                    switch (ch) {
                        case '.' -> {
                            currState = STATE.HAVE_DOT;
                            pos++;
                            col++;
                        }
                        default -> {
                            Token newToken = new Token(IToken.Kind.INT_LIT, sourceCode.substring(startPos, pos),
                                    tokenPosition, (pos - startPos));
                            currState = STATE.START;
                            return newToken;
                        }
                    }
                }
                case IN_NUM -> {
                    switch (ch) {
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            pos++;
                            col++;
                        }
                        case '.' -> {
                            currState = STATE.HAVE_DOT;
                            col++;
                            pos++;
                        }
                        default -> {
                            String str = sourceCode.substring(startPos, pos);
                            Token newToken = new Token(IToken.Kind.INT_LIT, str, tokenPosition,
                                    (pos - startPos));

                            try {
                                int temp = Integer.valueOf(str);
                            } catch (Exception e) {
                                throw new LexicalException("Invalid integer", tokenPosition.line(),
                                        tokenPosition.column());
                            }
                            currState = STATE.START;
                            return newToken;
                        }
                    }
                }
                case HAVE_DOT -> {
                    switch (ch) {
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            currState = STATE.IN_FLOAT;
                            col++;
                            pos++;
                        }
                        default -> {
                            throw new LexicalException("found error with input number(check number again)",
                                    tokenPosition.line(), tokenPosition.column());
                        }
                    }
                }
                case IN_FLOAT -> {
                    switch (ch) {
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            col++;
                            pos++;
                        }
                        default -> {
                            String fl = sourceCode.substring(startPos, pos);

                            Token newToken = new Token(IToken.Kind.FLOAT_LIT, sourceCode.substring(startPos, pos),
                                    tokenPosition, (pos - startPos));

                            try {
                                float floatValue = Float.valueOf(fl);
                            } catch (Exception e) {
                                throw new LexicalException("Invalid float input", tokenPosition.line(),
                                        tokenPosition.column());
                            }
                            currState = STATE.START;
                            return newToken;
                        }
                    }
                }
                case HAVE_GT -> {

                    switch (ch) {

                        case '=' -> {
                            Token newToken = new Token(IToken.Kind.GE, sourceCode.substring(startPos, pos + 1),
                                    tokenPosition, (pos + 1) - startPos);
                            currState = STATE.START;
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '>' -> {
                            Token newToken = new Token(IToken.Kind.RANGLE, sourceCode.substring(startPos, pos + 1),
                                    tokenPosition, (pos + 1) - startPos);
                            currState = STATE.START;
                            col++;
                            pos++;
                            return newToken;
                        }
                        default -> {
                            Token newToken = new Token(IToken.Kind.GT, sourceCode.substring(startPos, pos),
                                    tokenPosition,
                                    1);
                            currState = STATE.START;
                            return newToken;
                        }

                    }

                }
                case HAVE_LT -> {

                    switch (ch) {

                        case '=' -> {
                            Token newToken = new Token(IToken.Kind.LE, sourceCode.substring(startPos, pos + 1),
                                    tokenPosition, (pos + 1) - startPos);
                            currState = STATE.START;
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '-' -> {
                            Token newToken = new Token(IToken.Kind.LARROW, sourceCode.substring(startPos, pos + 1),
                                    tokenPosition, (pos + 1) - startPos);
                            currState = STATE.HAVE_MINUS;
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '<' -> {
                            Token newToken = new Token(IToken.Kind.LANGLE, sourceCode.substring(startPos, pos + 1),
                                    tokenPosition, (pos + 1) - startPos);
                            currState = STATE.START;
                            col++;
                            pos++;
                            return newToken;
                        }
                        default -> {
                            Token newToken = new Token(IToken.Kind.LT, sourceCode.substring(startPos, pos),
                                    tokenPosition,
                                    1);
                            currState = STATE.START;
                            return newToken;
                        }

                    }

                }
                case HAVE_EQ -> {

                    switch (ch) {

                        case '=' -> {
                            Token newToken = new Token(IToken.Kind.EQUALS, sourceCode.substring(startPos, pos + 1),
                                    tokenPosition, (pos + 1) - startPos);
                            currState = STATE.START;
                            col++;
                            pos++;
                            return newToken;
                        }
                        default -> {
                            Token newToken = new Token(IToken.Kind.ASSIGN, sourceCode.substring(startPos, pos),
                                    tokenPosition, 1);
                            currState = STATE.START;
                            return newToken;
                        }

                    }

                }
                case HAVE_EX -> {

                    switch (ch) {

                        case '=' -> {
                            Token newToken = new Token(IToken.Kind.NOT_EQUALS,
                                    sourceCode.substring(startPos, pos + 1),
                                    tokenPosition, (pos + 1) - startPos);
                            currState = STATE.START;
                            col++;
                            pos++;
                            return newToken;
                        }
                        default -> {
                            Token newToken = new Token(IToken.Kind.BANG, sourceCode.substring(startPos, pos),
                                    tokenPosition,
                                    1);
                            currState = STATE.START;
                            return newToken;
                        }

                    }

                }
                case HAVE_MINUS -> {

                    switch (ch) {

                        case '>' -> {
                            Token newToken = new Token(IToken.Kind.RARROW, sourceCode.substring(startPos, pos + 1),
                                    tokenPosition, (pos + 1) - startPos);
                            currState = STATE.START;
                            col++;
                            pos++;
                            return newToken;
                        }
                        default -> {
                            Token newToken = new Token(IToken.Kind.MINUS, sourceCode.substring(startPos, pos),
                                    tokenPosition, 1);
                            currState = STATE.START;
                            return newToken;
                        }

                    }

                }
                case IN_STRING -> {

                    switch (ch) {

                        case '\\' -> {
                            currState = STATE.HAVE_SLASH;
                            col++;
                            pos++;
                        }
                        case '"' -> {
                            Token newToken = new Token(IToken.Kind.STRING_LIT,
                                    sourceCode.substring(startPos, pos + 1),
                                    tokenPosition, (pos + 1) - startPos);
                            currState = STATE.START;
                            col++;
                            pos++;
                            return newToken;
                        }
                        case '\n' -> {
                            col = 0;
                            line++;
                            pos++;
                        }
                        default -> {
                            col++;
                            pos++;
                        }

                    }

                }
                case HAVE_SLASH -> {

                    switch (ch) {

                        case 'b', 't', 'n', 'f', 'r', '\\', '\'', '\"' -> {
                            currState = STATE.IN_STRING;
                            col++;
                            pos++;
                        }
                        default -> {
                            throw new LexicalException("Invalid escape character", tokenPosition.line(),
                                    tokenPosition.column());
                        }

                    }

                }
                case IN_COMMENT -> {

                    switch (ch) {

                        case '\n' -> {
                            currState = STATE.START;
                            line++;
                            col = 0;
                            pos++;
                        }

                        default -> {
                            col++;
                            pos++;
                        }

                    }

                }

            }

        }

    }

    private void HashMap() {
        rsrvd.put("true", IToken.Kind.BOOLEAN_LIT);
        rsrvd.put("false", IToken.Kind.BOOLEAN_LIT);
        rsrvd.put("BLACK", IToken.Kind.COLOR_CONST);
        rsrvd.put("BLUE", IToken.Kind.COLOR_CONST);
        rsrvd.put("CYAN", IToken.Kind.COLOR_CONST);
        rsrvd.put("DARK_GRAY", IToken.Kind.COLOR_CONST);
        rsrvd.put("DARK_GREY", IToken.Kind.COLOR_CONST);
        rsrvd.put("GRAY", IToken.Kind.COLOR_CONST);
        rsrvd.put("GREY", IToken.Kind.COLOR_CONST);
        rsrvd.put("GREEN", IToken.Kind.COLOR_CONST);
        rsrvd.put("LIGHT_GRAY", IToken.Kind.COLOR_CONST);
        rsrvd.put("LIGHT_GREY", IToken.Kind.COLOR_CONST);
        rsrvd.put("MAGENTA", IToken.Kind.COLOR_CONST);
        rsrvd.put("ORANGE", IToken.Kind.COLOR_CONST);
        rsrvd.put("PINK", IToken.Kind.COLOR_CONST);
        rsrvd.put("RED", IToken.Kind.COLOR_CONST);
        rsrvd.put("WHITE", IToken.Kind.COLOR_CONST);
        rsrvd.put("YELLOW", IToken.Kind.COLOR_CONST);
        rsrvd.put("if", IToken.Kind.KW_IF);
        rsrvd.put("fi", IToken.Kind.KW_FI);
        rsrvd.put("else", IToken.Kind.KW_ELSE);
        rsrvd.put("write", IToken.Kind.KW_WRITE);
        rsrvd.put("console", IToken.Kind.KW_CONSOLE);
        rsrvd.put("int", IToken.Kind.TYPE);
        rsrvd.put("float", IToken.Kind.TYPE);
        rsrvd.put("string", IToken.Kind.TYPE);
        rsrvd.put("boolean", IToken.Kind.TYPE);
        rsrvd.put("color", IToken.Kind.TYPE);
        rsrvd.put("image", IToken.Kind.TYPE);
        rsrvd.put("getRed", IToken.Kind.COLOR_OP);
        rsrvd.put("getGreen", IToken.Kind.COLOR_OP);
        rsrvd.put("getBlue", IToken.Kind.COLOR_OP);
        rsrvd.put("getWidth", IToken.Kind.IMAGE_OP);
        rsrvd.put("getHeight", IToken.Kind.IMAGE_OP);
        rsrvd.put("void", IToken.Kind.KW_VOID);

    }

}
