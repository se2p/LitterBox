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
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SwitchBackdropAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.PlaySoundUntilDone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetSoundEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetVolumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SwitchCostumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.GoToPosXY;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointTowards;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.cfg.Attribute;
import de.uni_passau.fim.se2.litterbox.cfg.Attribute.AttributeType;
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
                .withTransferFunction(new RedundantAssignmentTransferFunction(cfg))
                .build();

        analysis.applyAnalysis();
        return analysis;
    }

    private Set<Issue> findRedundantAssignments(ControlFlowGraph cfg, DataflowAnalysis<Use> analysis, Program program) {
        Set<Issue> issues = new LinkedHashSet<>();

        for (CFGNode node : cfg.getNodes()) {
            checkNode(node, analysis, program, issues, cfg);
        }

        return Collections.unmodifiableSet(issues);
    }

    private void checkNode(CFGNode node, DataflowAnalysis<Use> analysis, Program program, Set<Issue> issues,
                           ControlFlowGraph cfg) {
        Set<Definition> definitions = node.getDefinitions();
        if (definitions.isEmpty()) {
            return;
        }

        // Get LiveOut set (which is the input to the Transfer Function in Backward Analysis)
        Set<Defineable> liveVariables = getLiveVariables(node, analysis);

        for (Definition def : definitions) {
            checkDefinition(def, liveVariables, program, issues, cfg);
        }
    }

    private Set<Defineable> getLiveVariables(CFGNode node, DataflowAnalysis<Use> analysis) {
        return analysis.getDataflowFacts(node).stream()
                .map(Use::getDefinable)
                .collect(Collectors.toSet());
    }

    private void checkDefinition(Definition def, Set<Defineable> liveVariables, Program program, Set<Issue> issues,
                                 ControlFlowGraph cfg) {
        if (isDeadOnAllPaths(def, liveVariables)) {
            reportAlwaysRedundant(def, program, issues);
        } else {
            checkPathRedundancy(def, program, issues, cfg);
        }
    }

    private boolean isDeadOnAllPaths(Definition def, Set<Defineable> liveVariables) {
        return !liveVariables.contains(def.getDefinable());
    }

    private void reportAlwaysRedundant(Definition def, Program program, Set<Issue> issues) {
        ASTNode statement = def.getDefinitionSource().getASTNode();
        if (statement != null && isDynamicAssignment(statement)) {
            return;
        }
        createAndAddIssue(def, program, issues, HINT_ALWAYS);
    }

    private void checkPathRedundancy(Definition def, Program program, Set<Issue> issues, ControlFlowGraph cfg) {
        new PathRedundancyChecker(def, program, issues, cfg).check();
    }

    private class PathRedundancyChecker {
        private final Definition startDef;
        private final Program program;
        private final Set<Issue> issues;
        private final ControlFlowGraph cfg;
        private final Defineable variable;
        private final Set<CFGNode> visited = new HashSet<>();
        private final Set<CFGNode> reportedOverwriters = new HashSet<>();
        private final java.util.Queue<CFGNode> queue = new java.util.ArrayDeque<>();

        public PathRedundancyChecker(Definition startDef, Program program, Set<Issue> issues, ControlFlowGraph cfg) {
            this.startDef = startDef;
            this.program = program;
            this.issues = issues;
            this.cfg = cfg;
            this.variable = startDef.getDefinable();
        }

        public void check() {
            CFGNode startNode = startDef.getDefinitionSource();
            queue.addAll(new ForwardFlowDirection().getOutNodes(cfg, startNode));

            while (!queue.isEmpty()) {
                CFGNode current = queue.poll();
                if (!visited.add(current)) {
                    continue;
                }

                if (isVariableUsedAt(current)) {
                    continue;
                }

                if (isVariableRedefinedAt(current)) {
                    reportPossibleRedundancy(current);
                    continue; // Stop tracing this path
                }

                queue.addAll(new ForwardFlowDirection().getOutNodes(cfg, current));
            }
        }

        private boolean isVariableUsedAt(CFGNode node) {
            if (isRenderingStatement(node.getASTNode()) && isVisibleAttribute(variable)) {
                return true;
            }
            for (Use use : node.getUses()) {
                if (use.getDefinable().equals(variable)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isVariableRedefinedAt(CFGNode node) {
            return node.defines(variable);
        }

        private void reportPossibleRedundancy(CFGNode overwritingNode) {
            if (reportedOverwriters.contains(overwritingNode)) {
                return;
            }

            ASTNode stmt = overwritingNode.getASTNode();
            if (stmt != null && isDynamicAssignment(stmt)) {
                return;
            }

            Definition overwriteDef = new Definition(overwritingNode, variable);
            createAndAddIssue(overwriteDef, program, issues, HINT_POSSIBLE);
            reportedOverwriters.add(overwritingNode);
        }

        private boolean isRenderingStatement(ASTNode node) {
            if (node == null) {
                return false;
            }
            return node instanceof WaitSeconds
                    || node instanceof BroadcastAndWait
                    || node instanceof AskAndWait
                    || node instanceof SwitchBackdropAndWait
                    || node instanceof PlaySoundUntilDone
                    || node instanceof Say
                    || node instanceof SayForSecs
                    || node instanceof Think
                    || node instanceof ThinkForSecs;
        }

        private boolean isVisibleAttribute(Defineable variable) {
            if (variable instanceof Attribute attr) {
                AttributeType type = attr.getAttributeType();
                return type == AttributeType.POSITION_X
                        || type == AttributeType.POSITION_Y
                        || type == AttributeType.ROTATION
                        || type == AttributeType.ROTATION_STYLE
                        || type == AttributeType.SIZE
                        || type == AttributeType.VISIBILITY
                        || type == AttributeType.GRAPHIC_EFFECT
                        || type == AttributeType.LAYER
                        || type == AttributeType.COSTUME
                        || type == AttributeType.BACKDROP;
            }
            return false;
        }
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
        if (statement instanceof GoToPosXY stmt) {
            return !isConstant(stmt.getX()) || !isConstant(stmt.getY());
        }
        if (statement instanceof PointInDirection stmt) {
            return !isConstant(stmt.getDirection());
        }
        if (statement instanceof SetSizeTo stmt) {
            return !isConstant(stmt.getPercent());
        }
        if (statement instanceof SetGraphicEffectTo stmt) {
            return !isConstant(stmt.getValue());
        }
        if (statement instanceof SetSoundEffectTo stmt) {
            return !isConstant(stmt.getValue());
        }
        if (statement instanceof SetVolumeTo stmt) {
            return !isConstant(stmt.getVolumeValue());
        }
        if (statement instanceof SwitchCostumeTo stmt) {
            return isDynamic(stmt.getCostumeChoice());
        }
        if (statement instanceof SwitchBackdrop stmt) {
            return isDynamic(stmt.getElementChoice());
        }
        if (statement instanceof SwitchBackdropAndWait stmt) {
            return isDynamic(stmt.getElementChoice());
        }
        if (statement instanceof GoToPos stmt) {
            return isDynamic(stmt.getPosition());
        }
        if (statement instanceof PointTowards stmt) {
            return isDynamic(stmt.getPosition());
        }
        return false;
    }

    private boolean isDynamic(ElementChoice choice) {
        if (choice instanceof WithExpr withExpr) {
            return !isConstant(withExpr.getExpression());
        }
        // Random, Next, Prev are all dynamic interactions or values
        return choice instanceof Random || choice instanceof Next || choice instanceof Prev;
    }

    private boolean isDynamic(Position pos) {
        if (pos instanceof FromExpression fromExpression) {
            return !isConstant(fromExpression.getStringExpr());
        }
        // RandomPos, MousePos are dynamic
        return pos instanceof RandomPos || pos instanceof MousePos;
    }

    private boolean isConstant(Expression expr) {
        if (isConstantLiteral(expr)) {
            return true;
        }
        return areAllChildrenConstant(expr);
    }

    private boolean isConstantLiteral(Expression expr) {
        return expr instanceof BoolLiteral
                || expr instanceof ColorLiteral
                || expr instanceof NumberLiteral
                || expr instanceof StringLiteral;
    }

    private boolean areAllChildrenConstant(Expression expr) {
        if (expr.getChildren() == null) {
            return true;
        }
        for (ASTNode child : expr.getChildren()) {
            if (child instanceof Expression childExpr) {
                if (!isConstant(childExpr)) {
                    return false;
                }
            } else {
                return false;
            }
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
