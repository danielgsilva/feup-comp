package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.Instruction;
import org.specs.comp.ollir.inst.SingleOpInstruction;

import java.util.*;

public class RegisterAllocation {

    private final int maxRegs;
    private final InterferenceGraph graph;
    private final Map<String, Integer> regAssignment = new HashMap<>();

    public RegisterAllocation(InterferenceGraph graph, int maxRegs) {
        this.graph = graph;
        this.maxRegs = maxRegs;
    }

    public Map<String, Integer> assignRegisters(Method method) {

        Stack<String> coloringOrder = new Stack<>();
        Set<String> processed = new HashSet<>();

        Map<String, Set<String>> simplifiedGraph = new HashMap<>();
        for (String variable : graph.getGraph().keySet()) {
            if (variable.equals("this") || isMethodParam(method, variable)) continue;
            simplifiedGraph.put(variable, new HashSet<>(graph.getNeighbors(variable)));
        }

        // Graph simplification phase
        while (processed.size() < simplifiedGraph.size()) {
            boolean removed = false;

            for (String variable : simplifiedGraph.keySet()) {
                if (processed.contains(variable)) continue;

                long neighborCount = simplifiedGraph.get(variable).stream()
                        .filter(neigh -> !processed.contains(neigh))
                        .count();

                if (neighborCount < maxRegs || maxRegs <= 0) {
                    coloringOrder.push(variable);
                    processed.add(variable);
                    removed = true;
                    break;
                }
            }

            if (!removed) {
                // Spill node (if needed)
                for (String variable : simplifiedGraph.keySet()) {
                    if (!processed.contains(variable)) {
                        coloringOrder.push(variable);
                        processed.add(variable);
                        break;
                    }
                }
            }
        }

        // Coloring phase
        while (!coloringOrder.isEmpty()) {
            String variable = coloringOrder.pop();
            Set<Integer> takenRegs = new HashSet<>();

            for (String neighbor : graph.getNeighbors(variable)) {
                if (regAssignment.containsKey(neighbor)) {
                    takenRegs.add(regAssignment.get(neighbor));
                }
            }

            int assignedReg = 0;
            while (takenRegs.contains(assignedReg)) assignedReg++;

            if (maxRegs > 0 && assignedReg >= maxRegs) {
                throw new RuntimeException("Exceeded max register count: " + maxRegs);
            }

            regAssignment.put(variable, assignedReg);
        }

        // Apply register offset for 'this' and parameters
        int baseOffset = method.isStaticMethod() ? 0 : 1;
        baseOffset += method.getParams().size();

        for (var entry : method.getVarTable().entrySet()) {
            String varName = entry.getKey();
            Descriptor descriptor = entry.getValue();

            if (descriptor.getScope() == VarScope.LOCAL && regAssignment.containsKey(varName)) {
                int offsetReg = regAssignment.get(varName) + baseOffset;
                descriptor.setVirtualReg(offsetReg);
                System.out.println("[RegisterAllocator] " + varName + " => r" + offsetReg);
            }
        }


        // Cleanup: Remove unused temporaries
        var unusedTemps = new ArrayList<String>();
        for (var entry : method.getVarTable().entrySet()) {
            String varName = entry.getKey();
            Descriptor descriptor = entry.getValue();

            if (varName.startsWith("tmp") && !regAssignment.containsKey(varName)) {
                unusedTemps.add(varName);
            }
        }
        for (String temp : unusedTemps) {
            method.getVarTable().remove(temp);
        }

        return regAssignment;
    }


    private boolean isMethodParam(Method method, String varName) {
        return method.getParams().stream()
                .filter(Operand.class::isInstance)
                .map(p -> ((Operand) p).getName())
                .anyMatch(name -> name.equals(varName));
    }
}