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
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ChangeGraphicEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.SetGraphicEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeSoundEffectBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeVolumeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetSoundEffectTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SetVolumeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ChangeSizeBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SetSizeTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeXBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.ChangeYBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.SetPenColorToColorStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnLeft;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.dataflow.TypeAnalysis;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.dataflow.TypeInference;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.dataflow.VariableTypeFact;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.Variable;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Detects type errors using dataflow analysis and type inference.
 */
public class TypeError extends AbstractIssueFinder {
    public static final String NAME = "type_error";
    public static final String WEIRD_DISTANCE = "type_error_weird_distance";

    private DataflowAnalysis<VariableTypeFact> analysis;
    private final Map<ASTNode, CFGNode> astToCfg = new HashMap<>();

    @Override
    public Set<Issue> check(Program program) {
        this.analysis = TypeAnalysis.analyze(program);
        this.astToCfg.clear();
        buildCFGMap(analysis.getCFG());
        return super.check(program);
    }

    private void buildCFGMap(ControlFlowGraph cfg) {
        for (CFGNode node : cfg.getNodes()) {
            if (node.getASTNode() != null) {
                astToCfg.put(node.getASTNode(), node);
            }
        }
    }

    private Function<Identifier, Type> createResolver(ASTNode node) {
        CFGNode cfgNode = null;
        ASTNode current = node;
        while (cfgNode == null && current != null) {
            if (astToCfg.containsKey(current)) {
                cfgNode = astToCfg.get(current);
            } else {
                current = current.getParentNode();
            }
        }

        if (cfgNode == null) {
             return id -> Type.ANY;
        }
        Set<VariableTypeFact> facts = analysis.getDataflowFacts(cfgNode);
        if (facts == null) return id -> Type.ANY;
        
        return identifier -> {
            for (VariableTypeFact fact : facts) {
                if (fact.getVariable() instanceof Variable v) {
                    if (idsEqual(v.getIdentifier(), identifier)) {
                        return fact.getType();
                    }
                }
            }
            return Type.BOTTOM;
        };
    }

    private boolean idsEqual(Identifier a, Identifier b) {
        if (a instanceof LocalIdentifier la && b instanceof LocalIdentifier lb) {
            return la.getName().equals(lb.getName());
        }
        return a.equals(b);
    }
    
    // --- Helper Methods ---

    private void checkType(ASTNode node, Type expected) {
        if (node instanceof Expression expr) {
             Type actual = TypeInference.inferType(expr, createResolver(node));
             
             if (!isCompatible(expected, actual)) {
                 addIssue(node, node.getMetadata(), IssueSeverity.LOW);
             }
        }
    }
    
    private void checkComparison(BinaryExpression<?, ?> node) {
        // In comparisons, we expect operands to be compatible with each other.
        // Scratch is loosely typed, but some things are clearly odd.
        if (node.getChildren().size() >= 2) {
            if (node.getChildren().get(0) instanceof Expression left && node.getChildren().get(1) instanceof Expression right) {
                Type leftType = TypeInference.inferType(left, createResolver(node));
                Type rightType = TypeInference.inferType(right, createResolver(node));
                
                checkComparisonCompatibility(node, leftType, rightType);
            }
        }
    }

    private void checkComparisonCompatibility(ASTNode node, Type left, Type right) {
        if (left == Type.ANY || right == Type.ANY) return;
        if (left == Type.TOP || right == Type.TOP) return;
        if (left == Type.BOTTOM || right == Type.BOTTOM) return;

        if (left == Type.BOOLEAN && right == Type.NUMBER) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.NUMBER && right == Type.BOOLEAN) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.BOOLEAN && right == Type.STRING) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.STRING && right == Type.BOOLEAN) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.NUMBER && right == Type.STRING) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.STRING && right == Type.NUMBER) {
             addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        } else if (left == Type.BOOLEAN && right == Type.BOOLEAN) {
             if (node instanceof LessThan || node instanceof BiggerThan) {
                 addIssue(node, node.getMetadata(), IssueSeverity.LOW);
             }
        }
    }

    private boolean isCompatible(Type expected, Type actual) {
        if (actual == null) return true; // Inference failed
        if (actual == Type.ANY || actual == Type.TOP || actual == Type.BOTTOM) return true;
        if (expected == actual) return true;

        if (expected == Type.NUMBER) {
            return actual == Type.POSITION || actual == Type.DIRECTION || actual == Type.LOUDNESS;
        }
        
        return false;
    }

    // --- Visitor Methods ---

    @Override
    public void visit(LessThan node) {
        checkComparison(node);
        visitChildren(node); 
    }

    @Override
    public void visit(BiggerThan node) {
        checkComparison(node);
        visitChildren(node);
    }

    @Override
    public void visit(Equals node) {
        checkComparison(node);
        visitChildren(node);
    }

    @Override
    public void visit(DistanceTo node) {
        // DistanceTo returns Number.
        Position position = node.getPosition();
        if (position instanceof FromExpression fromExpression) {
            Hint hint = Hint.fromKey(WEIRD_DISTANCE);
            if (!(fromExpression.getStringExpr() instanceof AsString asString)) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            } else if (!((asString.getOperand1() instanceof StrId) || asString.getOperand1() instanceof Qualified)) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        checkType(node.getBoolExpr(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        checkType(node.getBoolExpr(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        checkType(node.getBoolExpr(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        checkType(node.getUntil(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(And node) {
        checkType(node.getOperand1(), Type.BOOLEAN);
        checkType(node.getOperand2(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(Or node) {
        checkType(node.getOperand1(), Type.BOOLEAN);
        checkType(node.getOperand2(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(Not node) {
        checkType(node.getOperand1(), Type.BOOLEAN);
        visitChildren(node);
    }
    
    @Override
    public void visit(ChangeVariableBy node) {
        checkType(node.getExpr(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetXTo node) {
        checkType(node.getNum(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetYTo node) {
        checkType(node.getNum(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeXBy node) {
        checkType(node.getNum(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeYBy node) {
        checkType(node.getNum(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(MoveSteps node) {
        checkType(node.getSteps(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(TurnLeft node) {
        checkType(node.getDegrees(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(TurnRight node) {
        checkType(node.getDegrees(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(PointInDirection node) {
        checkType(node.getDirection(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetSizeTo node) {
        checkType(node.getPercent(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeSizeBy node) {
        checkType(node.getNum(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetVolumeTo node) {
        checkType(node.getVolumeValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        checkType(node.getVolumeValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        checkType(node.getValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        checkType(node.getValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        checkType(node.getValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        checkType(node.getValue(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(WaitSeconds node) {
        checkType(node.getSeconds(), Type.NUMBER);
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        checkType(node.getTimes(), Type.NUMBER);
        visitChildren(node);
    }
    
    @Override
    public void visit(PenBlock node) {
        if (node instanceof SetPenColorToColorStmt stmt) {
            checkType(stmt.getColorExpr(), Type.COLOR);
        }
        visitChildren(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        checkType(node.getColor(), Type.COLOR);
        visitChildren(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        checkType(node.getOperand1(), Type.COLOR);
        checkType(node.getOperand2(), Type.COLOR);
        visitChildren(node);
    }

    @Override
    public void visit(AsBool node) {
        checkType(node.getOperand1(), Type.BOOLEAN);
        visitChildren(node);
    }

    @Override
    public void visit(AsNumber node) {
        checkType(node.getOperand1(), Type.NUMBER);
        visitChildren(node);
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
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(WEIRD_DISTANCE);
        return keys;
    }
}
