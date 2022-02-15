package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.PixelSelector;

import java.util.HashSet;

import static edu.ufl.cise.plc.CompilerComponentFactory.getLexer;

public class Parser implements IParser {
    ILexer lexer;
    Token currToken;
    Token nextToken;
    HashSet<IToken.Kind> ExprSet;
    HashSet<IToken.Kind> ConditionalExprSet;
    HashSet<IToken.Kind> LogicalOrExprSet;
    HashSet<IToken.Kind> LogicalAndExprSet;
    HashSet<IToken.Kind> ComparisonExprSet;
    HashSet<IToken.Kind> AdditiveExprSet;
    HashSet<IToken.Kind> MultiplicativeExprSet;
    HashSet<IToken.Kind> UnaryExprSet;
    HashSet<IToken.Kind> UnaryExprPostfixSet;
    HashSet<IToken.Kind> PrimaryExprSet;
    HashSet<IToken.Kind> PixelSelectorSet;

    void match(IToken.Kind kind) throws PLCException {
        if (kind == currToken.getKind()) {
            currToken = (Token) lexer.next();
        } else {
            throw new SyntaxException("expected currToken to match kind");
        }
    }

    void Expr() throws PLCException {
        if (ConditionalExprSet.contains(currToken.getKind())) {
            ConditionalExpr();
        } else if (LogicalOrExprSet.contains(currToken.getKind())) {
            LogicalOrExpr();
        } else {
            throw new SyntaxException("expected conditional or logical token in Expr");
        }
    }

    void ConditionalExpr() throws PLCException {
        if (currToken.getKind() == IToken.Kind.KW_IF) {
            match(IToken.Kind.KW_IF);
            match(IToken.Kind.LPAREN);
            Expr();
            match(IToken.Kind.RPAREN);
            Expr();
            match(IToken.Kind.KW_ELSE);
            Expr();
            match(IToken.Kind.KW_FI);
        } else {
            throw new SyntaxException("expected kw if in ConditionalExpr");
        }
    }

    void LogicalOrExpr() throws PLCException {
        if (LogicalOrExprSet.contains(currToken.getKind())) {
            LogicalAndExpr();
            while (currToken.getKind() == IToken.Kind.OR) {
                match(IToken.Kind.OR);
                LogicalAndExpr();
            }

        } else {
            throw new SyntaxException("expected logical and token in LogicalOrExpr");
        }
    }

    void LogicalAndExpr() throws PLCException {
        if (LogicalAndExprSet.contains(currToken.getKind())) {
            ComparisonExpr();
            while (IToken.Kind.AND == currToken.getKind()) { // x AND y, currToken.getKind returns AND,
                match(IToken.Kind.AND);
                ComparisonExpr();
            }
        } else {
            throw new SyntaxException("expected comparisonexpr in LogicalAndExpr");
        }
    }

    void ComparisonExpr() throws PLCException {
        AdditiveExpr();
        while ((currToken.getKind() == IToken.Kind.LARROW) || (currToken.getKind() == IToken.Kind.RARROW) ||
                (currToken.getKind() == IToken.Kind.EQUALS) || (currToken.getKind() == IToken.Kind.NOT_EQUALS) ||
                (currToken.getKind() == IToken.Kind.LE) || (currToken.getKind() == IToken.Kind.GE)) {
            if ((currToken.getKind() == IToken.Kind.LARROW)) {
                match(IToken.Kind.LARROW);
            } else if ((currToken.getKind() == IToken.Kind.RARROW)) {
                match(IToken.Kind.RARROW);
            } else if ((currToken.getKind() == IToken.Kind.EQUALS)) {
                match(IToken.Kind.EQUALS);
            } else if ((currToken.getKind() == IToken.Kind.NOT_EQUALS)) {
                match(IToken.Kind.NOT_EQUALS);
            } else if ((currToken.getKind() == IToken.Kind.LE)) {
                match(IToken.Kind.LE);
            } else if ((currToken.getKind() == IToken.Kind.GE)) {
                match(IToken.Kind.GE);
            } else {
                throw new SyntaxException("expected comparison operator in ComparisonExpr");
            }
            MultiplicativeExpr();
        }
    }

    void AdditiveExpr() throws PLCException {
        MultiplicativeExpr();
        while (IToken.Kind.PLUS == currToken.getKind() || IToken.Kind.MINUS == currToken.getKind()) {
            if (currToken.getKind() == IToken.Kind.PLUS) {
                match(IToken.Kind.PLUS);
            } else if (currToken.getKind() == IToken.Kind.MINUS) {
                match(IToken.Kind.MINUS);
            } else {
                throw new SyntaxException("expected addition operator in AdditiveExpr");
            }
            MultiplicativeExpr();
        }

    }

