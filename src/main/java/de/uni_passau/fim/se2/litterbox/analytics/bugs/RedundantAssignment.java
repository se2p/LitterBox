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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.HintPlaceholder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.cfg.Definition;
import de.uni_passau.fim.se2.litterbox.cfg.Use;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysisBuilder;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RedundantAssignment extends AbstractIssueFinder {

    public static final String NAME = "redundant_assignment";

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        
        ControlFlowGraph cfg = buildCFG(program);
        DataflowAnalysis<Use> analysis = performLivenessAnalysis(cfg, program);
        
        return findRedundantAssignments(cfg, analysis, program);
    }

    private ControlFlowGraph buildCFG(Program program) {
        ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
        program.accept(visitor);
        return visitor.getControlFlowGraph();
    }

    private DataflowAnalysis<Use> performLivenessAnalysis(ControlFlowGraph cfg, Program program) {
        // Build Liveness Analysis
        // We use a custom Transfer Function that treats Exit nodes as "Using All Variables"
        // to distinguish "Overwritten" from "Unused"
        DataflowAnalysisBuilder<Use> builder = new DataflowAnalysisBuilder<>(cfg);
        DataflowAnalysis<Use> analysis = builder.withBackward()
                .withMay()
                .withTransferFunction(new RedundantAssignmentTransferFunction(program, cfg))
                .build();
        
        analysis.applyAnalysis();
        return analysis;
    }

    private Set<Issue> findRedundantAssignments(ControlFlowGraph cfg, DataflowAnalysis<Use> analysis, Program program) {
        Set<Issue> issues = new LinkedHashSet<>();
        
        // Iterate over all nodes to find Redundant Assignments
        for (CFGNode node : cfg.getNodes()) {
            checkNode(node, analysis, program, issues);
        }
        
        return Collections.unmodifiableSet(issues);
    }
    
    private void checkNode(CFGNode node, DataflowAnalysis<Use> analysis, Program program, Set<Issue> issues) {
        Set<Definition> definitions = node.getDefinitions();
        if (definitions.isEmpty()) {
            return;
        }

        // Get LiveOut set (which is the input to the Transfer Function in Backward Analysis,
        // stored as dataflowFacts for the node)
        Set<Use> liveOut = analysis.getDataflowFacts(node);
        Set<Object> liveVariables = liveOut.stream()
                .map(Use::getDefinable)
                .collect(Collectors.toSet());

        for (Definition def : definitions) {
            checkDefinition(def, liveVariables, program, issues);
        }
    }
    
    private void checkDefinition(Definition def, Set<Object> liveVariables, Program program, Set<Issue> issues) {
        // If the defined variable is NOT in LiveOut, it means it is dead.
        // Since we forced "LiveAtExit" for all variables, being Dead implies
        // it is killed (overwritten) before reaching any Use or Exit.
        if (!liveVariables.contains(def.getDefinable())) {
            createAndAddIssue(def, program, issues);
        }
    }
    
    private void createAndAddIssue(Definition def, Program program, Set<Issue> issues) {
        ASTNode statement = def.getDefinitionSource().getASTNode();
        if (statement == null) {
            return;
        }

        ScriptEntity context = resolveContext(def, statement);
        
        if (context == null) {
             // If we can't find a context, we skip reporting to avoid errors.
             return; 
        }

        Hint hint = Hint.fromKey(NAME);
        hint.setParameter(Hint.HINT_VARIABLE, new HintPlaceholder.Defineable(def.getDefinable()));
        
        ActorDefinition actor = def.getDefinitionSource().getActor();
        
        Issue issue = new Issue(this, IssueSeverity.MEDIUM, program, actor, context, statement, statement.getMetadata(), hint) {};
        issues.add(issue);
    }
    
    private ScriptEntity resolveContext(Definition def, ASTNode statement) {
        ASTNode contextNode = def.getDefinitionSource().getScriptOrProcedure();
        
        if (contextNode instanceof ScriptEntity) {
            return (ScriptEntity) contextNode;
        }
        
        // fallback: traverse up to find ScriptEntity
        if (statement != null) {
            ASTNode current = statement.getParentNode();
            while (current != null) {
                if (current instanceof ScriptEntity) {
                    return (ScriptEntity) current;
                }
                current = current.getParentNode();
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
        return Collections.singletonList(NAME);
    }
}
