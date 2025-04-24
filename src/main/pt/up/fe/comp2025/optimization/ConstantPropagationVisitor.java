package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantPropagationVisitor extends PreorderJmmVisitor<Void, Boolean> {

    private boolean changed;
    private Map<String, Map<String, Constant>> constants; // methodName -> varName -> constant value
    private String currentMethod;
    private final SymbolTable symbolTable;

    public ConstantPropagationVisitor(SymbolTable symbolTable) {
        this.changed = false;
        this.constants = new HashMap<>();
        this.currentMethod = null;
        this.symbolTable = symbolTable;
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL.getNodeName(), this::visitMethodDecl);
        addVisit(Kind.ASSIGN_STMT.getNodeName(), this::visitAssignStmt);
        addVisit(Kind.VAR_REF_EXPR.getNodeName(), this::visitVarRefExpr);
        setDefaultVisit(this::defaultVisit);
    }

    private Boolean visitMethodDecl(JmmNode node, Void unused) {
        // Save current method context
        currentMethod = node.get("name");
        constants.putIfAbsent(currentMethod, new HashMap<>());

        // Continue visiting children
        boolean localChanged = false;
        for (JmmNode child : node.getChildren()) {
            localChanged |= visit(child);
        }

        return localChanged;
    }

    private Boolean visitAssignStmt(JmmNode node, Void unused) {
        if (currentMethod == null) {
            return false;
        }

        // Get the right-hand side of the assignment
        JmmNode rhs = node.getChild(0);
        String varName = node.get("name");

        // Visit the RHS first in case it contains references to variables that need propagation
        boolean rhsChanged = visit(rhs);

        // Check if this is a field assignment (we don't propagate fields)
        if (isField(varName)) {
            // Remove the variable from constants if it was there
            if (constants.get(currentMethod).containsKey(varName)) {
                constants.get(currentMethod).remove(varName);
                changed = true;
            }
            return rhsChanged;
        }

        // If RHS is a constant literal, store it
        if (Kind.INTEGER_LITERAL.check(rhs) || Kind.BOOLEAN_LITERAL.check(rhs)) {
            Constant constant = new Constant(rhs);

            // Check if we're updating an existing constant or adding a new one
            if (!constants.get(currentMethod).containsKey(varName) ||
                    !constants.get(currentMethod).get(varName).equals(constant)) {
                constants.get(currentMethod).put(varName, constant);
                changed = true;
                System.out.printf("Constant propagation: %s = %s%n", varName, constant.getValue());
            }
        } else {
            // If RHS is not a constant, remove the variable from constants if it was there
            if (constants.get(currentMethod).containsKey(varName)) {
                constants.get(currentMethod).remove(varName);
                changed = true;
            }
        }

        return rhsChanged || changed;
    }

    private Boolean visitVarRefExpr(JmmNode node, Void unused) {
        if (currentMethod == null) {
            return false;
        }

        String varName = node.get("name");
        Map<String, Constant> methodConstants = constants.get(currentMethod);

        // Check if this variable has a constant value
        if (methodConstants != null && methodConstants.containsKey(varName)) {
            Constant constant = methodConstants.get(varName);

            // Replace variable reference with the constant literal
            JmmNode constantNode = constant.toNode();
            if (node.hasAttribute("type")) {
                constantNode.put("type", node.get("type"));
            }

            node.replace(constantNode);

            changed = true;
            System.out.printf("Propagated constant: %s -> %s%n", varName, constant.getValue());
            return true;
        }

        return false;
    }

    private boolean isField(String varName) {
        // Check if the variable is a parameter in the current method
        if (symbolTable.getParameters(currentMethod) != null) {
            for (Symbol param : symbolTable.getParameters(currentMethod)) {
                if (param.getName().equals(varName)) {
                    return false;
                }
            }
        }

        // Check if the variable is a local variable in the current method
        if (symbolTable.getLocalVariables(currentMethod) != null) {
            for (Symbol localVar : symbolTable.getLocalVariables(currentMethod)) {
                if (localVar.getName().equals(varName)) {
                    return false;
                }
            }
        }

        // Check if the variable is a field of the class
        for (Symbol field : symbolTable.getFields()) {
            if (field.getName().equals(varName)) {
                return true;
            }
        }

        // If the variable is not found at all, it's likely an error
        // but for the purpose of this check, we won't treat it as a field
        return false;
    }

    private Boolean defaultVisit(JmmNode node, Void unused) {
        boolean localChanged = false;
        for (JmmNode child : node.getChildren()) {
            localChanged |= visit(child);
        }
        return localChanged;
    }

    public boolean didChange() {
        return changed;
    }

    public void reset() {
        changed = false;
    }

    // Inner class to represent constants
    public static class Constant {
        private final String kind;
        private final String value;

        public Constant(JmmNode node) {
            this.kind = node.getKind();
            this.value = node.get("value");
        }

        public String getValue() {
            return value;
        }

        public JmmNode toNode() {
            JmmNode node = new JmmNodeImpl(List.of(this.kind));
            node.put("value", this.value);
            return node;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Constant)) {
                return false;
            }
            Constant other = (Constant) obj;
            return this.kind.equals(other.kind) && this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return 31 * kind.hashCode() + value.hashCode();
        }
    }
}