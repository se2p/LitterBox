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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingInitialization;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.cfg.*;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysisBuilder;
import de.uni_passau.fim.se2.litterbox.dataflow.InitialDefinitionTransferFunction;
import de.uni_passau.fim.se2.litterbox.dataflow.LivenessTransferFunction;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Injects the {@link MissingInitialization} bug by removing initialization statements
 * from the start of GreenFlag scripts.
 *
 * <p>
 * <b>Bug Pattern:</b> A sprite/stage lacks proper initialization at the start of the program,
 * causing state to persist between runs.
 *
 * <p>
 * <b>Preconditions for injection:</b>
 * <ol>
 *   <li>The attribute must be USED (read) later in the script</li>
 *   <li>No other GreenFlag script may initialize the same attribute</li>
 *   <li>No later unconditional definition may "cover" the use</li>
 * </ol>
 *
 * @see MissingInitialization
 */
public class MissingInitializationInjector extends AbstractBugInjector {

    private ControlFlowGraph globalCfg;
    private DataflowAnalysis<Use> globalLiveness;
    private DataflowAnalysis<Definition> globalInitialDefinitions;

    private final InitialDefinitionTransferFunction initialDefinitionTransferFunction =
            new InitialDefinitionTransferFunction();

    private final IssueFinder issueFinder = new MissingInitialization();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        // Build global CFG and compute liveness once for the original program
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        program.accept(visitor);
        this.globalCfg = visitor.getControlFlowGraph();

        DataflowAnalysisBuilder<Use> useBuilder = new DataflowAnalysisBuilder<>(globalCfg);
        this.globalLiveness = useBuilder.withBackward().withMay()
                .withTransferFunction(new LivenessTransferFunction()).build();
        globalLiveness.applyAnalysis();

        DataflowAnalysisBuilder<Definition> initBuilder = new DataflowAnalysisBuilder<>(globalCfg);
        this.globalInitialDefinitions = initBuilder.withBackward().withMay()
                .withTransferFunction(initialDefinitionTransferFunction).build();
        globalInitialDefinitions.applyAnalysis();

