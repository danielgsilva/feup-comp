package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;

/**
 * Checks ...
 *
 */
public class ArrayIndexOutOfBounds extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.ARRAY_ACCESS_EXPR, this::visitArrayAccessExpr);
    }

    private Void visitArrayAccessExpr(JmmNode arrayAccessExpr, SymbolTable table) {
        var arrayIndex = arrayAccessExpr.getChild(1);
        if (arrayIndex.get("type").equals(TypeUtils.newIntType().toString())) {
            return null;
        }

        // Create error report
        var message = String.format("Array access index must be of type integer but found '%s'", arrayIndex.get("type"));
        addReport(Report.newError(
                Stage.SEMANTIC,
                arrayAccessExpr.getLine(),
                arrayAccessExpr.getColumn(),
                message,
                null)
        );

        return null;
    }


}
