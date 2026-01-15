/*
 * Copyright (C) 2019-2026 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.dataflow;

import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.DataflowFact;

import java.util.*;

public class DataflowAnalysis<T extends DataflowFact> {

    private final ControlFlowGraph cfg;

    private TransferFunction<T> transferFunction;

    private FlowDirection flowDirection;

    private JoinFunction<T> joinFunction;

    // The facts that are known about this node
    private final Map<CFGNode, Set<T>> dataflowFacts = new LinkedHashMap<>();

    // Store the last known value calculated for the out set to ensure fixpoint iteration
    private final Map<CFGNode, Set<T>> outFacts = new LinkedHashMap<>();

    public DataflowAnalysis(ControlFlowGraph cfg) {
        this.cfg = cfg;
    }

    public Set<T> getDataflowFacts(CFGNode node) {
        return Collections.unmodifiableSet(dataflowFacts.get(node));
    }

    public ControlFlowGraph getCFG() {
        return cfg;
    }

    // May forward: union of facts of predecessor
    // Must forward: intersection of facts of predecessor
    // May backward: union of facts of predecessor
    // Must backward: intersection of facts of predecessor
    private Set<T> in(CFGNode node) {
        Set<CFGNode> inNodes = flowDirection.getInNodes(cfg, node);
        List<CFGNode> sortedInNodes = new ArrayList<>(inNodes);
        sortedInNodes.sort(Comparator.comparingInt(CFGNode::getId));

        Set<Set<T>> inputFacts = new LinkedHashSet<>();
        for (CFGNode n : sortedInNodes) {
            inputFacts.add(outFacts.get(n));
        }
        return joinFunction.apply(inputFacts);
    }

    //For “all paths” problems, first guess is “everything” (set of all possible values)
    public void initializeMust(Set<T> allFacts) {
        cfg.getNodes().forEach(n -> dataflowFacts.put(n, new LinkedHashSet<>(allFacts)));
    }

    //For “any path” problems, first guess is “nothing” (empty set) at each node
    public void initializeMay() {
        cfg.getNodes().forEach(n -> dataflowFacts.put(n, Collections.emptySet()));
    }

    void setTransferFunction(TransferFunction<T> function) {
        this.transferFunction = function;
    }

    void setFlowDirection(FlowDirection flowDirection) {
        this.flowDirection = flowDirection;
    }

    public void setJoinFunction(JoinFunction<T> join) {
        this.joinFunction = join;
    }

    public void applyAnalysis() {
        outFacts.putAll(dataflowFacts); // TODO: Is this correct?
        final Deque<CFGNode> workList = new ArrayDeque<>();
        List<CFGNode> initialNodes = new ArrayList<>();
        flowDirection.getInitialNodes(cfg).forEach(initialNodes::add);
        initialNodes.sort(Comparator.comparingInt(CFGNode::getId));
        initialNodes.forEach(workList::add);

        while (!workList.isEmpty()) {
            final CFGNode node = workList.poll();

            Set<T> lastOut = outFacts.get(node);
            Set<T> inFacts = in(node);
            dataflowFacts.put(node, inFacts);
            Set<T> newOut = transferFunction.apply(node, inFacts);

            if (!lastOut.equals(newOut)) {
                outFacts.put(node, newOut);
                List<CFGNode> outNodes = new ArrayList<>();
                flowDirection.getOutNodes(cfg, node).forEach(outNodes::add);
                outNodes.sort(Comparator.comparingInt(CFGNode::getId));
                workList.addAll(outNodes);
            }
        }
    }
}
