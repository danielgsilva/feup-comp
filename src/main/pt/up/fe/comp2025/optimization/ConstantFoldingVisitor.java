package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.List;

public class ConstantFoldingVisitor extends PreorderJmmVisitor<Void, Boolean> {

    private boolean changed;

    public ConstantFoldingVisitor() {
        this.changed = false;
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit(Kind.BINARY_EXPR.getNodeName(), this::foldBinaryExpr);
        setDefaultVisit(this::defaultVisit);
    }

    private Boolean foldBinaryExpr(JmmNode node, Void unused) {
        var left = node.getChild(0);
        var right = node.getChild(1);
        var op = node.get("op");

        // Only fold if both sides are integer literals
        if (!Kind.INTEGER_LITERAL.check(left) || !Kind.INTEGER_LITERAL.check(right)) {
            return false;
        }

        int leftVal = Integer.parseInt(left.get("value"));
        int rightVal = Integer.parseInt(right.get("value"));
        int result;

        try {
            result = switch (op) {
                case "+" -> leftVal + rightVal;
                case "-" -> leftVal - rightVal;
                case "*" -> leftVal * rightVal;
                case "/" -> {
                    if (rightVal == 0) throw new ArithmeticException("Division by zero");
                    yield leftVal / rightVal;
                }
                default -> throw new IllegalArgumentException("Unsupported operator for folding: " + op);
            };
        } catch (Exception e) {
            return false;
        }

        // Debug output
        System.out.printf("Constant folding: %d %s %d -> %d%n", leftVal, op, rightVal, result);

        // Create a new literal node
        JmmNode foldedNode = new JmmNodeImpl(List.of(Kind.INTEGER_LITERAL.getNodeName()));
        foldedNode.put("value", String.valueOf(result));

        // Replace the original node with the folded result
        node.replace(foldedNode);

        changed = true;
        return true;
    }

    private Boolean defaultVisit(JmmNode node, Void unused) {
        boolean localChanged = false;
        for (var child : node.getChildren()) {
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
}
