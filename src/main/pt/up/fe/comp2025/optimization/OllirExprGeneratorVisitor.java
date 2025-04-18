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
        addVisit(BOOLEAN_LITERAL, this::visitBoolean);
        addVisit(METHOD_CALL_EXPR, this::visitMethodCallExpr);
        addVisit(NEW_INT_ARRAY_EXPR, this::visitNewIntArrayExpr);
        addVisit(LENGTH_EXPR, this::visitLengthExpr);
        addVisit(ARRAY_ACCESS_EXPR, this::visitArrayAccessExpr);
        addVisit(NEW_OBJECT_EXPR, this::visitNewObjectExpr);

        setDefaultVisit(this::defaultVisit);
    }

    private OllirExprResult visitNewObjectExpr(JmmNode node, Void unused) {
        String className = node.get("name");
        String ollirType = ollirTypes.toOllirType(node.get("type"));
        String code = ollirTypes.nextTemp() + ollirType;

        StringBuilder computation = new StringBuilder();
        computation.append(code).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE)
                .append("new").append("(").append(className).append(")").append(ollirType).append(END_STMT);
        computation.append("invokespecial(").append(code).append(", \"<init>\").V").append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitArrayAccessExpr(JmmNode node, Void unused) {
        var array = visit(node.getChild(0));
        var index = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();
        computation.append(array.getComputation());
        computation.append(index.getComputation());

        String ollirType = ollirTypes.toOllirType(node.get("type"));
        String code = ollirTypes.nextTemp() + ollirType;

        computation.append(code).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE)
                .append(array.getCode()).append("[").append(index.getCode()).append("]").append(ollirType).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitLengthExpr(JmmNode node, Void unused) {
        var array = visit(node.getChild(0));

        StringBuilder computation = new StringBuilder();
        computation.append(array.getComputation());

        String ollirType = ollirTypes.toOllirType(node.get("type"));
        String code = ollirTypes.nextTemp() + ollirType;

        computation.append(code).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE)
                .append("arraylength(").append(array.getCode()).append(")").append(ollirType).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitNewIntArrayExpr(JmmNode node, Void unused) {
        var size = visit(node.getChild(0));

        StringBuilder computation = new StringBuilder();
        computation.append(size.getComputation());

        String ollirType = ollirTypes.toOllirType(node.get("type"));
        String code = ollirTypes.nextTemp() + ollirType;

        computation.append(code).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE)
                .append("new").append("(").append("array").append(", ").append(size.getCode()).append(")")
                .append(ollirType).append(END_STMT);

        return new OllirExprResult(code, computation);
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

        // if the method is void we don't need to assign the result to tmp
        boolean isVoid = methodType.getName().equals("void");
        String code = isVoid ? "" : ollirTypes.nextTemp() + methodOllirType;
        if (!isVoid)
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

    private OllirExprResult visitBoolean(JmmNode node, Void unused) {
        var booleanType = TypeUtils.newBooleanType();
        String ollirBooleanType = ollirTypes.toOllirType(booleanType);
        String code = (node.get("value").equals("true") ? "1" : "0") + ollirBooleanType;
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

    private boolean isField(JmmNode node) {
        String methodName = node.getAncestor(METHOD_DECL).get().get("name");
        String varRefExprName = node.get("name");

        for (var localVar: table.getLocalVariables(methodName)) {
            if (localVar.getName().equals(varRefExprName))
                return false;
        }

        for (var param: table.getParameters(methodName)) {
            if (param.getName().equals(varRefExprName))
                return false;
        }

        for (var field: table.getFields()) {
            if (field.getName().equals(varRefExprName))
                return true;
        }

        return false;
    }


    private OllirExprResult visitVarRef(JmmNode node, Void unused) {

        var id = node.get("name");
        Type type = types.getExprType(node);

        if (type.getName().equals("imported"))
            return new OllirExprResult(id);

        String ollirType = ollirTypes.toOllirType(type);
        String code = id + ollirType;

        StringBuilder computation = new StringBuilder();
        if(isField(node)) {
            String tmp = ollirTypes.nextTemp() + ollirType;

            computation.append(tmp).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE)
                    .append("getfield(this, ").append(code).append(")").append(ollirType).append(END_STMT);
            code = tmp;
        }

        return new OllirExprResult(code, computation);
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
