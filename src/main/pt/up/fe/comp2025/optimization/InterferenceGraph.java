package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.inst.Instruction;

import java.util.*;

public class InterferenceGraph {

    private final Map<String, Set<String>> graph = new HashMap<>();

    /**
     * Adds a node (variable) to the graph if not already present.
     */
    public void addNode(String var) {
        graph.putIfAbsent(var, new HashSet<>());
    }

    /**
     * Adds an undirected edge between two variables (interference).
     */
    public void addEdge(String var1, String var2) {
        if (var1.equals(var2)) return; // no self edges

        addNode(var1);
        addNode(var2);

        graph.get(var1).add(var2);
        graph.get(var2).add(var1);
    }

    /**
     * Builds the interference graph from liveness analysis.
     */
    public static InterferenceGraph fromLiveness(Map<Instruction, Set<String>> outMap) {
        InterferenceGraph ig = new InterferenceGraph();

        for (var entry : outMap.entrySet()) {
            Instruction inst = entry.getKey();
            Set<String> liveOut = entry.getValue();

            // def = variable written by this instruction
            Set<String> def = new HashSet<>();
            if (inst instanceof org.specs.comp.ollir.inst.AssignInstruction assign) {
                var dest = assign.getDest();
                if (dest instanceof org.specs.comp.ollir.Operand op && !op.isLiteral()) {
                    def.add(op.getName());
                }
            }

            for (String d : def) {
                for (String o : liveOut) {
                    ig.addEdge(d, o);
                }
            }
        }

        return ig;
    }

    public Map<String, Set<String>> getGraph() {
        return graph;
    }

    public Set<String> getNeighbors(String var) {
        return graph.getOrDefault(var, Collections.emptySet());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Interference Graph:\n");
        for (var entry : graph.entrySet()) {
            sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
