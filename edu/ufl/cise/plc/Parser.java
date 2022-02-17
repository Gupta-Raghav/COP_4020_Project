package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.StringLitExpr;

import java.util.*;

import static edu.ufl.cise.plc.CompilerComponentFactory.getLexer;

public class Parser implements IParser {
    ILexer lexer;
    Token currToken;
    Token nextToken;

    private final ArrayList<Token> tokens;
    private int ind = 0;

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

    boolean match(IToken.Kind kind) throws PLCException {
        currToken = tokens.get(ind);
        if (kind == currToken.getKind()) {
            ind++;
            return true;
        } else {
            ind++;
            return false;
            // throw new SyntaxException("expected currToken to match kind");
        }

    }

    // private boolean peek(Object... patterns) {
    // for (int i = 0; i < patterns.length; i++) {
    // if (!currToken.has(i)) {
    // return false;
    // } else if (patterns[i] instanceof Token.Type) {
    // if (patterns[i] != currToken.get(i).getType()) {
    // return false;
    // }
    // } else if (patterns[i] instanceof String) {
    // if (!patterns[i].equals(currToken.get(i).getLiteral())) {
    // return false;
    // }
    // } else {
    // throw new AssertionError("Invalid pattern object: "
    // + patterns[i].getClass());
    // }
    // }
    // return true;
    // }

    // Expr :: = CondExpr | LogicExpr
    public Expr Expr() throws PLCException {
        currToken = tokens.get(ind);
        Expr e = null;
        if (ConditionalExprSet.contains(currToken.getKind())) {
            ConditionalExpr();
        } else if (LogicalOrExprSet.contains(currToken.getKind())) {
            LogicalOrExpr();
        } else {
            throw new SyntaxException("expected conditional or logical token in Expr");
        }
        return e;
    }

    public Expr ConditionalExpr() throws PLCException {
        currToken = tokens.get(ind);
        Expr e = null;
        if (currToken.getKind() == IToken.Kind.KW_IF) {
            match(IToken.Kind.KW_IF);
            match(IToken.Kind.LPAREN);
            e = Expr();
            match(IToken.Kind.RPAREN);
            e = Expr();
            match(IToken.Kind.KW_ELSE);
            e = Expr();
            match(IToken.Kind.KW_FI);
        } else {
            throw new SyntaxException("expected if in ConditionalExpr");
        }
        return e;
    }

    public Expr LogicalOrExpr() throws PLCException {
        IToken firstToken = tokens.get(ind);
        Expr left = null;
        Expr right = null;
        left = LogicalAndExpr();
        if (LogicalOrExprSet.contains(currToken.getKind())) {
            while (currToken.getKind() == IToken.Kind.OR) {
                IToken op = tokens.get(ind);
                match(IToken.Kind.OR);
                right = LogicalAndExpr();
                left = new BinaryExpr(firstToken, left, op, right);
            }
        } else {
            throw new SyntaxException("expected comparisonexpr in LogicalAndExpr");
        }
        return left;
    }

    public Expr LogicalAndExpr() throws PLCException {
        IToken firstToken = tokens.get(ind);
        Expr left = null;
        Expr right = null;
        left = ComparisonExpr();
        if (LogicalAndExprSet.contains(currToken.getKind())) {
            ComparisonExpr();
            while (IToken.Kind.AND == currToken.getKind()) { // x AND y, currToken.getKind returns AND,
                IToken op = tokens.get(ind);
                match(IToken.Kind.AND);
                right = ComparisonExpr();
                left = new BinaryExpr(firstToken, left, op, right);
            }
        } else {
            throw new SyntaxException("expected comparisonexpr in LogicalAndExpr");
        }
        return left;
    }

    public Expr ComparisonExpr() throws PLCException {
        IToken firstToken = tokens.get(ind);
        Expr left = null;
        Expr right = null;
        left = AdditiveExpr();
        while ((currToken.getKind() == IToken.Kind.LT) || (currToken.getKind() == IToken.Kind.GT) ||
                (currToken.getKind() == IToken.Kind.EQUALS) || (currToken.getKind() == IToken.Kind.NOT_EQUALS) ||
                (currToken.getKind() == IToken.Kind.LE) || (currToken.getKind() == IToken.Kind.GE)) {
            IToken op = tokens.get(ind);
            if ((currToken.getKind() == IToken.Kind.LT)) {
                match(IToken.Kind.LT);
            } else if ((currToken.getKind() == IToken.Kind.GT)) {
                match(IToken.Kind.GT);
            } else if ((currToken.getKind() == IToken.Kind.EQUALS)) {
                match(IToken.Kind.EQUALS);
            } else if ((currToken.getKind() == IToken.Kind.NOT_EQUALS)) {
                match(IToken.Kind.NOT_EQUALS);
            } else if ((currToken.getKind() == IToken.Kind.LE)) {
                match(IToken.Kind.LE);
            } else if ((currToken.getKind() == IToken.Kind.GE)) {
                match(IToken.Kind.GE);
            } else {
                throw new SyntaxException("expected addition operator in AdditiveExpr");
            }
            right = AdditiveExpr();
            left = new BinaryExpr(firstToken, left, op, right);

        }
        return left;
    }

