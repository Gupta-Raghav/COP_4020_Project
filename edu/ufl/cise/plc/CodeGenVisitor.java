package edu.ufl.cise.plc;

import java.util.*;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;
import edu.ufl.cise.plc.runtime.ConsoleIO;

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class CodeGenVisitor implements ASTVisitor {
    String packageName = "";
    Set<String> impts = new HashSet<String>();
    String file = "";

    public CodeGenVisitor(String packageName) {
        this.packageName = packageName;

    }

    public Object genTypeConversion(Type coerce, Object arg) {
        String type = "";
        if (coerce == INT) {
            type = "int";
        } else if (coerce == FLOAT) {
            type = "float";
        } else if (coerce == STRING) {
            type = "String";
        } else if (coerce == BOOLEAN) {
            type = "boolean";
        }

        else {
            return arg;
        }

        arg = "(" + type + ")" + arg;
        return arg;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        String kind = "";

        if (booleanLitExpr.getValue() == true) {
            kind = "true";
        } else if (booleanLitExpr.getValue() == false) {
            kind = "false";
        } else {
            throw new IllegalArgumentException("Neither true or false.");
        }

        arg += kind;

        return arg;

    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        String argTemp = "\"\"\"\n";
        argTemp = argTemp + stringLitExpr.getValue();
        argTemp += "\"\"\"";
        arg += (String) argTemp;

        return arg;

    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        Object argTemp = String.valueOf(intLitExpr.getValue());
        Type type = intLitExpr.getCoerceTo() != null ? intLitExpr.getCoerceTo() : intLitExpr.getType();

        if (intLitExpr.getType() != type) {
            argTemp = genTypeConversion(type, argTemp);
        }
        arg += (String) argTemp;
        return arg;
    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        Object argTemp = (Float.toString(floatLitExpr.getValue()));
        argTemp += "f";

        Type type = floatLitExpr.getCoerceTo() != null ? floatLitExpr.getCoerceTo() : floatLitExpr.getType();

        if (floatLitExpr.getType() != type) {
            argTemp = genTypeConversion(type, argTemp);
        }
        arg += (String) argTemp;
        return arg;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        if (identExpr.getCoerceTo() == IMAGE) {
            impts.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
            arg += "ImageOps.clone(";
        } else if (identExpr.getCoerceTo() == INT && identExpr.getType() == COLOR) {
            arg += (identExpr.getText());
            arg += ".pack()";
            return arg;
        }

        else if (identExpr.getCoerceTo() == COLOR && identExpr.getType() == INT) {
            arg += "new ColorTuple(";
            arg += identExpr.getText();
            arg += ")";
            return arg;
        }
        arg += (identExpr.getText());

        return arg;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
        Expr condition = conditionalExpr.getCondition();
        Expr trueCase = conditionalExpr.getTrueCase();
        Expr falseCase = conditionalExpr.getFalseCase();

        arg += "(";
        arg += "(";
        arg = condition.visit(this, arg);
        arg += ")";
        arg += (" ? ");
        arg = trueCase.visit(this, arg);
        arg += (" : ");
        arg = falseCase.visit(this, arg);
        arg += ")";

        return arg;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        arg = "";
        arg = arg + "public class " + program.getName() + "{" + "\n";
        arg = arg + "    " + "public static ";

        String Type = "";

        if (program.getReturnType() == VOID) {
            Type = "void";
        } else if (program.getReturnType() == INT) {
            Type = "int";
        } else if (program.getReturnType() == FLOAT) {
            Type = "float";
        } else if (program.getReturnType() == BOOLEAN) {
            Type = "boolean";
        } else if (program.getReturnType() == STRING) {
            Type = "String";
        } else if (program.getReturnType() == COLOR) {
            Type = "ColorTuple";
        } else if (program.getReturnType() == IMAGE) {
            Type = "BufferedImage";
        } else {
            throw new IllegalArgumentException("Compiler bug Unexpected value: " + program.getReturnType());
        }

        arg = arg + Type + " apply (";
        for (int i = 0; i < program.getParams().size(); i++) {
            NameDef nameDef = program.getParams().get(i);
            arg = nameDef.visit(this, arg);
            if (i != program.getParams().size() - 1) {
                arg = arg + ", ";

            }

        }

        arg = arg + ")" + "{" + "\n" + "        ";

        for (int i = 0; i < program.getDecsAndStatements().size(); i++) {
            ASTNode stat = program.getDecsAndStatements().get(i);
            arg = (String) stat.visit(this, arg);

            arg += "\n";

            if (i != program.getDecsAndStatements().size() - 1) {
                arg += ("        ");
            }

        }

        arg = arg + "    " + "}" + "\n" + "}";

        Object arg3 = "package " + packageName + ";" + "\n";

        Iterator<String> itr = impts.iterator();

        while (itr.hasNext()) {
            arg3 += itr.next();
        }

        arg = arg3 + "\n" + arg;

        return arg;

    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
        String Type = "";
        if (nameDef.getType() == INT) {
            Type = "int";
        } else if (nameDef.getType() == FLOAT) {
            Type = "float";
        } else if (nameDef.getType() == BOOLEAN) {
            Type = "boolean";
        } else if (nameDef.getType() == STRING) {
            Type = "String";
        } else if (nameDef.getType() == IMAGE) {
            Type = "BufferedImage";

        } else if (nameDef.getType() == COLOR) {
            Type = "ColorTuple";
        } else {
            throw new IllegalArgumentException("Compiler bug Unexpected value: " + nameDef.getType());
        }
        arg += (Type);
        arg += (" ");
        arg += (nameDef.getName());
        return arg;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        Expr expr = returnStatement.getExpr();

        arg += ("return ");
        arg = expr.visit(this, arg);
        arg = arg + ";\n";
        return arg;
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        Object argTemp = "";
        NameDef nameDef = declaration.getNameDef();

        argTemp = nameDef.visit(this, argTemp);

        String Type = "";

        if (declaration.getOp() != null) {
            if (declaration.getOp().getKind() == Kind.ASSIGN) {
                if (declaration.getNameDef().getType() == IMAGE && declaration.getExpr().getType() == IMAGE) {
                    if (declaration.getDim() != null) {
                        impts.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
                        argTemp += " = ImageOps.resize(";
                        argTemp = declaration.getExpr().visit(this, argTemp);
                        argTemp += ", ";
                        argTemp = declaration.getDim().visit(this, argTemp);
                        argTemp += ");";
                        arg += (String) argTemp;
                        return arg;

                    }
                }
                argTemp += " = ";
                argTemp = declaration.getExpr().visit(this, argTemp);
                arg += (String) argTemp;
                arg += ";\n";
                return arg;
            }

            else if (declaration.getOp().getKind() == Kind.LARROW && declaration.getExpr().getType() == STRING
                    && declaration.getNameDef().getType() != IMAGE) {
                impts.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                argTemp += " = ";
                Type type = declaration.getNameDef().getType();

                if (type == STRING) {
                    argTemp += "(String) ";
                } else if (type == INT) {
                    argTemp += "(int) ";
                }

                else if (type == FLOAT) {
                    argTemp += "(float) ";
                }

                else if (type == COLOR) {
                    argTemp += "(ColorTuple)";
                }

                else if (type == IMAGE) {
                    argTemp += "(BufferedImage)";
                }

                else if (type == BOOLEAN) {
                    argTemp += "(boolean)";
                }

                argTemp += "FileURLIO.readValueFromFile(";

                argTemp += declaration.getExpr().getText();
                arg += (String) argTemp;
                arg += ");\n";
                return arg;
            }

        }

        if (nameDef.getType() == IMAGE) {
            impts.add("import java.awt.image.BufferedImage;\n");

            if (declaration.getExpr() != null) {

                impts.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
                if (nameDef.getDim() != null) {
                    argTemp += "= FileURLIO.readImage(";

                    argTemp = declaration.getExpr().visit(this, argTemp);
                    argTemp += ", ";
                    argTemp = declaration.getDim().visit(this, argTemp);
                    argTemp += ");\n";
                    arg += (String) argTemp;
                    return arg;
                } else {
                    argTemp += "= FileURLIO.readImage(";
                    // argTemp += " = ImageOps.";
                    argTemp = declaration.getExpr().visit(this, argTemp);
                    argTemp += ");\n";

                    arg += (String) argTemp;
                    return arg;
                }
            }

            else {

                if (nameDef.getDim() != null) {
                    argTemp += " = new BufferedImage(";
                    argTemp = declaration.getDim().visit(this, argTemp);
                    argTemp += ", BufferedImage.TYPE_INT_RGB);\n";
                    arg += (String) argTemp;
                    return arg;
                } else {

                    // Should've thrown an exception beforehand.
                }
            }
        }

        if (declaration.getOp() != null) {
            argTemp += " = ";

            if (nameDef.getType() != declaration.getExpr().getType() && nameDef.getType() != IMAGE) {

                Type coerce = nameDef.getType();

                if (coerce == INT) {
                    Type = "int";
                    if (declaration.getExpr().getType() == STRING) {
                        argTemp += "(" + Type + ") " + "FileURLIO.readValueFromFile(";
                        argTemp = declaration.getExpr().visit(this, argTemp);
                        argTemp += ");";
                        arg += (String) argTemp;
                        return arg;
                    }
                } else if (coerce == FLOAT) {
                    Type = "float";
                    if (declaration.getExpr().getType() == STRING) {

                        argTemp += "(" + Type + ") " + "FileURLIO.readValueFromFile(";
                        argTemp = declaration.getExpr().visit(this, argTemp);
                        argTemp += ");";
                        arg += (String) argTemp;
                        return arg;
                    }
                } else if (coerce == STRING) {
                    Type = "String";
                } else if (coerce == BOOLEAN) {
                    Type = "boolean";
                    if (declaration.getExpr().getType() == STRING) {
                        argTemp += "(" + Type + ") " + "FileURLIO.readValueFromFile(";
                        argTemp = declaration.getExpr().visit(this, argTemp);
                        argTemp += ");";
                        arg += (String) argTemp;
                        return arg;
                    }
                }

                else if (coerce == IMAGE) {
                    Type = "BufferedImage";

                }

                else if (coerce == COLOR) {
                    Type = "ColorTuple";
                    argTemp += "(" + Type + ") " + "FileURLIO.readValueFromFile(";
                    argTemp = declaration.getExpr().visit(this, argTemp);
                    argTemp += ");";
                    arg += (String) argTemp;
                    return arg;
                }

            }

            else if (nameDef.getType() == COLOR) {
                impts.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
            }

            if (Type != "") {
                argTemp += "(" + Type + ")";
            }
            Expr expr = declaration.getExpr();

            argTemp = expr.visit(this, argTemp);
        }

        argTemp = argTemp + ";\n";

        arg += (String) argTemp;
        return arg;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {

        Object argTemp = "";
        Expr LeftE = binaryExpr.getLeft();
        Expr rightE = binaryExpr.getRight();
        Type leftT = LeftE.getCoerceTo() != null ? LeftE.getCoerceTo() : LeftE.getType();
        Type rightT = rightE.getCoerceTo() != null ? rightE.getCoerceTo() : rightE.getType();
        Kind op = binaryExpr.getOp().getKind();

        if (op == Kind.EQUALS || op == Kind.NOT_EQUALS) {
            if (LeftE.getType() == STRING) {
                if (op == Kind.NOT_EQUALS) {
                    argTemp += "!";
                }
                argTemp += LeftE.getText();
                argTemp += ".equals(";
                argTemp += rightE.getText();
                argTemp += ")";
                arg += (String) argTemp;
                return arg;

            } else if (LeftE.getType() == COLOR) {
                Object arg3 = "";
                arg3 = LeftE.visit(this, arg3);
                Object arg4 = "";
                arg4 = rightE.visit(this, arg4);

                if (op == Kind.NOT_EQUALS) {
                    if (!arg3.equals(arg4)) {

                        arg += "true";

                    } else {
                        arg += "false";
                    }
                } else {
                    if (arg3.equals(arg4)) {
                        arg += "true";
                    } else {
                        arg += "false";
                    }
                }
                return arg;
            }

        }

        if (leftT == IMAGE || leftT == COLOR || leftT == COLORFLOAT
                || rightT == IMAGE || rightT == COLOR || rightT == COLORFLOAT) {

            impts.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");

            if (leftT == IMAGE && rightT == COLOR || leftT == COLOR && rightT == IMAGE) {

            }

            else if (leftT == COLOR && rightT == COLOR) {

                argTemp += "(ImageOps.binaryTupleOp(ImageOps.OP.valueOf(";
                if (op == Kind.DIV) {
                    argTemp += "\"DIV\"),";
                } else if (op == Kind.MINUS) {
                    argTemp += "\"MINUS\"), ";
                } else if (op == Kind.PLUS) {
                    argTemp += "\"PLUS\"), ";
                } else if (op == Kind.TIMES) {
                    argTemp += "\"TIMES\"), ";
                } else if (op == Kind.MOD) {
                    argTemp += "\"MOD\"), ";
                }

                argTemp = LeftE.visit(this, argTemp);
                argTemp += ", ";

                if (binaryExpr.getLeft().getType() == COLOR && binaryExpr.getRight().getType() == INT) {
                    argTemp += "new ColorTuple(";
                    argTemp = binaryExpr.getRight().visit(this, argTemp);
                    argTemp += ")))";
                    arg += (String) argTemp;
                    return arg;
                }
                argTemp = rightE.visit(this, argTemp);
                argTemp += "))";
            }

            else if (leftT == IMAGE && rightT == INT || leftT == INT && rightT == IMAGE) {

                argTemp += "(ImageOps.binaryImageScalarOp(ImageOps.OP.valueOf(";
                if (op == Kind.DIV) {
                    argTemp += "\"DIV\"),";
                } else if (op == Kind.MINUS) {
                    argTemp += "\"MINUS\"), ";
                } else if (op == Kind.PLUS) {
                    argTemp += "\"PLUS\"), ";
                } else if (op == Kind.TIMES) {
                    argTemp += "\"TIMES\"), ";
                } else if (op == Kind.MOD) {
                    argTemp += "\"MOD\"), ";
                }

                argTemp = LeftE.visit(this, argTemp);
                argTemp += ", ";

                if (binaryExpr.getLeft().getType() == COLOR && binaryExpr.getRight().getType() == INT) {
                    argTemp += "new ColorTuple(";
                    argTemp = binaryExpr.getRight().visit(this, argTemp);
                    argTemp += ")))";
                    arg += (String) argTemp;
                    return arg;
                }
                argTemp = rightE.visit(this, argTemp);
                argTemp += "))";

            }
        } else {

            argTemp = "(";
            argTemp = binaryExpr.getLeft().visit(this, argTemp);
            argTemp += (binaryExpr.getOp().getText());
            argTemp = binaryExpr.getRight().visit(this, argTemp);
            argTemp += ")";
            if (leftT != rightT) {
                argTemp = genTypeConversion(leftT, argTemp);

            }
        }

        arg += (String) argTemp;
        return arg;
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        if (consoleExpr.getType() == STRING) {
            impts.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
        } else {
            impts.add("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
        }

        String Type = "";
        String Type2 = "";

        if (consoleExpr.getCoerceTo() == INT) {
            Type = "Integer";
            Type2 = "INT";
        } else if (consoleExpr.getCoerceTo() == BOOLEAN) {
            Type = "boolean";
            Type2 = "BOOLEAN";
        } else if (consoleExpr.getCoerceTo() == FLOAT) {
            Type = "Float";
            Type2 = "FLOAT";
        } else if (consoleExpr.getCoerceTo() == STRING) {
            Type = "String";
            Type2 = "STRING";

        } else if (consoleExpr.getCoerceTo() == COLOR) {
            impts.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
            Type = "ColorTuple";
            Type2 = "COLOR";
        }

        else {
            throw new IllegalArgumentException("Compiler bug Unexpected value: " + consoleExpr.getCoerceTo());
        }

        arg = arg + "(" + Type + ")";
        if (consoleExpr.getType() == STRING) {

            arg += "FileURLIO.readValueFromFile(";

            arg = arg + consoleExpr.getText() + ")";
        } else {
            arg += "ConsoleIO.readValueFromConsole(";
            arg = arg + "\"" + Type2 + "\"" + ", " + "\"";
            arg = arg + "Enter " + Type + ":" + "\"";
            arg = arg + ")";
        }

        return arg;

    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        IToken op = unaryExpression.getOp();
        Expr expr = unaryExpression.getExpr();
        Object argTemp = op.getText();
        if (op.getKind() == Kind.COLOR_OP) {
            if ((expr.getType() == INT)) {

                if (argTemp.equals("getRed")) {
                    argTemp = "ColorTuple.getRed(";
                } else if (argTemp.equals("getGreen")) {
                    argTemp = "ColorTuple.getGreen(";
                } else if (argTemp.equals("getBlue")) {
                    argTemp = "ColorTuple.getBlue(";
                }

                argTemp = "(" + argTemp;
                argTemp += "(";
                argTemp = expr.visit(this, argTemp);
                argTemp += ")))";
                arg += (String) argTemp;
                return arg;
            }

            else if (expr.getType() == IMAGE) {
                impts.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");

                if (argTemp.equals("getRed")) {
                    argTemp = "ImageOps.extractRed(";
                } else if (argTemp.equals("getGreen")) {
                    argTemp = "ImageOps.extractGreen(";
                } else if (argTemp.equals("getBlue")) {
                    argTemp = "ImageOps.extractBlue(";
                }

                argTemp = expr.visit(this, argTemp);
                argTemp += ")";
                arg += (String) argTemp;
                return arg;

            }

        }

        else if (unaryExpression.getOp().getKind() == Kind.IMAGE_OP) {
            argTemp = unaryExpression.getExpr().getText();
            argTemp += ".";
            argTemp += unaryExpression.getOp().getText();
            argTemp += "()";
            arg += (String) argTemp;
            return arg;
        }

        argTemp = expr.visit(this, argTemp);
        argTemp = "(" + argTemp + ")";

        arg += (String) argTemp;

        return arg;
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        if (readStatement.getSource().getType().toString().equals("STRING")) {
            impts.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");

            arg += readStatement.getName();
            arg += " = ";

            Type _type = readStatement.getTargetDec().getType();

            String type = "";

            if (_type == INT) {
                type = "Integer";
            } else if (_type == STRING) {
                type = "String";
            } else if (_type == BOOLEAN) {
                type = "boolean";
            } else if (_type == FLOAT) {
                type = "Float";
            } else if (_type == COLOR) {
                type = "ColorTuple";
            } else if (_type == IMAGE) {
                type = "BufferedImage";
            }

            arg = arg + "(" + type + ")" + "\n";
            file = readStatement.getSource().getText();

            if (readStatement.getTargetDec().getType() == IMAGE) {
                if (readStatement.getTargetDec().getDim() != null) {

                    impts.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
                    arg += "ImageOps.resize(";
                    arg += "FileURLIO.readImage(";
                    arg = arg + file + ")";
                    arg += ", ";
                    arg = readStatement.getTargetDec().getDim().visit(this, arg);
                    arg += ");\n";
                    return arg;

                }

                if (readStatement.getTargetDec().getDim() != null) {

                    impts.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
                    arg += "ImageOps.resize(";
                    arg += "FileURLIO.readValueFromFile(";
                    arg = arg + file + ")";
                    arg += ", ";
                    arg = readStatement.getTargetDec().getDim().visit(this, arg);
                    arg += ");\n";
                    return arg;

                }
            }

            else {
                arg += "FileURLIO.readValueFromFile(";
            }
            arg = arg + file + ");";

        }

        else {
            impts.add("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");

            arg += (readStatement.getName());

            arg += (" = ");
            Expr expr = readStatement.getSource();
            arg = expr.visit(this, arg);
            arg = arg + ";" + "\n";
        }

        return arg;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        file = writeStatement.getDest().getText();

        if (writeStatement.getDest().getType() == STRING && writeStatement.getSource().getType() != IMAGE) {
            impts.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
            arg += "FileURLIO.writeValue(";
            Expr source = writeStatement.getSource();
            arg = source.visit(this, arg);
            arg = arg + ", " + "" + writeStatement.getDest().getText() + "" + ");" + "\n";

        } else if (writeStatement.getSource().getType() == IMAGE && writeStatement.getDest().getType() == CONSOLE) {

            impts.add("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
            arg += "ConsoleIO.displayImageOnScreen(";
            arg += writeStatement.getSource().getText();
            arg = arg + ");" + "\n";

        }

        else if (writeStatement.getSource().getType() == IMAGE && writeStatement.getDest().getType() == STRING) {
            impts.add("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
            arg += "FileURLIO.writeImage(";
            arg += writeStatement.getSource().getText();
            arg += ", ";
            arg += file;
            arg += ");\n";
            return arg;
        }

        else {
            impts.add("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
            arg += ("ConsoleIO.console.println(");
            Expr source = writeStatement.getSource();
            arg = source.visit(this, arg);
            arg = arg + ");" + "\n";

        }

        return arg;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        Object argTemp = "";
        Expr expr = assignmentStatement.getExpr();
        Type name = assignmentStatement.getTargetDec().getType();

        if (name == IMAGE && expr.getType() == IMAGE) {
            if (assignmentStatement.getTargetDec().getDim() != null) {
                argTemp = assignmentStatement.getName();
                argTemp += " = ";
                impts.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
                argTemp += "ImageOps.resize(";
                argTemp += expr.getText();
                argTemp += ", ";
                argTemp = assignmentStatement.getTargetDec().getDim().visit(this, argTemp);
                argTemp += ");\n";
                arg += (String) argTemp;
                return arg;
            } else {
                argTemp = assignmentStatement.getName();
                argTemp += " = ";
                argTemp = expr.visit(this, argTemp);
                argTemp += ";\n";
                arg += (String) argTemp;
                return arg;
            }
        }

        else if (name == IMAGE && expr.getType() == INT) {
            impts.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
            impts.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
            String image = assignmentStatement.getName();
            argTemp = argTemp + "for (int x = 0; x < " + image + ".getWidth(); x++)\n    ";
            argTemp = argTemp + "for (int y = 0; y < " + image + ".getHeight(); y++)\n        ";
            argTemp = argTemp + "ImageOps.setColor(" + image + ", x, y, ";
            argTemp += "new ColorTuple(";
            argTemp = expr.visit(this, argTemp);
            argTemp += "));\n";
            arg += (String) argTemp;
            return arg;
        }

        else if ((expr.getCoerceTo() == COLOR || (expr.getType() == COLOR && expr.getCoerceTo() == null))
                && !(name == COLOR && expr.getType() == COLOR)) {
            impts.add("import edu.ufl.cise.plc.runtime.ImageOps;\n");
            String image = assignmentStatement.getName();
            argTemp = argTemp + "for (int x = 0; x < " + image + ".getWidth(); x++)\n    ";
            argTemp = argTemp + "for (int y = 0; y < " + image + ".getHeight(); y++)\n        ";
            argTemp = argTemp + "ImageOps.setColor(" + image + ", x, y, ";
            argTemp = expr.visit(this, argTemp);
            argTemp += ");\n";
            arg += (String) argTemp;
            return arg;
        }

        else if (expr.getCoerceTo() == INT) {
            String image = assignmentStatement.getTargetDec().getText();
            argTemp += "ColorTuple X = new ColorTuple(truncate(";
            argTemp = expr.visit(this, argTemp);
            argTemp += "));\n";
            argTemp = argTemp + "for (int x = 0; x < " + image + ".getWidth(); x++)\n    ";
            argTemp = argTemp + "for (int y = 0; y < " + image + ".getHeight(); y++)\n        ";
            argTemp = argTemp + "ImageOps.setColor(" + image + ", x, y, X);\n";
            arg += (String) argTemp;
            return arg;
        }

        if (expr.getType() != expr.getCoerceTo()) {
            Type coerce = expr.getCoerceTo();
            String type = "";
            if (coerce == INT) {
                type = "int";
            } else if (coerce == FLOAT) {
                type = "float";
            } else if (coerce == STRING) {
                type = "String";
            } else if (coerce == BOOLEAN) {
                type = "boolean";

            }

            else if (coerce == IMAGE) {
                type = "BufferedImage";

            }

            else if (coerce == COLOR) {
                type = "ColorTuple";

            }

            if (type != "") {
                argTemp += "(" + type + ")";
            }
        }
        argTemp += assignmentStatement.getName();
        argTemp += " = ";
        argTemp = expr.visit(this, argTemp);
        argTemp = argTemp + ";" + "\n";
        arg += (String) argTemp;
        return arg;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        impts.add("import java.awt.Color;\n");
        impts.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
        arg += "ColorTuple.toColorTuple(Color.";
        arg += colorConstExpr.getText();
        arg += ")";

        return arg;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        Expr red = colorExpr.getRed();
        Expr green = colorExpr.getGreen();
        Expr blue = colorExpr.getBlue();

        arg += "new ColorTuple(";
        arg = red.visit(this, arg);
        arg += ", ";
        arg = green.visit(this, arg);
        arg += ", ";
        arg = blue.visit(this, arg);
        arg += ")";

        impts.add("import edu.ufl.cise.plc.runtime.ColorTuple;\n");

        return arg;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        Object argTemp = "";
        argTemp = dimension.getWidth().visit(this, argTemp);
        argTemp += ", ";
        argTemp = dimension.getHeight().visit(this, argTemp);
        arg += (String) argTemp;
        return arg;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {

        pixelSelector.getX().visit(this, arg);
        arg += ", ";
        pixelSelector.getY().visit(this, arg);
        return arg;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        impts.add("import java.awt.image.BufferedImage;\n");
        Object argTemp = "";
        argTemp += "ColorTuple.unpack(";
        argTemp += unaryExprPostfix.getExpr().getText();
        argTemp += ".getRGB(";
        argTemp += unaryExprPostfix.getSelector().getX().getText();
        argTemp += ", ";
        argTemp += unaryExprPostfix.getSelector().getY().getText();
        argTemp += "))";
        arg += (String) argTemp;
        return arg;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {

        String Type = "";

        if (nameDefWithDim.getType() == IMAGE) {
            Type = "BufferedImage";

            impts.add("import java.awt.image.BufferedImage;\n");

        } else if (nameDefWithDim.getType() == COLOR) {
            Type = "ColorTuple";

        } else {
            throw new IllegalArgumentException("Compiler bug Unexpected value: " + nameDefWithDim.getType());
        }

        arg = Type + " " + (nameDefWithDim.getName()) + arg;
        return arg;
    }
}