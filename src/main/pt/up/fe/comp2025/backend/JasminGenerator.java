package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.*;
import org.specs.comp.ollir.tree.TreeNode;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates Jasmin code from an OllirResult.
 * <p>
 * One JasminGenerator instance per OllirResult.
 */
public class JasminGenerator {

    private static final String NL = "\n";
    private static final String TAB = "   ";

    private final OllirResult ollirResult;

    List<Report> reports;

    String code;

    Method currentMethod;
    Limits limits;

    private final JasminUtils types;

    private final FunctionClassMap<TreeNode, String> generators;

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;
        limits = null;

        types = new JasminUtils(ollirResult);

        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(NewInstruction.class, this::generateNew);
    }

    private String generateNew(NewInstruction newInstruction) {
        var callerType = newInstruction.getCaller().getType();

        if (callerType instanceof ArrayType arrayType) {
            var code = new StringBuilder();

            SpecsCheck.checkArgument(newInstruction.getArguments().size() == 1,
                    () -> "Expected number of arguments to be 1: " + newInstruction.getArguments().size());
            code.append(apply(newInstruction.getArguments().getFirst()));

            var typeCode = types.getArrayType(arrayType.getElementType());
            code.append("newarray ").append(typeCode).append(NL);
            limits.decrement();
            limits.increment();

            return code.toString();
        }

        return "";
    }

    private String apply(TreeNode node) {
        var code = new StringBuilder();

        // Print the corresponding OLLIR code as a comment
        //code.append("; ").append(node).append(NL);

        code.append(generators.apply(node));

        return code.toString();
    }


    public List<Report> getReports() {
        return reports;
    }

    public String build() {

        // This way, build is idempotent
        if (code == null) {
            code = apply(ollirResult.getOllirClass());
        }

        return code;
    }


    private String generateClassUnit(ClassUnit classUnit) {

        var code = new StringBuilder();

        // generate class name
        var className = ollirResult.getOllirClass().getClassName();
        code.append(".class ").append(className).append(NL).append(NL);

        // TODO: When you support 'extends', this must be updated
        var fullSuperClass = "java/lang/Object";

        code.append(".super ").append(fullSuperClass).append(NL);

        // generate a single constructor method
        var defaultConstructor = """
                ;default constructor
                .method public <init>()V
                    aload_0
                    invokespecial %s/<init>()V
                    return
                .end method
                """.formatted(fullSuperClass);
        code.append(defaultConstructor);

        // generate code for all other methods
        for (var method : ollirResult.getOllirClass().getMethods()) {

            // Ignore constructor, since there is always one constructor
            // that receives no arguments, and has been already added
            // previously
            if (method.isConstructMethod()) {
                continue;
            }

            code.append(apply(method));
        }

        return code.toString();
    }


    private String generateMethod(Method method) {
        //System.out.println("STARTING METHOD " + method.getMethodName());
        // set method
        currentMethod = method;
        limits = new Limits();

        var code = new StringBuilder();

        // calculate modifier
        var modifier = types.getModifier(method.getMethodAccessModifier());

        var methodName = method.getMethodName();

        var params = method.getParams().stream()
                .map(elem -> types.getDescriptor(elem.getType()))
                .collect(Collectors.joining());

        var returnType = types.getDescriptor(method.getReturnType());

        code.append("\n.method ").append(modifier)
                .append(methodName)
                .append("(" + params + ")" + returnType).append(NL);

        var bodyCode = new StringBuilder();
        for (var inst : method.getInstructions()) {
            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));
            bodyCode.append(instCode);
        }

        // Add limits
        code.append(TAB).append(".limit stack ").append(limits.getMaxStack()).append(NL);
        code.append(TAB).append(".limit locals ").append(limits.getMaxLocals()).append(NL);

        code.append(bodyCode);
        code.append(".end method\n");

        // unset method
        currentMethod = null;
        limits = null;
        //System.out.println("ENDING METHOD " + method.getMethodName());
        return code.toString();
    }

    private String generateAssign(AssignInstruction assign) {
        var code = new StringBuilder();

        // generate code for loading what's on the right
        code.append(apply(assign.getRhs()));

        // store value in the stack in destination
        var lhs = assign.getDest();

        if (!(lhs instanceof Operand)) {
            throw new NotImplementedException(lhs.getClass());
        }

        var operand = (Operand) lhs;

        code.append(store(operand));

        return code.toString();
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {
        limits.increment();
        return "ldc " + literal.getLiteral() + NL;
    }

    private String generateOperand(Operand operand) {
        return load(operand);
    }

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();

        // load values on the left and on the right
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));

        // TODO: Hardcoded for int type, needs to be expanded
        var typePrefix = "i";

        // apply operation
        var op = switch (binaryOp.getOperation().getOpType()) {
            case ADD -> "add";
            case MUL -> "mul";
            default -> throw new NotImplementedException(binaryOp.getOperation().getOpType());
        };

        code.append(typePrefix + op).append(NL);

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        var code = new StringBuilder();

        if (returnInst.getOperand().isEmpty()) {
            throw new NotImplementedException("void method");
        }

        var loadOperand = apply(returnInst.getOperand().get());
        code.append(loadOperand);
        code.append(types.getPrefix(returnInst.getReturnType())).append("return").append(NL);
        limits.decrement();

        return code.toString();
    }

    private String store(Operand operand) {
        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        var prefix = types.getPrefix(operand.getType());

        limits.updateLocals(reg.getVirtualReg());
        limits.decrement();

        var virtualReg = reg.getVirtualReg();

        return prefix + "store " + virtualReg + NL;
    }

    private String load(Operand operand) {
        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        var prefix = types.getPrefix(operand.getType());

        limits.updateLocals(reg.getVirtualReg());
        limits.increment();

        var virtualReg = reg.getVirtualReg();

        return prefix + "load " + virtualReg + NL;
    }
}