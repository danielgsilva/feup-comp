package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.symboltable.JmmSymbolTable;

/**
 * Utility methods regarding types.
 */
public class TypeUtils {


    private final JmmSymbolTable table;

    private static final String MULT = "*";
    private static final String DIV = "/";
    private static final String PLUS = "+";
    private static final String MINUS = "-";
    private static final String LT = "<";
    private static final String AND = "&&";
    private static final String NOT = "!";

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    public static Type newIntType() {
        return new Type("int", false);
    }
    public static Type newArrayIntType() { return new Type("int", true);}
    public static Type newBooleanType() { return new Type("boolean", false); }

    public static Type convertType(JmmNode typeNode) {

        // TODO: When you support new types, this must be updated
        var name = typeNode.get("name");
        var isArray = Boolean.parseBoolean(typeNode.get("isArray"));

        return new Type(name, isArray);
    }


    /**
     * Gets the {@link Type} of an arbitrary expression.
     *
     * @param expr
     * @return
     */
    public Type getExprType(JmmNode expr) {
        // TODO: Update when there are new types

        switch (Kind.fromString(expr.getKind())) {
            case BINARY_EXPR:
                return getBinaryExprType(expr);
            default:
                throw new IllegalArgumentException("Unsupported expression type: " + expr.getKind());
        }
    }

    private static Type getBinaryExprType(JmmNode binaryExpr) {
        var operator = binaryExpr.get("op");
        return switch (operator) {
            case MULT, DIV, PLUS, MINUS, LT -> newIntType();
            case NOT, AND -> newBooleanType();
            default ->
                    throw new RuntimeException("Unknown operator " + operator);
        };
    }


}
