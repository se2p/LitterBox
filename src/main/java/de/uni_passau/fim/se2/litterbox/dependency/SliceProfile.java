/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.dependency;

import com.google.common.collect.Sets;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.cfg.*;

import java.util.*;
import java.util.stream.Collectors;

public class SliceProfile {

    private Map<Defineable, Set<Stmt>> profileMap = new LinkedHashMap<>();

    // TODO: equals/hashCode in ASTNodes ignores metadata and so identical statements are equal,
    //       thus we have to use the identity hashCode to ensure all statements are stored
    private Map<Stmt, Integer> statementNumbers = new IdentityHashMap<>();

    private int methodLength = 0;

    public Map<Defineable, Set<Stmt>> getProfileMap() {
        return profileMap;
    }

    public SliceProfile() {
    }

    public SliceProfile(ProgramDependenceGraph pdg) {
        calculate(pdg);
    }

    /*
     * Length = number of statements
     */
    public double getScriptLength() {
        return methodLength;
    }

    private void setScriptLength(ProgramDependenceGraph pdg) {
        this.methodLength = (int) pdg.getNodes().stream()
                .map(CFGNode::getASTNode)
                .filter(Stmt.class::isInstance)
                .count();
    }

    /*
     * Length = length of intersection of all slices
     */
    public double getIntersectionLength() {
        Set<Stmt> intersection = new HashSet<>();
        boolean first = true;
        for (Set<Stmt> profile : profileMap.values()) {
            if (first) {
                intersection.addAll(profile);
                first = false;
            } else {
                intersection.retainAll(profile);
            }
        }
        return intersection.size();
    }

    /*
     * Average variable union slice length divided by the method length
     */
    public double getCoverage() {
        if (profileMap.isEmpty()) {
            return 0.0;
        }

        double coverage = 0.0;
        for (Set<Stmt> slice : profileMap.values()) {
            coverage += getScriptLength(slice) / getScriptLength();
        }

        coverage /= profileMap.size();
        return coverage;
    }

    /*
     * Average ratio of the length of the intersection of variable union slices to the length of each slice
     */
    public double getOverlap() {
        // 1/numvariables * sum_variables (slice_intersection_length / slice_length)
        if (profileMap.isEmpty()) {
            return 0.0;
        }

        double coverage = 0.0;
        double intersectionLength = getIntersectionLength();
        for (Set<Stmt> slice : profileMap.values()) {
            int sliceLength = getScriptLength(slice);
            if (sliceLength > 0) {
                coverage += intersectionLength / sliceLength;
            }
        }

        coverage /= profileMap.size();
        return coverage;
    }

    /*
     * Ratio of the length of the intersection of variable union slices to the method length
     */
    public double getTightness() {
        // slice_intersection_length / method_length
        if (getScriptLength() == 0) {
            return 0.0;
        }

        return getIntersectionLength() / getScriptLength();
    }

    private int getScriptLength(Set<Stmt> slice) {
        return slice.size();
    }

    public void calculate(ProgramDependenceGraph pdg) {
        calculateStatementToIndexMap(pdg.getCFG());
        profileMap.clear();
        setScriptLength(pdg);

        Set<Defineable> defineables = getDefineables(pdg.getCFG());
        for (Defineable defineable : defineables) {
            Set<Stmt> slice = new LinkedHashSet<>();
            Set<Stmt> ref = getReferencingStatements(pdg.getCFG(), defineable);
            Set<Stmt> def = getDefiningStatements(pdg.getCFG(), defineable);

            while (!ref.isEmpty() || !def.isEmpty()) {
                Optional<Stmt> bwCriterion = getLast(ref);
                Set<Stmt> bwSlice;
                if (bwCriterion.isPresent()) {
                    bwSlice = pdg.backwardSlice(bwCriterion.get());
                } else {
                    bwSlice = new LinkedHashSet<>();
                }

                Optional<Stmt> fwCriterion;
                if (bwSlice.isEmpty()) {
                    fwCriterion = getFirst(Sets.union(def, ref));
                } else {
                    fwCriterion = getFirst(Sets.intersection(Sets.union(def, ref), bwSlice));
                }

                Set<Stmt> fwSlice;
                if (fwCriterion.isPresent()) {
                    fwSlice = pdg.forwardSlice(fwCriterion.get());
                } else {
                    fwSlice = new LinkedHashSet<>();
                }
                Set<Stmt> unionSlice = Sets.union(fwSlice, bwSlice);

                ref.removeAll(unionSlice);
                def.removeAll(unionSlice);
                slice.addAll(unionSlice);
            }
            profileMap.put(defineable, slice);
        }
    }

    private void calculateStatementToIndexMap(ControlFlowGraph cfg) {
        statementNumbers.clear();
        int numStatement = 0;
        for (CFGNode node : cfg.getNodes()) {
            if (node.getASTNode() instanceof Stmt stmt) {
                statementNumbers.put(stmt, numStatement++);
                assert (statementNumbers.size() == numStatement);
            }
        }
    }

    private Optional<Stmt> getFirst(Set<Stmt> statements) {
        return statements.stream().min(Comparator.comparingInt(s -> statementNumbers.get(s)));
    }

    private Optional<Stmt> getLast(Set<Stmt> statements) {
        return statements.stream().max(Comparator.comparingInt(s -> statementNumbers.get(s)));
    }

    private Set<Stmt> getDefiningStatements(ControlFlowGraph cfg, Defineable defineable) {
        return cfg.getNodes().stream().filter(n -> n.defines(defineable))
                .map(CFGNode::getASTNode)
                .filter(Stmt.class::isInstance)
                .map(Stmt.class::cast)
                .collect(Collectors.toSet());
    }

    private Set<Stmt> getReferencingStatements(ControlFlowGraph cfg, Defineable defineable) {
        return cfg.getNodes().stream().filter(n -> n.uses(defineable))
                .map(CFGNode::getASTNode)
                .filter(Stmt.class::isInstance)
                .map(Stmt.class::cast)
                .collect(Collectors.toSet());
    }

    private Set<Defineable> getDefineables(ControlFlowGraph cfg) {
        Set<Defineable> uses = cfg.getUses().stream().map(Use::getDefinable).collect(Collectors.toSet());
        Set<Defineable> defs = cfg.getDefinitions().stream().map(Definition::getDefinable).collect(Collectors.toSet());
        return Sets.union(uses, defs);
    }
}
