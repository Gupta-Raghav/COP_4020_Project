package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

import java.util.Locale;

import java.awt.image.BufferedImage;
import java.io.File;

import edu.ufl.cise.plc.runtime.FileURLIO;

public class CodeGenVisitor implements ASTVisitor {

    private String packageName;

    public CodeGenVisitor(String packageName) {
        this.packageName = packageName;
    }

    public static void main(String[] args) {

    }

    public String boxed(Types.Type type) {
        if (type == Types.Type.INT)
            return "Integer";
        if (type == Types.Type.FLOAT)
            return "Float";
        if (type == Types.Type.BOOLEAN)
            return "Boolean";
        if (type == Types.Type.STRING)
            return "String";
        else
            return null;
    }

    public String lowerCaseString(Types.Type type) {
        if (type == Types.Type.IMAGE)
            return "BufferedImage";
        if (type == Types.Type.BOOLEAN)
            return "boolean";
        if (type == Types.Type.COLOR)
            return "ColorTuple";
        if (type == Types.Type.INT)
            return "int";
        if (type == Types.Type.VOID)
            return "void";
        if (type == Types.Type.STRING)
            return "String";
        else
            return type.toString().toLowerCase();
    }

    public static String opToOpText(String op) {
        return switch (op) {
            case "+" -> "PLUS";
            case "-" -> "MINUS";
            case "*" -> "TIMES";
            case "/" -> "DIV";
            case "%" -> "MOD";

            default -> throw new IllegalArgumentException("Unexpected type value: " + op);
        };
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        sb.append(booleanLitExpr.getValue());
        return sb;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        sb.append("\"\"\"");
        sb.append("\n");
        sb.append(stringLitExpr.getValue());
        sb.append("\"\"\"");
        return sb;
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        Types.Type type = (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Types.Type.INT)
                ? intLitExpr.getCoerceTo()
                : intLitExpr.getType();
        if (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Types.Type.INT)
            sb.append("(").append(lowerCaseString(type)).append(") ");
        sb.append(intLitExpr.getValue());
        return sb;
    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        Types.Type type = (floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != Types.Type.FLOAT)
                ? floatLitExpr.getCoerceTo()
                : floatLitExpr.getType();
        if (floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != Types.Type.FLOAT)
            sb.append("(").append(lowerCaseString(type)).append(") ");
        sb.append(floatLitExpr.getValue());
        sb.append("f");
        return sb;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        String col = colorConstExpr.getText();

        sb.append("ColorTuple.unpack(" + "Color." + col + ".getRGB");
        return sb;
        // throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        sb.append("(").append((boxed(consoleExpr.getCoerceTo()))).append(")").append(" "); // (Integer)
        sb.append("ConsoleIO.readValueFromConsole"); // ConsoleIO.readValueFromConsole
        sb.append("(").append("\"").append(lowerCaseString(consoleExpr.getCoerceTo()).toUpperCase()).append("\"")
                .append(",").append(" "); // (“INT”,
        sb.append("\"").append("Enter ").append(boxed(consoleExpr.getCoerceTo()).toLowerCase()); // integer:
        sb.append(":").append("\"").append(")").append(";"); // ”);

        return sb;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;

        String red = colorExpr.getRed().getText();
        String blue = colorExpr.getBlue().getText();
        String green = colorExpr.getGreen().getText();

        sb.append("new ColorTuple(" + red + ", " + green + ", " + blue + ")");

        return sb;
        // throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        sb.append(" (");
        sb.append(unaryExpression.getOp().getText());
        unaryExpression.getExpr().visit(this, sb);
        sb.append(") ");
        return sb;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        Expr leftExpr = binaryExpr.getLeft();
        Expr rightExpr = binaryExpr.getRight();

        Types.Type type = binaryExpr.getType();
        Types.Type leftType = leftExpr.getCoerceTo() == null ? leftExpr.getType() : leftExpr.getCoerceTo();
        Types.Type rightType = rightExpr.getCoerceTo() == null ? rightExpr.getType() : rightExpr.getCoerceTo();

        IToken op = binaryExpr.getOp();

        if (type == Types.Type.IMAGE) {
            sb.append("(");
            sb.append("ImageOps.binaryImageImageOp(ImageOps.OP." + opToOpText(op.getText()) + ", ");
            leftExpr.visit(this, sb);
            sb.append(",").append(" ");
            rightExpr.visit(this, sb);
            sb.append(")");
            sb.append(")");
            // throw new UnsupportedOperationException("Not implemented yet");

        } else if (type == Types.Type.COLOR) {
            sb.append("(");
            sb.append("ImageOps.binaryTupleOp(ImageOps.OP." + opToOpText(op.getText()) + ", ");
            leftExpr.visit(this, sb);
            sb.append(",").append(" ");
            rightExpr.visit(this, sb);
            sb.append(")");
            sb.append(")");
        } else if ((binaryExpr.getLeft().getType() == Types.Type.IMAGE
                && binaryExpr.getRight().getType() == Types.Type.COLOR)
                && (binaryExpr.getRight().getType() == Types.Type.IMAGE
                        && binaryExpr.getLeft().getType() == Types.Type.COLOR)) {
            // What is an image operation? How do I apply color to it?? See binaryExpr
            // description. Does it mean pixels?
        } else if ((binaryExpr.getLeft().getType() == Types.Type.IMAGE
                && binaryExpr.getRight().getType() == Types.Type.INT)
                && (binaryExpr.getRight().getType() == Types.Type.IMAGE
                        && binaryExpr.getLeft().getType() == Types.Type.INT)) {
            // Use the colorTuple constructor with 3 params
        } else {
            sb.append("(");
            binaryExpr.getLeft().visit(this, sb);
            sb.append(binaryExpr.getOp().getText());
            binaryExpr.getRight().visit(this, sb);
            sb.append(")");
        }

        return sb;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        Types.Type type = identExpr.getCoerceTo() != null ? identExpr.getCoerceTo() : identExpr.getType();
        // add cast type if applicable
        if (identExpr.getCoerceTo() != null && identExpr.getCoerceTo() != type) {
            sb.append("(").append(lowerCaseString(identExpr.getCoerceTo())).append(")");
        }
        sb.append(identExpr.getText());
        return sb;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        sb.append(" (");
        conditionalExpr.getCondition().visit(this, sb);
        sb.append("?");
        conditionalExpr.getTrueCase().visit(this, sb);
        sb.append(":");
        conditionalExpr.getFalseCase().visit(this, sb);
        sb.append(") ");
        return sb;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        sb.append(" (");
        dimension.getWidth().visit(this, sb);
        sb.append("str");
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        sb.append(assignmentStatement.getName());
        sb.append("=");
        if (assignmentStatement.getTargetDec().getType() != assignmentStatement.getExpr().getType()) {
            sb.append(" (");
            sb.append(lowerCaseString(assignmentStatement.getTargetDec().getType()));
            sb.append(") ");
        }
        if (assignmentStatement.getTargetDec().getType() == Types.Type.IMAGE &&
                assignmentStatement.getTargetDec().getDim() != null) {
            PixelSelector selector = assignmentStatement.getSelector();

            if (selector != null) {
                String name = assignmentStatement.getName();
                String x = selector.getX().getText();
                String y = selector.getY().getText();
                sb.append("for(int " + x + "= 0;" + x + " < " + name + ".getWidth();" + x + "++)").append("\n")
                        .append("\t").append("\t").append(
                                "for(int " + y + "= 0;" + y + " < " + name + ".getWidth();" + y + "++)")
                        .append("\n").append("\t").append("\t").append("\t").append(
                                "ImageOps.setColor(" + name + "," + x + "," + y + ", ");

                if (assignmentStatement.getExpr().getType() == Types.Type.COLOR) {
                    sb.append("ImageOps.setColor(" + name + "," + x + "," + y + ", ");
                    assignmentStatement.getExpr().visit(this, arg);
                    sb.append(")").append(";");
                } else if (assignmentStatement.getExpr().getType() == Types.Type.INT) {
                    sb.append("ImageOps.setColor(" + name + "," + x + "," + y
                            + ", ColorTuple.unpack(ColorTuple.truncate(");
                    assignmentStatement.getExpr().visit(this, arg);
                    sb.append(")").append(")").append(")").append(";").append("\n");
                }
            } else {
                String name = assignmentStatement.getName();
                String x = "x";
                String y = "y";
                String val = assignmentStatement.getExpr().getText();

                sb.append("for(int " + x + "= 0;" + x + " < " + name + ".getWidth();" + x + "++)").append("\n")
                        .append("\t").append("\t").append(
                                "for(int " + y + "= 0;" + y + " < " + name + ".getWidth();" + y + "++)")
                        .append("\n").append("\t").append("\t").append("\t");
                if (assignmentStatement.getExpr().getType() == Types.Type.COLOR) {
                    sb.append("ImageOps.setColor(" + name + "," + x + "," + y + ", Color." + val + ".getRGB()");
                    sb.append(")");
                } else {
                    sb.append("ImageOps.setColor(" + name + "," + x + "," + y + ", new ColorTuple(" + val + ", " + val
                            + ", " + val);
                    sb.append(")").append(")");
                }

                sb.append(";");
                sb.append(";").append("\n");
            }
        } else if (assignmentStatement.getTargetDec().getType() == Types.Type.IMAGE &&
                assignmentStatement.getTargetDec().getDim() == null) {
            PixelSelector selector = assignmentStatement.getSelector();

            if (selector != null) {
                String name = assignmentStatement.getName();
                String x = selector.getX().getText();
                String y = selector.getY().getText();
                sb.append("for(int " + x + "= 0;" + x + " < " + name + ".getWidth();" + x + "++)").append("\n")
                        .append("\t").append("\t").append(
                                "for(int " + y + "= 0;" + y + " < " + name + ".getWidth();" + y + "++)");
            }
        } else {
            sb.append(assignmentStatement.getName()).append("=");

            Expr expr = assignmentStatement.getExpr();
            expr.visit(this, sb);

            sb.append(";");
        }

        return sb;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        sb.append("ConsoleIO.console.println(");
        sb.append(writeStatement.getSource().getText()).append(")");
        return sb;
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        sb.append(readStatement.getName()).append("=");
        // if reading from console then append (object version of type)
        sb.append(" (").append(boxed(readStatement.getTargetDec().getType())).append(") ");
        readStatement.getSource().visit(this, sb);
        // if reading from console
        Types.Type targetType = readStatement.getTargetDec().getType();
        sb.append(targetType).append("\",");
        sb.append("\"Enter ").append(lowerCaseString(targetType)).append(":\")");
        return sb;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        StringBuilder sb = new StringBuilder();
        String typeLowerCase = lowerCaseString(program.getReturnType());
        sb.append("package ").append(packageName).append(";\n");
        sb.append("import java.awt.image.BufferedImage;\n");
        sb.append("import java.awt.Color;\n");
        sb.append("import edu.ufl.cise.plc.runtime.*; \n");
        sb.append("public class ").append(program.getName()).append("{\n");
        sb.append("public static ").append(typeLowerCase).append(" apply(");
        // append parameters
        for (int i = 0; i < program.getParams().size(); i++) {
            program.getParams().get(i).visit(this, sb);
            if (i != program.getParams().size() - 1)
                sb.append(", ");
        }
        sb.append("){\n");
        // append declarations and statements
        for (int i = 0; i < program.getDecsAndStatements().size(); i++) {
            sb.append("\t");
            program.getDecsAndStatements().get(i).visit(this, sb);
            sb.append(";");
            if (i != program.getDecsAndStatements().size() - 1)
                sb.append("\n");
        }
        sb.append("\n\t}\n}");

        return sb.toString();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        String typeLowerCase = lowerCaseString(nameDef.getType());
        sb.append(typeLowerCase).append(" ").append(nameDef.getName());
        return sb;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        // if getDim != null append readImage
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        Expr expr = returnStatement.getExpr();
        sb.append("return ");
        expr.visit(this, sb);
        return sb;
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        StringBuilder sb = (StringBuilder) arg;
        declaration.getNameDef().visit(this, sb);
        if (declaration.getExpr() != null) {
            sb.append("=");
            if (declaration.getType() != declaration.getExpr().getType()) {
                sb.append(" (");
                sb.append(lowerCaseString(declaration.getType()));
                sb.append(") ");
            }
            declaration.getExpr().visit(this, sb);
        }
        if (declaration.getType() == Types.Type.IMAGE) {
            if (declaration.getDim() != null && declaration.getExpr() != null) {
                sb.append("FileURLIO.readImage(");
                declaration.getExpr().visit(this, sb);
            } else if (declaration.getDim() != null && declaration.getExpr() == null) {
            } else if (declaration.getDim() == null && declaration.getExpr() != null) {

            } else {

            }
        }
        if (declaration.getType() == Types.Type.COLOR) {
            declaration.getExpr().visit(this, sb); // should go to visitColorExpr and make object ColorTuple
        }
        return sb;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}