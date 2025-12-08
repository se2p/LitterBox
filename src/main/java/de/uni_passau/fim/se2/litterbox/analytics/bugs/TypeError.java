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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
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
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.Color;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenBlock;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnLeft;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.TurnRight;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.dataflow.TypeAnalysis;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.dataflow.VariableTypeFact;
import de.uni_passau.fim.se2.litterbox.dataflow.DataflowAnalysis;
import de.uni_passau.fim.se2.litterbox.cfg.CFGNode;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.Variable;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * Detects type errors of the following form:
 * - Comparisons between Number and Boolean
 * - Comparisons between Direction and Loudness
 * - Comparisons between Direction and Position nodes (MouseX, MouseY, PositionX, PositionY)
 * - Comparisons between Loudness and Position nodes (MouseX, MouseY, PositionX, PositionY)
 * - Comparisons containing Touching or Not nodes
 * - Distance to containing weird blocks
 */
public class TypeError extends AbstractIssueFinder {
    public static final String NAME = "type_error";
    public static final String WEIRD_DISTANCE = "type_error_weird_distance";
    private boolean isInsideComparison = false;
    private boolean isRightSide = false;

    private Type type = null;
    private boolean isInsideQualified = false;
    private IssueSeverity issueSeverity;

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

    @Override
    public void visit(LessThan node) {
        if (!isContextCompatible(Type.BOOLEAN)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        traverseComparison(node);
    }

    @Override
    public void visit(BiggerThan node) {
        if (!isContextCompatible(Type.BOOLEAN)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        traverseComparison(node);
    }

    @Override
    public void visit(Equals node) {
        if (!isContextCompatible(Type.BOOLEAN)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        traverseComparison(node);
    }

    @Override
    public void visit(DistanceTo node) {
        if (!isContextCompatible(Type.NUMBER)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        Position position = node.getPosition();
        if (position instanceof FromExpression fromExpression) {
            Hint hint = Hint.fromKey(WEIRD_DISTANCE);
            if (!(fromExpression.getStringExpr() instanceof AsString asString)) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            } else if (!((asString.getOperand1() instanceof StrId) || asString.getOperand1() instanceof Qualified)) {
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
        visitChildrenWithResetContext(node);
    }

    private void traverseComparison(BinaryExpression<ComparableExpr, ComparableExpr> node) {
        boolean oldInside = isInsideComparison;
        Type oldType = type;
        
        isInsideComparison = true;
        type = null;
        
        if (!node.getChildren().isEmpty()) {
            isRightSide = false;
            ASTNode leftChild = node.getChildren().get(0);
            leftChild.accept(this);
            ASTNode rightChild = node.getChildren().get(1);
            isRightSide = true;
            rightChild.accept(this);
        }
        
        isInsideComparison = oldInside;
        type = oldType;
    }

    @Override
    public void visit(StringExpr node) {
        if (isInsideComparison) {
            if (!isRightSide) {
                // String expressions (like join) can produce values that Scratch interprets 
                // as numbers or booleans (e.g. join "1" "0" -> 10). 
                // Therefore, we don't enforce a specific type expectation on the other side.
                type = null;
            }
        }
    }

    @Override
    public void visit(Loudness node) {
        if (!isContextCompatible(Type.LOUDNESS)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public void visit(StringLiteral node) {
        // String literals inside qualified nodes (variables) are just identifiers (the variable name),
        // not actual string values used in the comparison.
        if (isInsideComparison && !isInsideQualified) {
            if (!isRightSide) {
                this.type = Type.STRING;
            } else {
                if (this.type != null && type != Type.STRING) {
                    addIssue(node.getParentNode(), node.getParentNode().getMetadata(), IssueSeverity.LOW);
                }
            }
        }
    }



    @Override
    public void visit(Qualified node) {
        isInsideQualified = true;
        visitChildren(node);
        isInsideQualified = false;
    }

    @Override
    public void visit(MouseX node) {
        if (!isContextCompatible(Type.POSITION)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public void visit(MouseY node) {
        if (!isContextCompatible(Type.POSITION)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public void visit(PositionX node) {
        if (!isContextCompatible(Type.POSITION)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public void visit(PositionY node) {
        if (!isContextCompatible(Type.POSITION)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public void visit(PickRandom node) {
        if (!isContextCompatible(Type.NUMBER)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        if (isInsideComparison) {
            if (!isRightSide) {
                type = Type.NUMBER;
            } else {
                if (this.type != null && (this.type == Type.BOOLEAN || this.type == Type.STRING)) {
                    addIssue(node, node.getMetadata(), IssueSeverity.LOW);
                }
            }
        }
    }

    @Override
    public void visit(NumExpr node) {
        if (isInsideComparison) {
            if (!isRightSide) {
                type = Type.NUMBER;
            } else {
                if (this.type != null && (this.type == Type.BOOLEAN || this.type == Type.STRING)) {
                    addIssue(node, node.getMetadata(), IssueSeverity.LOW);
                }
            }
        }
    }

    @Override
    public void visit(Direction node) {
        if (!isContextCompatible(Type.DIRECTION)) {
            addIssue(node, node.getMetadata(), issueSeverity);
        }
    }

    @Override
    public void visit(AsString node) {
        if (!isContextCompatible(Type.STRING)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        if (isInsideComparison) {
            if (!isRightSide) {
                if (node.getOperand1() instanceof Touching || node.getOperand1() instanceof Not) {
                    type = Type.BOOLEAN;
                } else {
                    type = Type.STRING;
                }
            } 
        }

        boolean oldInside = isInsideComparison;
        boolean oldRight = isRightSide;
        Type oldType = type;

        node.getOperand1().accept(this);

        isInsideComparison = oldInside;
        isRightSide = oldRight;
        type = oldType;
    }




    public void visit(AsNumber node) {
        if (!isContextCompatible(Type.NUMBER)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        if (isInsideComparison) {
            if (!isRightSide) {
                type = Type.NUMBER;
            } else {
                // POSITION, DIRECTION, LOUDNESS, COLOR are all treated as numbers and are therefore compatible
                if (this.type == Type.BOOLEAN || this.type == Type.STRING) {
                    addIssue(node.getOperand1(), node.getOperand1().getMetadata(), IssueSeverity.LOW);
                }
            }
        }
        
        boolean oldInside = isInsideComparison;
        boolean oldRight = isRightSide;
        Type oldType = type;

        node.getOperand1().accept(this);

        isInsideComparison = oldInside;
        isRightSide = oldRight;
        type = oldType;
    }

    @Override
    public void visit(NumberLiteral node) {
        if (isInsideComparison) {
            if (!isRightSide) {
                type = Type.NUMBER;
            } else {
                if (this.type != null && (this.type == Type.BOOLEAN || this.type == Type.STRING)) {
                    addIssue(node.getParentNode(), node.getParentNode().getMetadata(), IssueSeverity.LOW);
                }
            }
        }
    }

    private boolean isContextCompatible(Type type) {
        if (isInsideComparison) {
            if (!isRightSide) {
                this.type = type;
            } else {
                if (this.type != null && this.type != type) {
                    if (this.type == Type.NUMBER && type == Type.BOOLEAN) {
                        return false;
                    }
                    if (this.type != Type.NUMBER) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void checkExpectedType(ASTNode node, Type expectedType) {
        boolean oldInside = isInsideComparison;
        boolean oldRight = isRightSide;
        Type oldType = type;

        isInsideComparison = true;
        isRightSide = true;
        type = expectedType;

        if (node != null) {
            node.accept(this);
        }

        isInsideComparison = oldInside;
        isRightSide = oldRight;
        type = oldType;
    }

    @Override
    public void visit(Color node) {
        if (!isContextCompatible(Type.COLOR)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        checkExpectedType(node.getBoolExpr(), Type.BOOLEAN);
        node.getThenStmts().accept(this);
    }

    @Override
    public void visit(IfElseStmt node) {
        checkExpectedType(node.getBoolExpr(), Type.BOOLEAN);
        node.getThenStmts().accept(this);
        node.getElseStmts().accept(this);
    }

    @Override
    public void visit(UntilStmt node) {
        checkExpectedType(node.getBoolExpr(), Type.BOOLEAN);
        node.getStmtList().accept(this);
    }

    @Override
    public void visit(WaitUntil node) {
        checkExpectedType(node.getUntil(), Type.BOOLEAN);
    }

    @Override
    public void visit(PenBlock node) {
        if (node instanceof SetPenColorToColorStmt stmt) {
            checkExpectedType(stmt.getColorExpr(), Type.COLOR);
        }
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        checkExpectedType(node.getColor(), Type.COLOR);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        checkExpectedType(node.getOperand1(), Type.COLOR);
        checkExpectedType(node.getOperand2(), Type.COLOR);
    }

    @Override
    public void visit(And node) {
        if (!isContextCompatible(Type.BOOLEAN)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        checkExpectedType(node.getOperand1(), Type.BOOLEAN);
        checkExpectedType(node.getOperand2(), Type.BOOLEAN);
    }

    @Override
    public void visit(Or node) {
        if (!isContextCompatible(Type.BOOLEAN)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        checkExpectedType(node.getOperand1(), Type.BOOLEAN);
        checkExpectedType(node.getOperand2(), Type.BOOLEAN);
    }

    @Override
    public void visit(Not node) {
        if (!isContextCompatible(Type.BOOLEAN)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        checkExpectedType(node.getOperand1(), Type.BOOLEAN);
    }

    /**
     * Visits the children of the given node with a reset context.
     * This ensures that the current comparison state (e.g., expected type, right-side flag)
     * does not propagate to independent child expressions (such as arguments to a function call),
     * preventing false positives where child nodes are judged against the parent's context.
     */
    private void visitChildrenWithResetContext(ASTNode node) {
        boolean oldInside = isInsideComparison;
        Type oldType = type;
        boolean oldRight = isRightSide;

        isInsideComparison = false;
        type = null;
        isRightSide = false;

        visitChildren(node);

        isInsideComparison = oldInside;
        type = oldType;
        isRightSide = oldRight;
    }

    @Override
    public void visit(Touching node) {
        if (!isContextCompatible(Type.BOOLEAN)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        visitChildrenWithResetContext(node);
    }

    @Override
    public void visit(IsKeyPressed node) {
        if (!isContextCompatible(Type.BOOLEAN)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        visitChildrenWithResetContext(node);
    }

    @Override
    public void visit(IsMouseDown node) {
        if (!isContextCompatible(Type.BOOLEAN)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        visitChildrenWithResetContext(node);
    }

    @Override
    public void visit(ListContains node) {
        if (!isContextCompatible(Type.BOOLEAN)) {
            addIssue(node, node.getMetadata(), IssueSeverity.LOW);
        }
        visitChildrenWithResetContext(node);
    }
    @Override
    public void visit(ChangeVariableBy node) {
        checkExpectedType(node.getExpr(), Type.NUMBER);
        node.getIdentifier().accept(this);
    }

    @Override
    public void visit(SetXTo node) {
        checkExpectedType(node.getNum(), Type.NUMBER);
    }

    @Override
    public void visit(SetYTo node) {
        checkExpectedType(node.getNum(), Type.NUMBER);
    }

    @Override
    public void visit(ChangeXBy node) {
        checkExpectedType(node.getNum(), Type.NUMBER);
    }

    @Override
    public void visit(ChangeYBy node) {
        checkExpectedType(node.getNum(), Type.NUMBER);
    }

    @Override
    public void visit(MoveSteps node) {
        checkExpectedType(node.getSteps(), Type.NUMBER);
    }

    @Override
    public void visit(TurnLeft node) {
        checkExpectedType(node.getDegrees(), Type.NUMBER);
    }

    @Override
    public void visit(TurnRight node) {
        checkExpectedType(node.getDegrees(), Type.NUMBER);
    }

    @Override
    public void visit(PointInDirection node) {
        checkExpectedType(node.getDirection(), Type.NUMBER);
    }

    @Override
    public void visit(SetSizeTo node) {
        checkExpectedType(node.getPercent(), Type.NUMBER);
    }

    @Override
    public void visit(ChangeSizeBy node) {
        checkExpectedType(node.getNum(), Type.NUMBER);
    }

    @Override
    public void visit(SetVolumeTo node) {
        checkExpectedType(node.getVolumeValue(), Type.NUMBER);
    }

    @Override
    public void visit(ChangeVolumeBy node) {
        checkExpectedType(node.getVolumeValue(), Type.NUMBER);
    }

    @Override
    public void visit(SetSoundEffectTo node) {
        checkExpectedType(node.getValue(), Type.NUMBER);
    }

    @Override
    public void visit(ChangeSoundEffectBy node) {
        checkExpectedType(node.getValue(), Type.NUMBER);
    }

    @Override
    public void visit(SetGraphicEffectTo node) {
        checkExpectedType(node.getValue(), Type.NUMBER);
    }

    @Override
    public void visit(ChangeGraphicEffectBy node) {
        checkExpectedType(node.getValue(), Type.NUMBER);
    }

    @Override
    public void visit(WaitSeconds node) {
        checkExpectedType(node.getSeconds(), Type.NUMBER);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        checkExpectedType(node.getTimes(), Type.NUMBER);
        node.getStmtList().accept(this);
    }

    @Override
    public void visit(StrId node) {
        if (isInsideComparison && !isRightSide) {
             Type varType = getVariableType(node);
             if (varType != Type.ANY && varType != Type.TOP && varType != Type.BOTTOM) {
                  this.type = varType;
             } else {
                  this.type = null;
             }
        } else if (isInsideComparison) {
            if (this.type != null) {
                Type varType = getVariableType(node);
                if (!isCompatible(this.type, varType)) {
                     addIssue(node, node.getMetadata(), IssueSeverity.LOW);
                }
            }
        }
    }

    private Type getVariableType(LocalIdentifier node) {
        if (analysis == null) return Type.ANY;

        ASTNode current = node;
        CFGNode cfgNode = null;
        while (current != null) {
            if (astToCfg.containsKey(current)) {
                cfgNode = astToCfg.get(current);
                break;
            }
            current = current.getParentNode();
        }

        if (cfgNode == null) return Type.ANY;

        Set<VariableTypeFact> facts = analysis.getDataflowFacts(cfgNode);
        if (facts == null) return Type.ANY;

        for (VariableTypeFact fact : facts) {
             if (fact.getVariable() instanceof Variable v) {
                 Identifier id = v.getIdentifier();
                 if (id instanceof LocalIdentifier localId) {
                     if (localId.getName().equals(node.getName())) {
                         return fact.getType();
                     }
                 } else if (id instanceof Qualified qualified) {
                     if (qualified.getSecond().getName().getName().equals(node.getName())) {
                         return fact.getType();
                     }
                 }
             }
        }
        
        return Type.BOTTOM;
    }

    private boolean isCompatible(Type expected, Type actual) {
        if (actual == Type.ANY || actual == Type.TOP || actual == Type.BOTTOM) return true;
        if (expected == actual) return true;

        if (expected == Type.NUMBER && actual == Type.POSITION) return true;
        if (expected == Type.NUMBER && actual == Type.DIRECTION) return true;
        if (expected == Type.NUMBER && actual == Type.LOUDNESS) return true;

        return false;
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
