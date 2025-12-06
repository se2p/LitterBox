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
import de.uni_passau.fim.se2.litterbox.cfg.Defineable;
import de.uni_passau.fim.se2.litterbox.cfg.Definition;
import de.uni_passau.fim.se2.litterbox.cfg.Use;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysisBuilder;
import de.uni_passau.fim.se2.litterbox.dataflow.ForwardFlowDirection;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;


import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;

import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;

import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.cfg.Attribute;
import de.uni_passau.fim.se2.litterbox.cfg.Attribute.AttributeType;


public class RedundantAssignment extends AbstractIssueFinder {

    public static final String NAME = "redundant_assignment";
    public static final String HINT_ALWAYS = "redundant_assignment_always";
    public static final String HINT_POSSIBLE = "redundant_assignment_possible";

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
            checkNode(node, analysis, program, issues, cfg);
        }
        
        return Collections.unmodifiableSet(issues);
    }
    
    private void checkNode(CFGNode node, DataflowAnalysis<Use> analysis, Program program, Set<Issue> issues, ControlFlowGraph cfg) {
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
            checkDefinition(def, liveVariables, program, issues, cfg);
        }
    }
    
    private void checkDefinition(Definition def, Set<Object> liveVariables, Program program, Set<Issue> issues, ControlFlowGraph cfg) {
        // Case 1: Overwritten on ALL Paths
        // If the defined variable is NOT in LiveOut, it means it is dead on all paths.
        if (!liveVariables.contains(def.getDefinable())) {
            ASTNode statement = def.getDefinitionSource().getASTNode();
            if (statement != null && isDynamicAssignment(statement)) {
                 return;
            }
            createAndAddIssue(def, program, issues, HINT_ALWAYS);
        } else {
            // Case 2: Overwritten on SOME Paths
            checkPathRedundancy(def, program, issues, cfg);
        }
    }


    private void checkPathRedundancy(Definition def, Program program, Set<Issue> issues, ControlFlowGraph cfg) {
        CFGNode startNode = def.getDefinitionSource();
        Object variable = def.getDefinable();

        // BFS to find overwriting definitions without intervening uses
        Set<CFGNode> visited = new HashSet<>();
        java.util.Queue<CFGNode> queue = new java.util.ArrayDeque<>();
        
        Collection<CFGNode> successors = new ForwardFlowDirection().getOutNodes(cfg, startNode);
        queue.addAll(successors);
        
        Set<CFGNode> reportedOverwriters = new HashSet<>();

        while (!queue.isEmpty()) {
            CFGNode current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }

            // Check if 'variable' is Used at this node
            boolean used = false;
            if (isRenderingStatement(current.getASTNode()) && isVisibleAttribute(variable)) {
                used = true;
            }
            if (!used) {
                for (Use use : current.getUses()) {
                    if (use.getDefinable().equals(variable)) {
                        used = true;
                        break;
                    }
                }
            }
            
            if (used) {
                continue;
            }

            // Check if 'variable' is Re-defined (Overwritten) at this node
            if (current.defines((Defineable) variable)) {
                // Found an overwrite without use!
                if (!reportedOverwriters.contains(current)) {
                    ASTNode stmt = current.getASTNode();
                    if (stmt == null || !isDynamicAssignment(stmt)) {
                         Definition overwriteDef = new Definition(current, (Defineable) variable);
                         createAndAddIssue(overwriteDef, program, issues, HINT_POSSIBLE);
                         reportedOverwriters.add(current);
                    }
                }
                // Stop tracing this path (value killed)
                continue;
            }
            
            queue.addAll(new ForwardFlowDirection().getOutNodes(cfg, current));
        }
    }

    private boolean isRenderingStatement(ASTNode node) {
        if (node == null) return false;
        return node instanceof WaitSeconds ||
               node instanceof BroadcastAndWait ||
               node instanceof AskAndWait ||
               node instanceof SwitchBackdropAndWait ||
               node instanceof PlaySoundUntilDone ||
               node instanceof Say ||
               node instanceof SayForSecs ||
               node instanceof Think ||
               node instanceof ThinkForSecs;
    }

    private boolean isVisibleAttribute(Object variable) {
        if (variable instanceof Attribute attr) {
            AttributeType type = attr.getAttributeType();
            return type == AttributeType.POSITION_X ||
                   type == AttributeType.POSITION_Y ||
                   type == AttributeType.ROTATION ||
                   type == AttributeType.ROTATION_STYLE ||
                   type == AttributeType.SIZE ||
                   type == AttributeType.VISIBILITY ||
                   type == AttributeType.GRAPHIC_EFFECT ||
                   type == AttributeType.LAYER ||
                   type == AttributeType.COSTUME ||
                   type == AttributeType.BACKDROP;
        }
        return false;
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

    private boolean isDynamicAssignment(ASTNode statement) {
        if (statement instanceof SetVariableTo stmt) {
            return !isConstant(stmt.getExpr());
        }
        if (statement instanceof SetXTo stmt) {
            return !isConstant(stmt.getNum());
        }
        if (statement instanceof SetYTo stmt) {
            return !isConstant(stmt.getNum());
        }
        return false;
    }

    private boolean isConstant(Expression expr) {
        if (expr instanceof BoolLiteral || expr instanceof ColorLiteral || expr instanceof NumberLiteral || expr instanceof StringLiteral) {
            return true;
        }
        // If it's not a literal, check if it's composed only of literals (e.g. arithmetic on constants)
        // This is a simplification; we assume non-literals are dynamic for now (e.g. DataExpr)
        // But (1) + (1) is constant. We should ideally check recursively.
        // However, standard Scratch constants are usually parsed as Literals or simple trees.
        // Safer to assume anything NOT a literal is dynamic, except maybe lists of literals?
        // Let's iterate children.
        if (expr.getChildren() != null) {
             for (ASTNode child : expr.getChildren()) {
                 if (child instanceof Expression childExpr) {
                     if (!isConstant(childExpr)) {
                         return false;
                     }
                 } else {
                     // If child is not an expression (e.g. a block part?), assume dynamic to be safe,
                     // unless we know it's safe.
                     // But most expressions children are expressions.
                     return false;
                 }
             }
             return true; 
        }
        return true; 
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
}
