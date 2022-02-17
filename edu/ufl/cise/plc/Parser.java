package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

import java.util.HashSet;

import static edu.ufl.cise.plc.CompilerComponentFactory.getLexer;

public class Parser implements IParser {
    ILexer lexer;
    Token currToken;
    Token nextToken;
    HashSet<IToken.Kind> ExprSet = new HashSet();
    HashSet<IToken.Kind> ConditionalExprSet = new HashSet();
    HashSet<IToken.Kind> LogicalOrExprSet = new HashSet();
    HashSet<IToken.Kind> LogicalAndExprSet = new HashSet();
    HashSet<IToken.Kind> ComparisonExprSet = new HashSet();
    HashSet<IToken.Kind> AdditiveExprSet = new HashSet();
    HashSet<IToken.Kind> MultiplicativeExprSet = new HashSet();
    HashSet<IToken.Kind> UnaryExprSet = new HashSet();
    HashSet<IToken.Kind> UnaryExprPostfixSet = new HashSet();
    HashSet<IToken.Kind> PrimaryExprSet = new HashSet();
    HashSet<IToken.Kind> PixelSelectorSet = new HashSet();

    void consume() throws PLCException {
        currToken = (Token) lexer.next();
    }

    void match(IToken.Kind kind) throws PLCException {
        if (kind == currToken.getKind()) {
            currToken = (Token) lexer.next();
        } else {
            throw new SyntaxException("expected currToken " + currToken.getKind() + " to match " + kind);
        }
    }

    Expr ExprFunc() throws PLCException {
        // Expr newToken;
        if (ConditionalExprSet.contains(currToken.getKind())) {
            return ConditionalExprFunc();
        } else if (LogicalOrExprSet.contains(currToken.getKind())) {
            return LogicalOrExprFunc();
        } else {
            throw new SyntaxException("expected conditional or logical token in Expr");
        }
    }

    ConditionalExpr ConditionalExprFunc() throws PLCException {
        IToken firstToken = currToken;
        Expr condition;
        Expr trueCase;
        Expr falseCase;
        lexer.peek();
        if (currToken.getKind() == IToken.Kind.KW_IF) {
            match(IToken.Kind.KW_IF);
            match(IToken.Kind.LPAREN);
            condition = ExprFunc();
            match(IToken.Kind.RPAREN);
            trueCase = ExprFunc();
            match(IToken.Kind.KW_ELSE);
            falseCase = ExprFunc();
            match(IToken.Kind.KW_FI);
        } else {
            throw new SyntaxException("expected kw if in ConditionalExpr");
        }
        return new ConditionalExpr(firstToken, condition, trueCase, falseCase);
    }

    Expr LogicalOrExprFunc() throws PLCException {
        IToken firstToken = currToken;
        Expr leftExpr;
        Expr rightExpr;
        lexer.peek();
        if (LogicalOrExprSet.contains(currToken.getKind())) {
            leftExpr = LogicalAndExprFunc();
            while (currToken.getKind() == IToken.Kind.OR) {
                IToken operator = currToken;
                match(IToken.Kind.OR);
                rightExpr = LogicalAndExprFunc();
                leftExpr = new BinaryExpr(firstToken, leftExpr, operator, rightExpr);
            }
            return leftExpr;

        } else {
            throw new SyntaxException("expected logical and token in LogicalOrExpr");
        }
    }

    Expr LogicalAndExprFunc() throws PLCException {
        IToken firstToken = currToken;
        Expr leftExpr;
        lexer.peek();
        if (LogicalAndExprSet.contains(currToken.getKind())) {
            leftExpr = ComparisonExprFunc();
            while (IToken.Kind.AND == currToken.getKind()) { // x AND y, currToken.getKind returns AND,
                IToken operator = currToken;
                match(IToken.Kind.AND);
                Expr rightExpr = ComparisonExprFunc();
                leftExpr = new BinaryExpr(firstToken, leftExpr, operator, rightExpr);
            }
            return leftExpr;

        } else {
            throw new SyntaxException("expected comparisonexpr in LogicalAndExpr");
        }
    }

