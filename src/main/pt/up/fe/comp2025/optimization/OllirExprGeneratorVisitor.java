package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends PreorderJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    public OllirExprGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
    }


    @Override
    protected void buildVisitor() {
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(BINARY_EXPR, this::visitBinExpr);
        addVisit(INTEGER_LITERAL, this::visitInteger);
        addVisit(METHOD_CALL_EXPR, this::visitMethodCallExpr);

        setDefaultVisit(this::defaultVisit);
    }

    private OllirExprResult visitMethodCallExpr(JmmNode node, Void unused) {

        var caller = visit(node.getChild(0));

        StringBuilder computation = new StringBuilder();
        computation.append(caller.getComputation());

        List<String> tmpCodes = new ArrayList<>();
        var numParams = node.getChildren().size() - 1;
        for (int i = 1; i <= numParams; i++) {
            var param = visit(node.getChild(i));
            computation.append(param.getComputation());
            tmpCodes.add(param.getCode());
        }

        var methodName = node.get("name");
        var methodType = TypeUtils.getTypeFromString(node.get("type"));

        // if method type is imported, we try to get the type from the assign statement
        // if it is not present, we set it to void
        var assignStmt = node.getAncestor(ASSIGN_STMT);
        if (methodType.getName().equals("imported")) {
            if (assignStmt.isPresent())
                methodType = TypeUtils.getTypeFromString(assignStmt.get().get("type"));
            else
                methodType = TypeUtils.newVoidType();
        }
        var methodOllirType = ollirTypes.toOllirType(methodType);

        // if the method is void or the assign statement is empty, we don't need to assign the result to tmp
        boolean isVoid = methodType.getName().equals("void");
        String code = (isVoid || assignStmt.isEmpty()) ? "" : ollirTypes.nextTemp() + methodOllirType;
        if (!isVoid && assignStmt.isPresent())
            computation.append(code).append(SPACE).append(ASSIGN).append(methodOllirType).append(SPACE);

        var callerType = TypeUtils.getTypeFromString(node.getChild(0).get("type"));
        if (callerType.getName().equals("imported"))
            computation.append("invokestatic");
        else if (methodName.equals(table.getClassName()))
            computation.append("invokespecial");
        else
            computation.append("invokevirtual");

        computation.append("(").append(caller.getCode()).append(", ").append(String.format("\"%s\"", methodName));
        for (int i = 1; i <= numParams; i++) {
            computation.append(", ");
            computation.append(tmpCodes.get(i - 1));
        }
        computation.append(")").append(methodOllirType).append(END_STMT);

        return new OllirExprResult(code, computation.toString());
    }


    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        var intType = TypeUtils.newIntType();
        String ollirIntType = ollirTypes.toOllirType(intType);
        String code = node.get("value") + ollirIntType;
        return new OllirExprResult(code);
    }


    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {

        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();

        // code to compute the children
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        // code to compute self
        Type resType = types.getExprType(node);
        String resOllirType = ollirTypes.toOllirType(resType);
        String code = ollirTypes.nextTemp() + resOllirType;

        computation.append(code).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE);

        Type type = types.getExprType(node);
        computation.append(node.get("op")).append(ollirTypes.toOllirType(type)).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(code, computation);
    }


    private OllirExprResult visitVarRef(JmmNode node, Void unused) {

        var id = node.get("name");
        Type type = types.getExprType(node);
        if (type.getName().equals("imported"))
            return new OllirExprResult(id);
        String ollirType = ollirTypes.toOllirType(type);

        String code = id + ollirType;

        return new OllirExprResult(code);
    }

    /**
     * Default visitor. Visits every child node and return an empty result.
     *
     * @param node
     * @param unused
     * @return
     */
    private OllirExprResult defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return OllirExprResult.EMPTY;
    }

}
