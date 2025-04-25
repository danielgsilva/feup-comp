package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.*;

public class ConstantPropagationVisitor extends PreorderJmmVisitor<Void, Void> {

    private boolean changed;
    private final Map<String, Map<String, JmmNode>> constants;
    private String currentMethod;

    public ConstantPropagationVisitor() {
        this.changed = false;
        this.constants = new HashMap<>();
        this.currentMethod = null;
    }

    public boolean didChange() {
        return changed;
    }

    @Override
    protected void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt);
        addVisit(Kind.WHILE_STMT, this::visitWhileStmt);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
        setDefaultVisit(this::defaultVisit);
    }

    private Void visitMethodDecl(JmmNode node, Void unused) {
        currentMethod = node.get("name");
        constants.putIfAbsent(currentMethod, new HashMap<>());

        return null;
    }

    private Void visitAssignStmt(JmmNode node, Void unused) {
        String varName = node.get("name");

        // Get the right-hand side of the assignment
        JmmNode rhs = node.getChild(0);

        // If RHS is a constant literal, store it
        if (Kind.INTEGER_LITERAL.check(rhs) || Kind.BOOLEAN_LITERAL.check(rhs)) {
            constants.get(currentMethod).put(varName, rhs);
        } else {
            constants.get(currentMethod).remove(varName);
            visit(rhs);
        }

        return null;
    }

    private Void visitVarRefExpr(JmmNode node, Void unused) {
        String varName = node.get("name");
        var methodConstants = constants.get(currentMethod);

        // Check if this variable has a constant value
        if (methodConstants != null && methodConstants.containsKey(varName)) {
            var constant = methodConstants.get(varName);

            // Replace variable reference with the constant literal
            JmmNode newNode = new JmmNodeImpl(constant.getHierarchy());
            newNode.put("value", constant.get("value"));
            newNode.put("type", constant.get("type"));

            node.replace(newNode);
            changed = true;
        }

        return null;
    }

    private Void visitWhileStmt(JmmNode node, Void unused) {
        // Get the condition of the while statement
        JmmNode condition = node.getChild(0);

        // Check if the condition contains any variable references
        var conditionVarRefs = new HashSet<String>();
        condition.getDescendants(Kind.VAR_REF_EXPR).forEach((elem) -> conditionVarRefs.add(elem.get("name")));

        // Check if any of the variables in the condition are assigned within the while block
        var usedVarRefs = new HashSet<String>();
        node.getChild(1).getDescendants(Kind.VAR_REF_EXPR).forEach(
                (elem) -> {
                    if (conditionVarRefs.contains(elem.get("name")))
                        usedVarRefs.add(elem.get("name"));
                }
        );
        constants.get(currentMethod)
                .keySet()
                .removeIf(usedVarRefs::contains);

        return null;
    }

    private Void defaultVisit(JmmNode node, Void unused) {
        for (var child : node.getChildren())
            visit(child);

        return null;
    }
}