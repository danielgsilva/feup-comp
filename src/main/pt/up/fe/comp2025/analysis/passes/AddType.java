package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

/**
 * Annotates each expression node with its type.
 */
public class AddType extends AnalysisVisitor {

    private String currentMethod;
    private final TypeUtils types;

    public AddType(SymbolTable table) {
        this.types = new TypeUtils(table);
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.BINARY_EXPR, this::visitBinaryExpr);
        addVisit(Kind.INTEGER_LITERAL, this::visitIntegerLiteral);
        addVisit(Kind.BOOLEAN_LITERAL, this::visitBooleanLiteral);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    private Void visitBinaryExpr(JmmNode binaryExpr, SymbolTable table) {
        Type type = types.getExprType(binaryExpr);
        binaryExpr.put("type", type.toString());
        return null;
    }

    private Void visitIntegerLiteral(JmmNode integerLiteral, SymbolTable table) {
        integerLiteral.put("type", TypeUtils.newIntType().toString());
        return null;
    }

    private Void visitBooleanLiteral(JmmNode booleanLiteral, SymbolTable table) {
        booleanLiteral.put("type", TypeUtils.newBooleanType().toString());
        return null;
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable table) {
        var varRefName = varRefExpr.get("name");

        // Check if the variable is a local variable
        for (var localVar : table.getLocalVariables(currentMethod)) {
            if (localVar.getName().equals(varRefName)) {
                varRefExpr.put("type", localVar.getType().toString());
                return null;
            }
        }

        // Check if the variable is a parameter
        for (var param: table.getParameters(currentMethod)) {
            if (param.getName().equals(varRefName)) {
                varRefExpr.put("type", param.getType().toString());
                return null;
            }
        }

        // Check if the variable is a field
        for (var field : table.getFields()) {
            if (field.getName().equals(varRefName)) {
                varRefExpr.put("type", field.getType().toString());
                return null;
            }
        }

        // Variable not found
        throw new IllegalArgumentException("Variable '" + varRefExpr.get("name") + "' not found in method '" + currentMethod + "'");
    }

}
