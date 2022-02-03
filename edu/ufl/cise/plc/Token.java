package edu.ufl.cise.plc;

public class Token implements IToken {

    private Kind Type;
    private String sourceCode;
    private SourceLocation pos;
    private int length;

    public Token(Kind Type, String sourceCode, SourceLocation pos, int length) {
        this.Type = Type;
        this.sourceCode = sourceCode;
        this.pos = pos;
        this.length = length;
    }

    @Override
    public Kind getKind() {
        return Type;
    }

    @Override
    public String getText() {
        return sourceCode;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return pos;
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

    @Override
    public String getStringValue() {

        String resString = "";

        for (int i = 1; i < sourceCode.length() - 1; i++) {

            if (sourceCode.charAt(i) != '\\') {
                resString += sourceCode.charAt(i);
            } else {
                switch (sourceCode.charAt(i + 1)) {

                    case 'r' -> {
                        resString += '\r';
                    }
                    case 'f' -> {
                        resString += '\f';
                    }
                    case 'n' -> {
                        resString += '\n';
                    }
                    case 't' -> {
                        resString += '\t';
                    }
                    case 'b' -> {
                        resString += '\b';
                    }
                    case '"' -> {
                        resString += '\"';
                    }
                    case '\'' -> {
                        resString += '\'';
                    }
                    case '\\' -> {
                        resString += '\\';
                    }

                }
                i++;
            }

        }

        return resString;
    }

}