    void MultiplicativeExpr() throws PLCException {
        UnaryExpr();
        while (IToken.Kind.TIMES == currToken.getKind() || IToken.Kind.DIV == currToken.getKind()
                || IToken.Kind.MOD == currToken.getKind()) {
            if (currToken.getKind() == IToken.Kind.TIMES) {
                match(IToken.Kind.TIMES);
            } else if (currToken.getKind() == IToken.Kind.DIV) {
                match(IToken.Kind.DIV);
            } else if (currToken.getKind() == IToken.Kind.MOD) {
                match(IToken.Kind.MOD);
            } else {
                throw new SyntaxException("expected multiplication operator in MultiplicativeExpr");
            }
        }
    }

    void UnaryExpr() throws PLCException {
        if ((currToken.getKind() == IToken.Kind.BANG) || (currToken.getKind() == IToken.Kind.MINUS) ||
                (currToken.getKind() == IToken.Kind.COLOR_OP) || (currToken.getKind() == IToken.Kind.IMAGE_OP)) {
            if (currToken.getKind() == IToken.Kind.BANG) {
                match(IToken.Kind.BANG);
            } else if (currToken.getKind() == IToken.Kind.MINUS) {
                match(IToken.Kind.MINUS);
            } else if (currToken.getKind() == IToken.Kind.COLOR_OP) {
                match(IToken.Kind.COLOR_OP);
            } else if (currToken.getKind() == IToken.Kind.IMAGE_OP) {
                match(IToken.Kind.IMAGE_OP);
            } else {
                throw new SyntaxException("expected unary operator in UnaryExpr");
            }
            UnaryExpr();
        } else
            UnaryExprPostfix();
    }

    void UnaryExprPostfix() throws PLCException {
        PrimaryExpr();
        if (PixelSelectorSet.contains(currToken.getKind())) {
            PixelSelector();
        }
    }

    void PrimaryExpr() throws PLCException {
        if (PrimaryExprSet.contains(currToken.getKind())) {
            switch (currToken.getKind()) {
                case BOOLEAN_LIT -> match(IToken.Kind.BOOLEAN_LIT);
                case STRING_LIT -> match(IToken.Kind.STRING_LIT);
                case INT_LIT -> match(IToken.Kind.INT_LIT);
                case FLOAT_LIT -> match(IToken.Kind.FLOAT_LIT);
                case IDENT -> match(IToken.Kind.IDENT);
                case LPAREN -> {
                    match(IToken.Kind.LPAREN);
                    Expr();
                    match(IToken.Kind.LPAREN);
                }
                default -> throw new SyntaxException("expected PrimaryExpr in PrimaryExpr");
            }
        } else {
            throw new SyntaxException("expected primaryexpr in PrimaryExpr");
        }
    }

    void PixelSelector() throws PLCException {
        if (currToken.getKind() == IToken.Kind.LSQUARE) {
            match(IToken.Kind.LSQUARE);
            Expr();
            match(IToken.Kind.COMMA);
            Expr();
            match(IToken.Kind.RPAREN);
        } else
            throw new SyntaxException("Expected LSQUARE in PixelSelector");
    }

    public ASTNode parse() throws PLCException {
        while (true) {
            currToken = (Token) lexer.next();
            return new IdentExpr(nextToken);
        }
        // return new IdentExpr(new Token(0, 0, "", IToken.Kind.IDENT, 0));
    }

    public Parser(String input) {
        lexer = getLexer(input);
        PixelSelectorSet.add(IToken.Kind.LSQUARE);
        ConditionalExprSet.add(IToken.Kind.KW_IF);
        PrimaryExprSet.add(IToken.Kind.BOOLEAN_LIT);
        PrimaryExprSet.add(IToken.Kind.STRING_LIT);
        PrimaryExprSet.add(IToken.Kind.INT_LIT);
        PrimaryExprSet.add(IToken.Kind.FLOAT_LIT);
        PrimaryExprSet.add(IToken.Kind.IDENT);
        PrimaryExprSet.add(IToken.Kind.LPAREN);
        UnaryExprPostfixSet.addAll(PrimaryExprSet);
        UnaryExprSet.addAll(UnaryExprPostfixSet);
        UnaryExprSet.add(IToken.Kind.BANG);
        UnaryExprSet.add(IToken.Kind.MINUS);
        UnaryExprSet.add(IToken.Kind.COLOR_OP);
        UnaryExprSet.add(IToken.Kind.IMAGE_OP);

        MultiplicativeExprSet.addAll(UnaryExprSet);
        AdditiveExprSet.addAll(UnaryExprSet);
        ComparisonExprSet.addAll(UnaryExprSet);
        LogicalAndExprSet.addAll(UnaryExprSet);
        LogicalOrExprSet.addAll(UnaryExprSet);

        ExprSet.addAll(ConditionalExprSet);
        ExprSet.addAll(LogicalOrExprSet);
    }
}