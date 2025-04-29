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

                // OUT[n] = U IN[s] para cada sucessor s
                Set<String> out = new HashSet<>();
                for (Node succ : Arrays.asList(inst.getSucc1(), inst.getSucc2())) {
                    if (succ instanceof Instruction && inMap.containsKey(succ)) {
                        out.addAll(inMap.get(succ));
                    }
                }

                Set<String> use = getUse(inst);
                Set<String> def = getDef(inst);

                // IN[n] = use[n] U (OUT[n] - def[n])
                Set<String> in = new HashSet<>(use);
                out.removeAll(def);
                in.addAll(out);

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
            if (dest instanceof Operand op) {
                defs.add(op.getName());
            }
        }
        return defs;
    }

    private Set<String> getUse(Instruction inst) {
        Set<String> uses = new HashSet<>();
        for (Element operand : inst.getOperands()) {
            if (operand instanceof Operand op && !op.isLiteral()) {
                uses.add(op.getName());
            }
        }
        return uses;
    }
}
