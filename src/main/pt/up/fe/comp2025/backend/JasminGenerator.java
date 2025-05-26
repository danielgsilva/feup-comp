package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final Map<String, String> importedClassPaths;

    private final JasminUtils types;

    private final FunctionClassMap<TreeNode, String> generators;

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;
        limits = null;

        types = new JasminUtils(ollirResult);

        importedClassPaths = new HashMap<>();
        for (var importPath : ollirResult.getOllirClass().getImports()) {
            var parts = importPath.split("\\.");
            var lastPart = parts[parts.length - 1];
            importedClassPaths.put(lastPart, importPath.replace('.', '/'));
        }

        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Field.class, this::generateField);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(PutFieldInstruction.class, this::putField);
        generators.put(GetFieldInstruction.class, this::getField);
        generators.put(NewInstruction.class, this::generateNew);
        generators.put(InvokeSpecialInstruction.class, this::generateInvokeSpecial);
    }

    private String putField(PutFieldInstruction putFieldInstruction) {
        var code = new StringBuilder();

        code.append("aload_0").append(NL);
        limits.increment();

        code.append(generators.apply(putFieldInstruction.getOperands().get(2)));

        var className = currentMethod.getOllirClass().getClassName();
        var fieldName = putFieldInstruction.getField().getName();

        code.append("putfield ").append(className).append("/").append(fieldName)
                .append(" ").append(types.getDescriptor(putFieldInstruction.getField().getType())).append(NL);

        limits.decrement(2);

        return code.toString();
    }

    private String getField(GetFieldInstruction getFieldInstruction) {
        var code = new StringBuilder();

        var className = currentMethod.getOllirClass().getClassName();
        var fieldName = getFieldInstruction.getField().getName();

        code.append("aload_0").append(NL);
        code.append("getfield ").append(className).append("/").append(fieldName)
                .append(" ").append(types.getDescriptor(getFieldInstruction.getField().getType())).append(NL);

        limits.increment();

        return code.toString();
    }

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeSpecial) {
        var code = new StringBuilder();

        Operand caller = (Operand) invokeSpecial.getCaller();
        if (this.currentMethod.getVarTable().get(caller.getName()) != null) {
            code.append(generators.apply(caller));
        }

        var className = "";
        if (invokeSpecial.getCaller().getType() instanceof ClassType Callertype) {
            className = Callertype.getName();
        }

        var fullClassName = importedClassPaths.getOrDefault(className, className);
        code.append("invokenonvirtual ").append(fullClassName).append("/<init>()V").append(NL);

        limits.decrement();

        return code.toString();
    }

    private String generateField(Field field) {
        var code = new StringBuilder();

        code.append(".field ");

        var accessModifier = "";
        switch (field.getFieldAccessModifier()) {
            case PUBLIC -> accessModifier = "public ";
            case PRIVATE -> accessModifier = "private ";
            case PROTECTED -> accessModifier = "protected ";
            case DEFAULT -> accessModifier = "";
        }

        code.append(accessModifier).append("'").append(field.getFieldName()).append("'").append(" ").
                append(types.getDescriptor(field.getFieldType())).append(NL);

        return code.toString();
    }

    private String generateNew(NewInstruction newInstruction) {
        var callerType = newInstruction.getCaller().getType();

        var code = new StringBuilder();
        if (callerType instanceof ArrayType arrayType) {

            SpecsCheck.checkArgument(newInstruction.getArguments().size() == 1,
                    () -> "Expected number of arguments to be 1: " + newInstruction.getArguments().size());
            code.append(apply(newInstruction.getArguments().getFirst()));

            var typeCode = types.getArrayType(arrayType.getElementType());
            code.append("newarray ").append(typeCode).append(NL);
            limits.decrement();
            limits.increment();

            return code.toString();
        } else if (callerType instanceof ClassType classType) {
            var className = classType.getName();
            var fullClassName = importedClassPaths.getOrDefault(className, className);

            code.append("new ").append(fullClassName).append(NL);
            limits.increment();

            return code.toString();

        }

        // TODO: Handle other types of new instructions
        throw new NotImplementedException(callerType);
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

        var fullSuperClass = "";
        if (classUnit.getSuperClass() != null) {
            var superClass = classUnit.getSuperClass();
            if (importedClassPaths.containsKey(superClass)) {
                fullSuperClass = importedClassPaths.get(superClass);
            } else {
                fullSuperClass = superClass.replace('.', '/');
            }
        } else {
            fullSuperClass = "java/lang/Object";
        }

        code.append(".super ").append(fullSuperClass).append(NL);

        for (var field : ollirResult.getOllirClass().getFields()) {
            code.append(generators.apply(field));
        }

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

        code.append("\n.method ").append(modifier);

        if (method.isStaticMethod())
            code.append("static ");

        code.append(methodName).append("(").append(params).append(")").append(returnType).append(NL);

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

        int intValue = Integer.parseInt(literal.getLiteral());

        if (intValue == -1) {
            return "iconst_m1" + NL;
        } else if (intValue >= 0 && intValue <= 5) {
            return "iconst_" + intValue + NL;
        } else if (intValue >= -128 && intValue <= 127) {
            return "bipush " + intValue + NL;
        } else if (intValue >= -32768 && intValue <= 32767) {
            return "sipush " + intValue + NL;
        } else {
            return "ldc " + literal.getLiteral() + NL;
        }
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
            code.append("return").append(NL);
        } else {
            var loadOperand = apply(returnInst.getOperand().get());
            code.append(loadOperand);
            code.append(types.getPrefix(returnInst.getReturnType())).append("return").append(NL);
            limits.decrement();
        }

        return code.toString();
    }

    private String store(Operand operand) {
        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        var prefix = types.getPrefix(operand.getType());

        limits.updateLocals(reg.getVirtualReg());
        limits.decrement();

        var virtualReg = reg.getVirtualReg();

        if (virtualReg >= 0 && virtualReg <= 3)
            return prefix + "store_" + virtualReg + NL;

        return prefix + "store " + virtualReg + NL;
    }

    private String load(Operand operand) {
        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        var prefix = types.getPrefix(operand.getType());

        limits.updateLocals(reg.getVirtualReg());
        limits.increment();

        var virtualReg = reg.getVirtualReg();

        if (virtualReg >= 0 && virtualReg <= 3)
            return prefix + "load_" + virtualReg + NL;

        return prefix + "load " + virtualReg + NL;
    }
}