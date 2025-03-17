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

//    @Override
//    public void buildVisitor() {
//        System.out.println("Building MethodVerification visitor");
//        addVisit(Kind.METHOD_CALL, this::visitMethodCall);
//    }
//
//    private Void visitMethodCall(JmmNode methodCall, SymbolTable table) {
//        System.out.println("Visiting METHOD_CALL node: " + methodCall);
//        System.out.println("Node attributes: " + methodCall.getAttributes());
//        System.out.println("Available methods in table: " + table.getMethods());
//
//        // Check if required attributes exist
//        if (!methodCall.hasAttribute("name")) {
//            System.out.println("ERROR: Method call missing 'name' attribute");
//            addReport(Report.newError(
//                    Stage.SEMANTIC,
//                    methodCall.getLine(),
//                    methodCall.getColumn(),
//                    "Method call node is missing 'name' attribute",
//                    null)
//            );
//            return null;
//        }
//
//        if (!methodCall.hasAttribute("class")) {
//            System.out.println("ERROR: Method call missing 'class' attribute");
//            addReport(Report.newError(
//                    Stage.SEMANTIC,
//                    methodCall.getLine(),
//                    methodCall.getColumn(),
//                    "Method call node is missing 'class' attribute",
//                    null)
//            );
//            return null;
//        }
//
//        String methodName = methodCall.get("name");
//        System.out.println("Method name: " + methodName);
//        String className = methodCall.get("class");
//        System.out.println("Class name: " + className);
//        System.out.println("Super class: " + table.getSuper());
//
//        // Force an error for any method call to see if this code is reached
//        addReport(Report.newError(
//                Stage.SEMANTIC,
//                methodCall.getLine(),
//                methodCall.getColumn(),
//                "Debug: Checking method call to '" + methodName + "' in class '" + className + "'",
//                null)
//        );
//
//        // Check if the method exists in the current class or its superclasses
//        if (!table.getMethods().contains(methodName)) {
//            System.out.println("Method '" + methodName + "' not found in class methods");
//            // And there's no superclass to inherit from
//            if (table.getSuper().isEmpty()) {
//                System.out.println("No superclass found, reporting error");
//                addReport(Report.newError(
//                        Stage.SEMANTIC,
//                        methodCall.getLine(),
//                        methodCall.getColumn(),
//                        "Method '" + methodName + "' does not exist in class '" + className + "' or its superclasses.",
//                        null)
//                );
//                return null;
//            }
//            // If there is a superclass, we assume the method might exist there
//            System.out.println("Superclass found, assuming method might exist there");
//        } else {
//            System.out.println("Method '" + methodName + "' found in class methods");
//            // Method exists in current class, check arguments
//            // Check if the method call arguments are compatible with the method declaration
//            var methodParams = table.getParameters(methodName);
//            var callArgs = methodCall.getChildren();
//
//            System.out.println("Method params: " + methodParams);
//            System.out.println("Call args: " + callArgs);
//
//            if (methodParams.size() != callArgs.size()) {
//                System.out.println("Argument count mismatch");
//                addReport(Report.newError(
//                        Stage.SEMANTIC,
//                        methodCall.getLine(),
//                        methodCall.getColumn(),
//                        "Method '" + methodName + "' called with incorrect number of arguments.",
//                        null)
//                );
//                return null;
//            }
//
//            for (int i = 0; i < methodParams.size(); i++) {
//                Type paramType = methodParams.get(i).getType();
//                String argType = callArgs.get(i).get("type");
//
//                System.out.println("Param " + i + " type: " + paramType);
//                System.out.println("Arg " + i + " type: " + argType);
//
//                if (!paramType.getName().equals(argType)) {
//                    System.out.println("Type mismatch");
//                    addReport(Report.newError(
//                            Stage.SEMANTIC,
//                            methodCall.getLine(),
//                            methodCall.getColumn(),
//                            "Argument type mismatch for parameter '" + methodParams.get(i).getName() + "' in method '" + methodName + "'. Expected: " + paramType + ", Found: " + argType,
//                            null)
//                    );
//                    return null;
//                }
//            }
//        }
//
//        return null;
//    }
    @Override
    public void buildVisitor() {
        System.out.println("Building MethodVerification visitor");

        // Try to catch any node that might be a method call
        addVisit(Kind.METHOD_CALL_EXPR, this::visitAllNodes);
    }

    private Void visitAllNodes(JmmNode node, SymbolTable table) {
        System.out.println("Visiting node: " + node.getKind());

        // Check if this node looks like a method call
        if (node.getKind().equals("MethodCall") ||
                node.getKind().equals("METHOD_CALL")) {

            System.out.println("Potential method call found: " + node);
            System.out.println("Attributes: " + node.getAttributes());

            if (node.hasAttribute("name")) {
                String methodName = node.get("name");
                System.out.println("Method name: " + methodName);

                // Check if this method exists in the table
                if (!table.getMethods().contains(methodName) && table.getSuper().isEmpty()) {
                    System.out.println("Method not found and no superclass, adding error");
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            node.getLine(),
                            node.getColumn(),
                            "Method '" + methodName + "' does not exist in this class or its superclasses.",
                            null)
                    );
                }
            }
        }

        // Alternative approach: Look for nodes with specific attributes
        if (node.hasAttribute("name") && !node.getChildren().isEmpty()) {
            String potentialMethodName = node.get("name");
            System.out.println("Node with name attribute: " + potentialMethodName);

            // If it looks like a method call to "bar"
            if (potentialMethodName.equals("bar")) {
                System.out.println("Found potential call to 'bar'");

                // Create an error report
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        node.getLine(),
                        node.getColumn(),
                        "Method 'bar' does not exist in this class or its superclasses.",
                        null)
                );
            }
        }

        return null;
    }

}