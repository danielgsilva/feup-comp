package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.specs.util.SpecsCheck;

public class MethodVerification extends AnalysisVisitor {
    @Override
    public void buildVisitor() {
        System.out.println("Building MethodVerification visitor");

        // Catch all nodes that might represent method calls
        addVisit(Kind.METHOD_CALL_EXPR, this::visitAllNodes);
    }

    private Void visitAllNodes(JmmNode node, SymbolTable table) {
        System.out.println("Visiting node: " + node.getKind());

        // Check if this node represents a method call
        if (isMethodCall(node)) {
            System.out.println("Potential method call found: " + node);
            System.out.println("Attributes: " + node.getAttributes());

            if (node.hasAttribute("name")) {
                String methodName = node.get("name");
                System.out.println("Method name: " + methodName);

                // Check if this method exists in the current class
                if (!table.getMethods().contains(methodName)) {
                    // Handle null or empty superclass (treat it as no superclass)
                    String superClass = table.getSuper();
                    if (superClass == null || superClass.isEmpty()) {
                        // Check if the method is assumed to exist in an imported class
                        if (!isMethodInImportedClass(methodName, table)) {
                            System.out.println("Method not found in current class, superclass, or imported classes, adding error");
                            addReport(Report.newError(
                                    Stage.SEMANTIC,
                                    node.getLine(),
                                    node.getColumn(),
                                    "Method '" + methodName + "' does not exist in this class, its superclasses, or imported classes.",
                                    null)
                            );
                        } else {
                            System.out.println("Method assumed to exist in imported class.");
                        }
                    } else {
                        System.out.println("Method not found, but superclass exists. Assuming method might be in superclass.");
                    }
                }
            }
        }

        return null;
    }

    /**
     * Helper method to check if a node represents a method call.
     */
    private boolean isMethodCall(JmmNode node) {
        // Check if the node kind indicates a method call
        return node.getKind().equals("MethodCall") ||
                node.getKind().equals("METHOD_CALL") ||
                node.getKind().equals("MethodCallExpr");
    }

    /**
     * Helper method to check if a method exists in an imported class.
     */
    private boolean isMethodInImportedClass(String methodName, SymbolTable table) {
        // Get the list of imported classes from the SymbolTable
        var imports = table.getImports();
        for (String importedClass : imports) {
            // Assume that the method exists in the imported class
            // (In a real implementation, you would need to check the imported class's methods)
            System.out.println("Assuming method '" + methodName + "' exists in imported class: " + importedClass);
            return true;
        }
        return false;
    }

}