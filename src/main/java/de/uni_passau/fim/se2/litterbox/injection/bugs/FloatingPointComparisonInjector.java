/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.FloatingPointComparison;
import de.uni_passau.fim.se2.litterbox.ast.builder.IfStmtBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects FloatingPointComparison bug in two ways:
 * 1. For Equals comparisons with safe operands: Replace operands with unsafe ones found in the program
 * 2. For LessThan/BiggerThan with BOTH unsafe operands: Replace with Equals comparison
 * <p>
 * Only modifies comparisons inside conditions (if, until, wait until).
 * Uses immutable transformations to maintain AST immutability.
 */
public class FloatingPointComparisonInjector extends AbstractBugInjector {

    private final CloneVisitor cloner = new CloneVisitor();
    private final List<ComparableExpr> foundUnsafeOperands = new ArrayList<>();

    @Override
    public IssueFinder getIssueFinder() {
        return new FloatingPointComparison();
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        // First pass: collect unsafe operands used in the program
        foundUnsafeOperands.clear();
        collectUnsafeOperands(program);

        // Then proceed with normal injection
        return super.inject(program);
    }

    private void collectUnsafeOperands(Program program) {
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(Timer node) {
                foundUnsafeOperands.add(node);
            }

            @Override
            public void visit(MouseX node) {
                foundUnsafeOperands.add(node);
            }

            @Override
            public void visit(MouseY node) {
                foundUnsafeOperands.add(node);
            }

            @Override
            public void visit(DistanceTo node) {
                foundUnsafeOperands.add(node);
            }

            @Override
            public void visit(PositionX node) {
                foundUnsafeOperands.add(node);
            }

            @Override
            public void visit(PositionY node) {
                foundUnsafeOperands.add(node);
            }

            @Override
            public void visit(Direction node) {
                foundUnsafeOperands.add(node);
            }

            @Override
            public void visit(Volume node) {
                foundUnsafeOperands.add(node);
            }

            @Override
            public void visit(AttributeOf node) {
                if (node.getAttribute() instanceof AttributeFromFixed attrFixed) {
                    FixedAttribute.FixedAttributeType type = attrFixed.getAttribute().getType();
                    if (type == FixedAttribute.FixedAttributeType.X_POSITION
                            || type == FixedAttribute.FixedAttributeType.Y_POSITION
                            || type == FixedAttribute.FixedAttributeType.DIRECTION
                            || type == FixedAttribute.FixedAttributeType.VOLUME) {
                        foundUnsafeOperands.add(node);
                    }
                }
            }
        });
    }

    @Override
    public void visit(Equals node) {
        if (!isInConditionContext(node)) {
            super.visit(node);
            return;
        }

        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        // Only inject if both operands are currently "safe" (no bug present yet)
        if (isSafeOperand(op1) && isSafeOperand(op2)) {
            // Only inject if we found at least 2 unsafe operands in the program
            if (foundUnsafeOperands.size() >= 2) {
                // Use two different unsafe operands from the program
                ComparableExpr unsafe1 = cloner.apply(foundUnsafeOperands.get(0));
                ComparableExpr unsafe2 = cloner.apply(foundUnsafeOperands.get(1));
                addImmutableMutation(node, n -> new Equals(unsafe1, unsafe2, n.getMetadata()));
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        tryInjectComparisonReplacement(node);
        super.visit(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        tryInjectComparisonReplacement(node);
        super.visit(node);
    }

    /**
     * Common logic for injecting LessThan/BiggerThan -> Equals replacement in if-statements.
     */
    private void tryInjectComparisonReplacement(IfStmt node) {
        BoolExpr condition = node.getBoolExpr();

        // Check if condition is LessThan or BiggerThan with both operands unsafe
        if (!isUnsafeComparison(condition)) {
            return;
        }

        addMutation(node, clone -> {
            BoolExpr clonedCondition = clone.getBoolExpr();

            // Extract operands from the comparison
            @SuppressWarnings("unchecked")
            BinaryExpression<ComparableExpr, ComparableExpr> comparison =
                    (BinaryExpression<ComparableExpr, ComparableExpr>) clonedCondition;

            Equals equals = new Equals(
                    cloner.apply(comparison.getOperand1()),
                    cloner.apply(comparison.getOperand2()),
                    clonedCondition.getMetadata()
            );

            // Build new if-statement with Equals condition
            IfStmtBuilder builder = new IfStmtBuilder()
                    .withCondition(equals)
                    .withBody(clone.getThenStmts())
                    .withMetadata(clone.getMetadata());

            // Add else body if present
            if (clone instanceof IfElseStmt ifElse) {
                builder.withElseBody(ifElse.getElseStmts());
            }

            replaceInParentStmtList(clone, builder.build());
        });
    }

    private boolean isUnsafeComparison(BoolExpr condition) {
        if (condition instanceof LessThan lt) {
            return !isSafeOperand(lt.getOperand1()) && !isSafeOperand(lt.getOperand2());
        } else if (condition instanceof BiggerThan gt) {
            return !isSafeOperand(gt.getOperand1()) && !isSafeOperand(gt.getOperand2());
        }
        return false;
    }

    private void replaceInParentStmtList(Stmt oldStmt, Stmt newStmt) {
        ASTNode parent = oldStmt.getParentNode();
        if (parent instanceof StmtList stmtList) {
            List<Stmt> stmts = stmtList.getStmts();
            int index = stmts.indexOf(oldStmt);
            if (index >= 0) {
                stmts.set(index, newStmt);
                newStmt.setParentNode(stmtList);
            }
        }
    }

    private boolean isInConditionContext(ASTNode node) {
        ASTNode parent = node.getParentNode();
        while (parent != null) {
            if (parent instanceof IfThenStmt || parent instanceof IfElseStmt
                    || parent instanceof UntilStmt || parent instanceof WaitUntil) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    private boolean isSafeOperand(ComparableExpr operand) {
        if (operand instanceof MouseX || operand instanceof MouseY || operand instanceof DistanceTo
                || operand instanceof PositionX || operand instanceof PositionY || operand instanceof Direction
                || operand instanceof Volume || operand instanceof Timer) {
            return false;
        } else if (operand instanceof AttributeOf attributeOf) {
            if (attributeOf.getAttribute() instanceof AttributeFromFixed attributeFromFixed) {
                return attributeFromFixed.getAttribute().getType() != FixedAttribute.FixedAttributeType.X_POSITION
                        && attributeFromFixed.getAttribute().getType() != FixedAttribute.FixedAttributeType.Y_POSITION
                        && attributeFromFixed.getAttribute().getType() != FixedAttribute.FixedAttributeType.DIRECTION
                        && attributeFromFixed.getAttribute().getType() != FixedAttribute.FixedAttributeType.VOLUME;
            }
        }
        return true;
    }
}
