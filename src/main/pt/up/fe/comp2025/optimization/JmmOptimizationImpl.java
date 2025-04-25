package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.CompilerConfig;

import java.util.Collections;

public class JmmOptimizationImpl implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        // Create visitor that will generate the OLLIR code
        var visitor = new OllirGeneratorVisitor(semanticsResult.getSymbolTable());

        // Visit the AST and obtain OLLIR code
        var ollirCode = visitor.visit(semanticsResult.getRootNode());

        System.out.println("\nOLLIR:\n\n" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        // Check if optimization is enabled (option "-o")
        if (!CompilerConfig.getOptimize(semanticsResult.getConfig()))
            return semanticsResult;

        var ast = semanticsResult.getRootNode();

        // Print AST before optimization
        //System.out.println("\nAST BEFORE OPTIMIZATION:\n\n" + ast.toTree());

        boolean changed;
        do {
            // Apply constant propagation
            var propagationVisitor = new ConstantPropagationVisitor();
            propagationVisitor.visit(ast);

            // Apply constant folding
            var foldingVisitor = new ConstantFoldingVisitor();
            foldingVisitor.visit(ast);

            changed = propagationVisitor.didChange() || foldingVisitor.didChange();

        } while (changed);

        // Print AST after optimization
        //System.out.println("\nAST AFTER OPTIMIZATION:\n\n" + ast.toTree());

        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {

        //TODO: Do your OLLIR-based optimizations here

        return ollirResult;
    }


}
