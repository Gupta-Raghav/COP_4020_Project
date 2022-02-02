package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken;

public class Token implements IToken {

    private Kind tokenType;
    private String sourceCode;
    private SourceLocation position;
    private int length;

    public TokenClass(Kind tokenType, String sourceCode, SourceLocation position, int length) {
        this.tokenType = tokenType;
        this.sourceCode = sourceCode;
        this.position = position;
        this.length = length;
    }

    @Override
    public Kind getKind() {
        return tokenType;
    }

    @Override
    public String getText() {
        return sourceCode;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return position;
    }

    @Override
    public int getIntValue() {
        return Integer.valueOf(sourceCode);
    }

    @Override
    public float getFloatValue() {
        return Float.valueOf(sourceCode);
    }

    @Override
    public boolean getBooleanValue() {
        return Boolean.valueOf(sourceCode);
    }

    // Need to change this so that it removes the "s and the escape sequences
    @Override
    public String getStringValue() {

        String stringValue = "";

        for (int i = 1; i < sourceCode.length() - 1; i++) {

            if (sourceCode.charAt(i) != '\\') {
                stringValue += sourceCode.charAt(i);
            } else {
                switch (sourceCode.charAt(i + 1)) {

                    case 'b' -> {
                        stringValue += '\b';
                    }
                    case 't' -> {
                        stringValue += '\t';
                    }
                    case 'n' -> {
                        stringValue += '\n';
                    }
                    case 'f' -> {
                        stringValue += '\f';
                    }
                    case 'r' -> {
                        stringValue += '\r';
                    }
                    case '"' -> {
                        stringValue += '\"';
                    }
                    case '\'' -> {
                        stringValue += '\'';
                    }
                    case '\\' -> {
                        stringValue += '\\';
                    }

                }
                i++;
            }

        }

        return stringValue;
    }

}