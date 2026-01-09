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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysisBuilder;
import de.uni_passau.fim.se2.litterbox.dataflow.ForwardFlowDirection;
import de.uni_passau.fim.se2.litterbox.dataflow.TransferFunction;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

public class RedundantAssignment extends AbstractIssueFinder {

    public static final String NAME = "redundant_assignment";
    public static final String HINT_ALWAYS = "redundant_assignment_always";
    public static final String HINT_POSSIBLE = "redundant_assignment_possible";

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);

        ControlFlowGraph cfg = buildCFG(program);
        DataflowAnalysis<Defineable> livenessAnalysis = performLivenessAnalysis(cfg);
        DataflowAnalysis<ReachingDefinitionState> reachingAnalysis = performReachingAnalysis(cfg);

        return findRedundantAssignments(cfg, livenessAnalysis, reachingAnalysis, program);
    }

    private ControlFlowGraph buildCFG(Program program) {
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        program.accept(visitor);
        return visitor.getControlFlowGraph();
    }

    private DataflowAnalysis<Defineable> performLivenessAnalysis(ControlFlowGraph cfg) {
        // We use a custom Transfer Function that treats Exit nodes as "Using All Variables"
        // to distinguish "Overwritten" from "Unused"
        DataflowAnalysisBuilder<Defineable> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Defineable> analysis = builder.withBackward()
                .withMay()
                .withTransferFunction(new RedundantAssignmentTransferFunction(cfg))
                .build();

        analysis.applyAnalysis();
        return analysis;
    }

    private DataflowAnalysis<ReachingDefinitionState> performReachingAnalysis(ControlFlowGraph cfg) {
        // Build Forward Analysis to track reaching definitions and their usage status
        DataflowAnalysisBuilder<ReachingDefinitionState> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<ReachingDefinitionState> analysis = builder.withForward()
                .withMay()
                .withTransferFunction(new ReachingDefinitionsTransferFunction())
                .build();

        analysis.applyAnalysis();
        return analysis;
    }

    private Set<Issue> findRedundantAssignments(
            ControlFlowGraph cfg,
            DataflowAnalysis<Defineable> livenessAnalysis,
            DataflowAnalysis<ReachingDefinitionState> reachingAnalysis,
            Program program
    ) {
        Set<Issue> issues = new LinkedHashSet<>();
        Set<Definition> alwaysRedundantDefs = new HashSet<>();

        // Pass 1: Check Always Redundant (Dead Code)
        for (CFGNode node : cfg.getNodes()) {
            Set<Definition> definitions = node.getDefinitions();
            if (definitions.isEmpty()) {
                continue;
            }
            Set<Defineable> liveVariables = livenessAnalysis.getDataflowFacts(node);
            for (Definition def : definitions) {
                if (!liveVariables.contains(def.getDefinable())) {
                    if (reportAlwaysRedundant(def, program, issues)) {
                        alwaysRedundantDefs.add(def);
                    }
                }
            }
        }

        // Pass 2: Check Possible Redundancy (Reaching Definitions overwritten here)
        for (CFGNode node : cfg.getNodes()) {
            Set<Definition> definitions = node.getDefinitions();
            if (definitions.isEmpty()) {
                continue;
            }
            Set<ReachingDefinitionState> reachingStates = reachingAnalysis.getDataflowFacts(node);
            for (Definition def : definitions) {
                checkPossibleRedundancy(def, reachingStates, program, issues, alwaysRedundantDefs);
            }
        }

        return Collections.unmodifiableSet(issues);
    }

    private void checkPossibleRedundancy(Definition currentDef,
                                         Set<ReachingDefinitionState> reachingStates,
                                         Program program,
                                         Set<Issue> issues,
                                         Set<Definition> alwaysRedundantDefs) {
        Defineable variable = currentDef.getDefinable();
        CFGNode currentNode = currentDef.getDefinitionSource();

        // If the variable is USED in the same node (e.g. "change x by 1"), then the reaching definition was used!
        for (Use use : currentNode.getUses()) {
            if (use.getDefinable().equals(variable)) {
                return;
            }
        }

        for (ReachingDefinitionState state : reachingStates) {
            if (state.isUsed()) {
                continue; // We only care about unused definitions
            }
            Definition reached = state.definition();

            if (alwaysRedundantDefs.contains(reached)) {
                continue;
            }
            if (reached.getDefinable().equals(variable)) {
                // Refinement: If this overwrite point is reached by a definition that is unused on THIS path,
                // check if it was used on ANY incoming path.
                // Since the analysis merges facts (May Analysis), if it was used on any path,
                // reachingStates will contain ReachingDefinitionState(reached, true).
                if (reachingStates.contains(new ReachingDefinitionState(reached, true))) {
                    continue; // Suppress (Partially Dead)
                }

                createAndAddIssue(reached, program, issues, HINT_POSSIBLE);
            }
        }
    }

    private boolean reportAlwaysRedundant(Definition def, Program program, Set<Issue> issues) {
        createAndAddIssue(def, program, issues, HINT_ALWAYS);
        return true;
    }

    private void createAndAddIssue(Definition def, Program program, Set<Issue> issues, String hintKey) {
        ASTNode statement = def.getDefinitionSource().getASTNode();
        if (statement == null) {
            return;
        }

        ScriptEntity context = resolveContext(def, statement);
        if (context == null) {
            return;
        }

        Hint hint = Hint.fromKey(hintKey);
        hint.setParameter(Hint.HINT_VARIABLE, new HintPlaceholder.Defineable(def.getDefinable()));

        ActorDefinition actor = def.getDefinitionSource().getActor();

        Issue issue = new IssueBuilder()
                .withFinder(this)
                .withSeverity(IssueSeverity.MEDIUM)
                .withProgram(program)
                .withActor(actor)
                .withScriptOrProcedure(context)
                .withCurrentNode(statement)
                .withMetadata(statement.getMetadata())
                .withHint(hint)
                .build();

        issues.add(issue);
    }

    private ScriptEntity resolveContext(Definition def, ASTNode statement) {
        ASTNode contextNode = def.getDefinitionSource().getScriptOrProcedure();
        if (contextNode instanceof ScriptEntity scriptEntity) {
            return scriptEntity;
        }
        if (statement != null) {
            for (ASTNode current = statement.getParentNode(); current != null; current = current.getParentNode()) {
                if (current instanceof ScriptEntity scriptEntity) {
                    return scriptEntity;
                }
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        return Arrays.asList(HINT_ALWAYS, HINT_POSSIBLE);
    }

    /**
     * Custom Transfer Function for Liveness Analysis.
     *
     * <p>Treats all variables/attributes defined in this script as "Live" at exit nodes, ensuring that an assignment
     * is only redundant if it is overwritten before any use *and* before the end of the script.
     *
     * <p>Also treats interprocedural edge statements (broadcast, clone, call) as implicit uses of all variables.
     * This prevents false positives where a definition in one script is reported as redundant because it is
     * overwritten in a different script (reached via broadcast). The visual state change is observable before
     * control transfers to the other script.
     *
     * <p>Note: We track {@code Defineable} (variables) instead of {@code Use} objects.
     * Since we only care *if* a variable is used, not *where*, tracking the variable itself reduces
     * the size of the dataflow sets (O(|Variables|) vs O(|Uses|)), improving performance and convergence time.
     */
    private static class RedundantAssignmentTransferFunction implements TransferFunction<Defineable> {
        private final ControlFlowGraph cfg;
        private final Set<Defineable> allDefinedVariables;

        public RedundantAssignmentTransferFunction(ControlFlowGraph cfg) {
            this.cfg = cfg;
            this.allDefinedVariables = new HashSet<>();
            for (CFGNode cfgNode : cfg.getNodes()) {
                for (Definition def : cfgNode.getDefinitions()) {
                    this.allDefinedVariables.add(def.getDefinable());
                }
            }
        }

        @Override
        public Set<Defineable> apply(CFGNode node, Set<Defineable> inFacts) {
            Set<Defineable> outFacts = new HashSet<>(inFacts);

            if (isExitNode(node)) {
                outFacts.addAll(allDefinedVariables);
            }

            // Remove variables defined at this node (Kill)
            for (Definition def : node.getDefinitions()) {
                outFacts.remove(def.getDefinable());
            }

            // Add variables used at this node (Gen)
            for (Use use : node.getUses()) {
                outFacts.add(use.getDefinable());
            }

            // Add variables implicitly used by halting (Gen)
            if (isHaltingNode(node)) {
                outFacts.addAll(allDefinedVariables);
            }

            // Treat interprocedural edge statements as implicit uses
            // This prevents false positives where a definition before a broadcast/clone/call
            // is reported as redundant due to overwrites in a different script.
            if (isInterproceduralEdgeNode(node)) {
                outFacts.addAll(allDefinedVariables);
            }

            return outFacts;
        }

        private boolean isHaltingNode(CFGNode node) {
            return AstNodeUtil.containsHaltingStatement(node.getASTNode());
        }

        private boolean isExitNode(CFGNode node) {
            return new ForwardFlowDirection().getOutNodes(cfg, node).isEmpty();
        }

        /**
         * Checks if the node represents an interprocedural edge (broadcast, clone, or call).
         * These statements transfer control to another script/procedure, and any visual state
         * changes before them are observable.
         */
        private boolean isInterproceduralEdgeNode(CFGNode node) {
            ASTNode astNode = node.getASTNode();
            if (astNode == null) {
                return false;
            }
            return astNode instanceof Broadcast
                    || astNode instanceof BroadcastAndWait
                    || astNode instanceof CreateCloneOf
                    || astNode instanceof CallStmt;
        }
    }

    /**
     * Reaching Definitions Transfer Function that also tracks usage status.
     */
    private static class ReachingDefinitionsTransferFunction implements TransferFunction<ReachingDefinitionState> {

        @Override
        public Set<ReachingDefinitionState> apply(CFGNode node, Set<ReachingDefinitionState> inFacts) {
            Set<ReachingDefinitionState> outFacts = new HashSet<>();

            Set<Defineable> usedVariables = new HashSet<>();
            for (Use use : node.getUses()) {
                usedVariables.add(use.getDefinable());
            }

            Set<Defineable> definedVariables = new HashSet<>();
            for (Definition def : node.getDefinitions()) {
                definedVariables.add(def.getDefinable());
            }

            boolean isImplicitUse = isHaltingNode(node) || isInterproceduralEdgeNode(node);

            // Process IN facts
            for (ReachingDefinitionState state : inFacts) {
                // Kill if redefined
                if (definedVariables.contains(state.definition().getDefinable())) {
                    continue;
                }

                // Gen Use: If variable used, mark as used
                if (usedVariables.contains(state.definition().getDefinable())) {
                    outFacts.add(new ReachingDefinitionState(state.definition(), true));
                } else if (isImplicitUse) {
                    outFacts.add(new ReachingDefinitionState(state.definition(), true));
                } else {
                    outFacts.add(state);
                }
            }

            // Gen Definitions: Add new definitions as Unused
            for (Definition def : node.getDefinitions()) {
                outFacts.add(new ReachingDefinitionState(def, false));
            }

            return outFacts;
        }

        private boolean isHaltingNode(CFGNode node) {
            return AstNodeUtil.containsHaltingStatement(node.getASTNode());
        }

        /**
         * Checks if the node represents an interprocedural edge (broadcast, clone, or call).
         * These statements transfer control to another script/procedure, and any visual state
         * changes before them are observable.
         */
        private boolean isInterproceduralEdgeNode(CFGNode node) {
            ASTNode astNode = node.getASTNode();
            if (astNode == null) {
                return false;
            }
            return astNode instanceof Broadcast
                    || astNode instanceof BroadcastAndWait
                    || astNode instanceof CreateCloneOf
                    || astNode instanceof CallStmt;
        }
    }

    private record ReachingDefinitionState(Definition definition, boolean isUsed) implements DataflowFact {

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ReachingDefinitionState that = (ReachingDefinitionState) o;
            return isUsed == that.isUsed && definition.equals(that.definition);
        }

        @Override
        public String toString() {
            return definition.toString() + (isUsed ? "(USED)" : "(UNUSED)");
        }
    }
}