    public Expr AdditiveExpr() throws PLCException {
        IToken firstToken = tokens.get(ind);
        Expr left = null;
        Expr right = null;
        left = MultiplicativeExpr();
        while (IToken.Kind.PLUS == currToken.getKind() || IToken.Kind.MINUS == currToken.getKind()) {
            IToken op = tokens.get(ind);
            if (currToken.getKind() == IToken.Kind.PLUS) {
                match(IToken.Kind.PLUS);
            } else if (currToken.getKind() == IToken.Kind.MINUS) {
                match(IToken.Kind.MINUS);
            } else {
                throw new SyntaxException("expected addition operator in AdditiveExpr");
            }
            right = MultiplicativeExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;

    }

    public Expr MultiplicativeExpr() throws PLCException {
        IToken firstToken = tokens.get(ind);
        Expr left = null;
        Expr right = null;
        left = UnaryExpr();
        while (IToken.Kind.TIMES == currToken.getKind() || IToken.Kind.DIV == currToken.getKind()
                || IToken.Kind.MOD == currToken.getKind()) {
            IToken op = tokens.get(ind);
            if (currToken.getKind() == IToken.Kind.TIMES) {
                match(IToken.Kind.TIMES);
            } else if (currToken.getKind() == IToken.Kind.DIV) {
                match(IToken.Kind.DIV);
            } else if (currToken.getKind() == IToken.Kind.MOD) {
                match(IToken.Kind.MOD);
            } else {
                throw new SyntaxException("expected multiplication operator in MultiplicativeExpr");
            }
            right = UnaryExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    public Expr UnaryExpr() throws PLCException {
        IToken firstToken = tokens.get(ind);
        Expr e = null;
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
            e = UnaryExpr();
        } else
            e = UnaryExprPostfix();
        return e;
    }

    public Expr UnaryExprPostfix() throws PLCException {
        IToken firstToken = tokens.get(ind);
        Expr e = null;
        PrimaryExpr();
        if (PixelSelectorSet.contains(currToken.getKind())) {
            PixelSelector();
        }
        return e;
    }

    public Expr PrimaryExpr() throws PLCException {
        currToken = tokens.get(ind);
        Expr e = null;
        if (PrimaryExprSet.contains(currToken.getKind())) {
            switch (currToken.getKind()) {
                case BOOLEAN_LIT -> {
                    e = new BooleanLitExpr(currToken);
                    match(IToken.Kind.BOOLEAN_LIT);
                }
                case STRING_LIT -> {
                    e = new StringLitExpr(currToken);
                    match(IToken.Kind.STRING_LIT);
                }
                case INT_LIT -> {
                    e = new IntLitExpr(currToken);
                    match(IToken.Kind.INT_LIT);
                }
                case FLOAT_LIT -> {
                    e = new FloatLitExpr(currToken);
                    match(IToken.Kind.FLOAT_LIT);
                }
                case IDENT -> {
                    e = new IdentExpr(currToken);
                    match(IToken.Kind.IDENT);
                }
                case LPAREN -> {
                    match(IToken.Kind.LPAREN);
                    e = Expr();
                    match(IToken.Kind.LPAREN);
                }
                default -> throw new SyntaxException("expected PrimaryExpr in PrimaryExpr");
            }
        } else {
            throw new SyntaxException("expected primaryexpr in PrimaryExpr");
        }
        return e;
    }

    public Expr PixelSelector() throws PLCException {
        IToken firstToken = tokens.get(ind);
        Expr e = null;
        if (currToken.getKind() == IToken.Kind.LSQUARE) {
            match(IToken.Kind.LSQUARE);
            e = Expr();
            match(IToken.Kind.COMMA);
            e = Expr();
            match(IToken.Kind.RPAREN);
        } else
            throw new SyntaxException("Expected LSQUARE in PixelSelector");
        return e;
    }

    public ASTNode parse() throws PLCException {
        while (true) {
            currToken = (Token) lexer.next();
            return new IdentExpr(nextToken);
        }
        // return new IdentExpr(new Token(0, 0, "", IToken.Kind.IDENT, 0));
    }

    public Parser(String input) {
        ArrayList<Token> tokens;
        lexer = getLexer(input);
        // predict sets for the given CFG
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