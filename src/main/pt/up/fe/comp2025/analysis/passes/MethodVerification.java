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
        addVisit(Kind.METHOD_CALL, this::visitMethodCall);
    }

    private Void visitMethodCall(JmmNode methodCall, SymbolTable table) {
        String methodName = methodCall.get("name");
        String className = methodCall.get("class");

        // Check if the method exists in the current class or its superclasses
        if (!table.getMethods().contains(methodName)) {
            // And there's no superclass to inherit from
            if (table.getSuper().isEmpty()) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        methodCall.getLine(),
                        methodCall.getColumn(),
                        "Method '" + methodName + "' does not exist in class '" + className + "' or its superclasses.",
                        null)
                );
                return null;
            }
            // If there is a superclass, we assume the method might exist there
            // No error report in this case
        } else {
            // Method exists in current class, check arguments
            // Check if the method call arguments are compatible with the method declaration
            var methodParams = table.getParameters(methodName);
            var callArgs = methodCall.getChildren();

            if (methodParams.size() != callArgs.size()) {
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        methodCall.getLine(),
                        methodCall.getColumn(),
                        "Method '" + methodName + "' called with incorrect number of arguments.",
                        null)
                );
                return null;
            }

            for (int i = 0; i < methodParams.size(); i++) {
                Type paramType = methodParams.get(i).getType();
                String argType = callArgs.get(i).get("type");

                if (!paramType.getName().equals(argType)) {
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            methodCall.getLine(),
                            methodCall.getColumn(),
                            "Argument type mismatch for parameter '" + methodParams.get(i).getName() + "' in method '" + methodName + "'. Expected: " + paramType + ", Found: " + argType,
                            null)
                    );
                    return null;
                }
            }
        }

        return null;
    }
}