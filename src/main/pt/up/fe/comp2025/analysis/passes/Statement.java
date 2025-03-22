package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;

public class Statement extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        addVisit(Kind.IF_STMT, this::visitIfStmt);
        addVisit(Kind.WHILE_STMT, this::visitWhileStmt);
    }

    private Void visitIfStmt(JmmNode ifStmt, SymbolTable table) {
        var expr = ifStmt.getChild(0);
        var exprType = TypeUtils.getNameType(expr.get("type"));
        if(exprType.equals("boolean"))
            return null;

        // Create error report
        var message = String.format("Condition in if statement must return a boolean, but got '%s'", exprType);
        addReport(Report.newError(
                Stage.SEMANTIC,
                ifStmt.getLine(),
                ifStmt.getColumn(),
                message,
                null)
        );

        return null;
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable table) {
        var expr = whileStmt.getChild(0);
        var exprType = TypeUtils.getNameType(expr.get("type"));
        if(exprType.equals("boolean"))
            return null;

        // Create error report
        var message = String.format("Condition in while statement must return a boolean, but got '%s'", exprType);
        addReport(Report.newError(
                Stage.SEMANTIC,
                whileStmt.getLine(),
                whileStmt.getColumn(),
                message,
                null)
        );

        return null;
    }


}