    Expr ComparisonExprFunc() throws PLCException {
        IToken firstToken = currToken;
        Expr right = null;
        Expr left = null;
        left = AdditiveExprFunc();
        while ((currToken.getKind() == IToken.Kind.LARROW) || (currToken.getKind() == IToken.Kind.RARROW) ||
                (currToken.getKind() == IToken.Kind.EQUALS) || (currToken.getKind() == IToken.Kind.NOT_EQUALS) ||
                (currToken.getKind() == IToken.Kind.LE) || (currToken.getKind() == IToken.Kind.GE) ||
                (currToken.getKind() == IToken.Kind.LT) || (currToken.getKind() == IToken.Kind.GT)) {
            IToken op = currToken;
            consume();
            right = AdditiveExprFunc();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    Expr AdditiveExprFunc() throws PLCException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        left = MultiplicativeExprFunc();
        while (IToken.Kind.PLUS == currToken.getKind() || IToken.Kind.MINUS == currToken.getKind()) {
            IToken op = currToken;
            consume();
            right = MultiplicativeExprFunc();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    Expr MultiplicativeExprFunc() throws PLCException {
        IToken firstToken = currToken;
        Expr left = null;
        Expr right = null;
        left = UnaryExprFunc();
        while (IToken.Kind.TIMES == currToken.getKind() || IToken.Kind.DIV == currToken.getKind()
                || IToken.Kind.MOD == currToken.getKind()) {
            IToken operator = currToken;
            consume();
            right = UnaryExprFunc();
            left = new BinaryExpr(firstToken, left, operator, right);
        }
        return left;
    }

    Expr UnaryExprFunc() throws PLCException {
        IToken firstToken = currToken;
        Expr e = null;
        if ((currToken.getKind() == IToken.Kind.BANG) || (currToken.getKind() == IToken.Kind.MINUS) ||
                (currToken.getKind() == IToken.Kind.COLOR_OP) || (currToken.getKind() == IToken.Kind.IMAGE_OP)) {
            IToken op = currToken;
            consume();
            e = new UnaryExpr(firstToken, op, UnaryExprFunc());
            /*
             * if(currToken.getKind() == IToken.Kind.BANG){
             * match(IToken.Kind.BANG);
             * }
             * else if(currToken.getKind() == IToken.Kind.MINUS){
             * match(IToken.Kind.MINUS);
             * }
             * else if(currToken.getKind() == IToken.Kind.COLOR_OP){
             * match(IToken.Kind.COLOR_OP);
             * }
             * else if(currToken.getKind() == IToken.Kind.IMAGE_OP){
             * match(IToken.Kind.IMAGE_OP);
             * }
             * else{
             * throw new SyntaxException("expected unary operator in UnaryExpr");
             * }
             */
        } else
            e = UnaryExprPostfixFunc();
        return e;
    }

    Expr UnaryExprPostfixFunc() throws PLCException {
        IToken firstToken = currToken;
        Expr left = PrimaryExprFunc();
        if (PixelSelectorSet.contains(currToken.getKind())) {
            PixelSelector right = PixelSelectorFunc();
            return new UnaryExprPostfix(firstToken, left, right);
        }
        return left;

    }

    Expr PrimaryExprFunc() throws PLCException {
        IToken firstToken = currToken;
        Expr e = null;
        lexer.peek();
        if (PrimaryExprSet.contains(currToken.getKind())) {
            switch (currToken.getKind()) {
                case BOOLEAN_LIT -> {
                    e = new BooleanLitExpr(firstToken);
                    consume();
                }
                case STRING_LIT -> {
                    e = new StringLitExpr(firstToken);
                    consume();
                }
                case INT_LIT -> {
                    e = new IntLitExpr(firstToken);
                    consume();
                }
                case FLOAT_LIT -> {
                    e = new FloatLitExpr(firstToken);
                    consume();
                }
                case IDENT -> {
                    e = new IdentExpr(firstToken);
                    consume();
                }
                case LPAREN -> {
                    consume();
                    e = ExprFunc();
                    match(IToken.Kind.RPAREN);
                }
                default -> throw new SyntaxException("expected PrimaryExpr in PrimaryExpr");
            }
            return e;
        } else {
            throw new SyntaxException("expected primaryexpr in PrimaryExpr");
        }
    }

    PixelSelector PixelSelectorFunc() throws PLCException {
        IToken firstToken = currToken;
        Expr x = null;
        Expr y = null;
        PixelSelector e = null;
        lexer.peek();
        if (currToken.getKind() == IToken.Kind.LSQUARE) {
            consume();
            x = ExprFunc();
            match(IToken.Kind.COMMA);
            y = ExprFunc();
            match(IToken.Kind.RSQUARE);
            e = new PixelSelector(firstToken, x, y);
        } else
            throw new SyntaxException("Expected LSQUARE in PixelSelector");
        return e;
    }

    public ASTNode parse() throws PLCException {
        while (true) {
            consume();
            return ExprFunc();
        }
        // return new IdentExpr(new Token(0, 0, "", IToken.Kind.IDENT, 0));
    }

    public Parser(String input) {
        lexer = getLexer(input);

        // predict sets for all the functions
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