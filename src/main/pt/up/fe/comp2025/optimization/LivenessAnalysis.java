package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;

import java.util.*;

public class LivenessAnalysis {

    private final Map<Instruction, Set<String>> inMap = new HashMap<>();
    private final Map<Instruction, Set<String>> outMap = new HashMap<>();

    public Map<Instruction, Set<String>> getInMap() {
        return inMap;
    }

    public Map<Instruction, Set<String>> getOutMap() {
        return outMap;
    }

    public void analyze(Method method) {
        if (!method.getInstructions().isEmpty()) {
            method.buildCFG();
        }

        List<Instruction> instructions = method.getInstructions();

        // Inicializa conjuntos in e out
        for (Instruction inst : instructions) {
            inMap.put(inst, new HashSet<>());
            outMap.put(inst, new HashSet<>());
        }

        boolean changed;
        do {
            changed = false;

            for (int i = instructions.size() - 1; i >= 0; i--) {
                Instruction inst = instructions.get(i);

                Set<String> oldIn = new HashSet<>(inMap.get(inst));
                Set<String> oldOut = new HashSet<>(outMap.get(inst));

                // OUT[n] = união dos IN dos sucessores
                Set<String> out = new HashSet<>();
                for (Node succ : inst.getSuccessors()) {
                    if (succ instanceof Instruction successor && inMap.containsKey(successor)) {
                        out.addAll(inMap.get(successor));
                    }
                }

                Set<String> use = getUse(inst);
                Set<String> def = getDef(inst);

                // IN[n] = use[n] U (OUT[n] - def[n])
                Set<String> in = new HashSet<>(use);
                Set<String> outMinusDef = new HashSet<>(out);
                outMinusDef.removeAll(def);
                in.addAll(outMinusDef);

                // Atualiza
                inMap.put(inst, in);
                outMap.put(inst, out);

                if (!in.equals(oldIn) || !out.equals(oldOut)) {
                    changed = true;
                }
            }
        } while (changed);
    }

    private Set<String> getDef(Instruction inst) {
        Set<String> defs = new HashSet<>();

        if (inst instanceof AssignInstruction assign) {
            Element dest = assign.getDest();
            if (dest instanceof Operand op && !op.isLiteral()) {
                defs.add(op.getName());
            }
        }

        return defs;
    }

    private Set<String> getUse(Instruction inst) {
        Set<String> uses = new HashSet<>();

        if (inst instanceof AssignInstruction assign) {
            Instruction rhs = assign.getRhs();
            uses.addAll(getUsesFromInstruction(rhs));
        }

        if (inst instanceof CallInstruction call) {
            for (Element arg : call.getArguments()) {
                if (arg instanceof Operand op && !op.isLiteral()) {
                    uses.add(op.getName());
                }
            }
        }

        if (inst instanceof ReturnInstruction ret) {
            ret.getOperand().ifPresent(op -> {
                if (op instanceof Operand operand && !operand.isLiteral()) {
                    uses.add(operand.getName());
                }
            });
        }

        return uses;
    }

    // Helper to extract uses recursively from nested instructions (like BinaryOp)
    private Set<String> getUsesFromInstruction(Instruction inst) {
        Set<String> uses = new HashSet<>();

        if (inst instanceof BinaryOpInstruction binOp) {
            Element left = binOp.getLeftOperand();
            Element right = binOp.getRightOperand();

            if (left instanceof Operand op && !op.isLiteral()) uses.add(op.getName());
            if (right instanceof Operand op && !op.isLiteral()) uses.add(op.getName());

        } else if (inst instanceof UnaryOpInstruction unOp) {
            Element operand = unOp.getOperand();
            if (operand instanceof Operand op && !op.isLiteral()) uses.add(op.getName());

        } else if (inst instanceof CallInstruction call) {
            for (Element arg : call.getArguments()) {
                if (arg instanceof Operand op && !op.isLiteral()) uses.add(op.getName());
            }
        } else if (inst instanceof SingleOpInstruction single) {
            Element operand = single.getSingleOperand();
            if (operand instanceof Operand op && !op.isLiteral()) uses.add(op.getName());
        }

        return uses;
    }
}