        return super.inject(program);
    }

    @Override
    public void visit(Script script) {
        if (script.getEvent() instanceof GreenFlag) {
            processGreenFlagScript(script);
        }
        super.visit(script);
    }

    /**
     * Processes a GreenFlag script, creating mutations for each removable initialization.
     */
    private void processGreenFlagScript(Script script) {
        List<Stmt> statements = script.getStmtList().getStmts();

        for (int i = 0; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);
            boolean isInit = isInitialDefinitionStatement(stmt);

            if (!isInit) {
                break; // Stop at first non-initialization statement
            }

            if (canRemoveInitialization(script, statements, i)) {
                final int index = i;
                modifyClonedNode(script.getStmtList(), clonedStmtList ->
                        clonedStmtList.getStmts().remove(index)
                );
            }
        }
    }

    private boolean canRemoveInitialization(Script script, List<Stmt> statements, int index) {
        Stmt stmt = statements.get(index);
        Set<Definition> definitions = getDefinitions(stmt);

        // Must not be already initialized by an earlier statement in the same script
        if (isInitializedEarlierInScript(statements, index, definitions)) {
            return false;
        }

        // Must not be initialized by another script
        if (isInitializedByOtherScript(script, definitions)) {
            return false;
        }

        // Must not have an unconditional definition later in the same script
        // (which would provide a backup initialization that the finder would detect)
        if (isDefinedLaterInScript(statements, index, definitions)) {
            return false;
        }

        // Must be live at the point immediately after the initialization statement.
        // This ensures that removing the initialization makes it live at the entry point.
        Optional<CFGNode> nodeOpt = globalCfg.getNode(stmt);
        if (nodeOpt.isEmpty()) {
            return false;
        }

        Set<Use> liveOut = globalLiveness.getDataflowFacts(nodeOpt.get());
        for (Definition def : definitions) {
            // Skip cloud variables as they are skipped by the finder
            if (def.getDefinable() instanceof Variable variable && variable.getIdentifier().toString().contains("☁")) {
                continue;
            }

            // Skip visibility attributes as they are skipped by the finder
            if (def.getDefinable() instanceof Attribute attribute
                    && attribute.getAttributeType().equals(Attribute.AttributeType.VISIBILITY)) {
                continue;
            }

            if (liveOut.stream().anyMatch(u -> u.getDefinable().equals(def.getDefinable()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any of the target definitions are already initialized earlier.
     */
    private boolean isInitializedEarlierInScript(List<Stmt> statements, int currentIndex, Set<Definition> targets) {
        for (int i = 0; i < currentIndex; i++) {
            Stmt stmt = statements.get(i);
            if (!isInitialDefinitionStatement(stmt)) {
                break;
            }
            Set<Definition> earlierDefs = getDefinitions(stmt);
            Set<Defineable> earlierDefineables = earlierDefs.stream()
                    .map(Definition::getDefinable)
                    .collect(Collectors.toSet());
            for (Definition target : targets) {
                if (earlierDefineables.contains(target.getDefinable())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if any of the target definitions are defined elsewhere in the same script.
     * A later definition would serve as a backup initialization that the finder would detect.
     * This checks all statements in the script, including those nested in loops and conditionals.
     *
     * <p>
     * Note: For statements inside loops, we don't filter by isInitialDefinitionStatement because
     * the finder's may-analysis considers any definition path as valid, even if the definition
     * also uses the variable (like IfOnEdgeBounce which uses rotation before defining it).
     * For top-level statements, we apply the filter to avoid false positives from definitions
     * that are side effects of other operations (like move steps defining position).
     */
    private boolean isDefinedLaterInScript(List<Stmt> statements, int currentIndex, Set<Definition> targets) {
        Stmt currentStmt = statements.get(currentIndex);
        // Collect target Defineables for equals comparison
        Set<Defineable> targetDefineables = targets.stream()
                .map(Definition::getDefinable)
                .collect(Collectors.toSet());

        ASTNode targetScript = targets.iterator().next().getDefinitionSource().getScriptOrProcedure();

        // Check all nodes in the global CFG for definitions of the same variables
        // that belong to the same script but are different statements
        for (CFGNode node : globalCfg.getNodes()) {
            // Skip the current statement
            if (node.getASTNode() == currentStmt) {
                continue;
            }

            // For top-level statements (not inside loops), apply isInitialDefinitionStatement filter
            // For loop-nested statements, skip this filter since may-analysis considers them valid
            if (node.getASTNode() instanceof Stmt nodeStmt) {
                boolean isInsideLoop = isInsideLoop(nodeStmt);
                if (!isInsideLoop && !isInitialDefinitionStatement(nodeStmt)) {
                    continue;
                }
            }

            // Check if this node defines any of the target variables
            for (Definition nodeDef : node.getDefinitions()) {
                Defineable nodeDefDefineable = nodeDef.getDefinable();

                // Check by equals for same attribute type/name
                if (targetDefineables.contains(nodeDefDefineable)
                        && nodeDef.getDefinitionSource().getScriptOrProcedure() == targetScript) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a statement is inside a loop (RepeatForever, Repeat, Until).
     */
    private boolean isInsideLoop(Stmt stmt) {
        ASTNode parent = stmt.getParentNode();
        while (parent != null) {
            if (parent instanceof RepeatForeverStmt
                    || parent instanceof RepeatTimesStmt
                    || parent instanceof UntilStmt) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    private boolean isInitialDefinitionStatement(Stmt stmt) {
        Optional<CFGNode> nodeOpt = globalCfg.getNode(stmt);
        if (nodeOpt.isEmpty()) {
            return false;
        }
        CFGNode node = nodeOpt.get();
        // A statement is an initial definition if it generates a definition that is NOT killed by a use
        // in the same node.
        Set<Definition> defs = node.getDefinitions();
        Set<Definition> initialDefs = initialDefinitionTransferFunction.apply(node, Collections.emptySet());
        return !initialDefs.isEmpty() && defs.size() == initialDefs.size();
    }

    private Set<Definition> getDefinitions(Stmt stmt) {
        Optional<CFGNode> nodeOpt = globalCfg.getNode(stmt);
        return nodeOpt.map(CFGNode::getDefinitions).orElse(Collections.emptySet());
    }

    private boolean isInitializedByOtherScript(Script currentScript, Set<Definition> definitions) {
        // A variable is initialized by another script if it reaches the global entry point
        // from a script other than the current one.
        Set<Definition> initialDefinitions = globalInitialDefinitions.getDataflowFacts(globalCfg.getEntryNode());
        Set<Defineable> targetDefineables = definitions.stream()
                .map(Definition::getDefinable)
                .collect(Collectors.toSet());

        return initialDefinitions.stream()
                .filter(d -> targetDefineables.contains(d.getDefinable()))
                .filter(d -> d.getDefinitionSource().getScriptOrProcedure() instanceof Script)
                .anyMatch(d -> d.getDefinitionSource().getScriptOrProcedure() != currentScript);
    }
}
