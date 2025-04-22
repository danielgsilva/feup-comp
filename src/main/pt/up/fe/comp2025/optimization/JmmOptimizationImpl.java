package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;

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
        var ast = semanticsResult.getRootNode();

        // DEBUG: print AST before optimization
        System.out.println("AST BEFORE OPTIMIZATION:\n" + ast.toTree());

        var folding = new ConstantFoldingVisitor();

        boolean changed;
        do {
            folding.reset();
            changed = folding.visit(ast);
        } while (changed);

        // DEBUG: print AST after optimization
        System.out.println("AST AFTER OPTIMIZATION:\n" + ast.toTree());

        var parserResult = new JmmParserResult(ast, semanticsResult.getReports(), semanticsResult.getConfig());

        return new JmmSemanticsResult(parserResult,
                semanticsResult.getSymbolTable(),
                semanticsResult.getReports());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {

        //TODO: Do your OLLIR-based optimizations here

        return ollirResult;
    }


}
