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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Injects FloatingPointComparison bug in two ways:
 * <p>
 * <b>Bug Pattern:</b> Comparing floating-point values with exact equality can fail due to
 * precision issues.
 * <p>
 * <b>Injection Strategies:</b>
 * <ol>
 *   <li>For Equals comparisons with safe operands: Replace operands with unsafe ones found in the program</li>
 *   <li>For LessThan/BiggerThan with BOTH unsafe operands: Replace with Equals comparison</li>
 * </ol>
 * <p>
 * Only modifies comparisons inside conditions (if, until, wait until).
 */
public class FloatingPointComparisonInjector extends AbstractBugInjector {

    /** Expression types that produce floating-point values (unsafe for exact comparison). */
    private static final Set<Class<? extends Expression>> UNSAFE_OPERAND_TYPES = Set.of(
            MouseX.class, MouseY.class, DistanceTo.class,
            PositionX.class, PositionY.class, Direction.class,
            Volume.class, Timer.class
    );

    /** AttributeOf fixed attribute types that are unsafe for exact comparison. */
    private static final Set<FixedAttribute.FixedAttributeType> UNSAFE_ATTRIBUTE_TYPES = Set.of(
            FixedAttribute.FixedAttributeType.X_POSITION,
            FixedAttribute.FixedAttributeType.Y_POSITION,
            FixedAttribute.FixedAttributeType.DIRECTION,
            FixedAttribute.FixedAttributeType.VOLUME
    );

    private final List<ComparableExpr> foundUnsafeOperands = new ArrayList<>();

    @Override
    public IssueFinder getIssueFinder() {
        return new FloatingPointComparison();
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        foundUnsafeOperands.clear();
        collectUnsafeOperands(program);
        return super.inject(program);
    }

    private void collectUnsafeOperands(Program program) {
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(Timer node) { foundUnsafeOperands.add(node); }

            @Override
            public void visit(MouseX node) { foundUnsafeOperands.add(node); }

            @Override
            public void visit(MouseY node) { foundUnsafeOperands.add(node); }

            @Override
            public void visit(DistanceTo node) { foundUnsafeOperands.add(node); }

            @Override
            public void visit(PositionX node) { foundUnsafeOperands.add(node); }

            @Override
            public void visit(PositionY node) { foundUnsafeOperands.add(node); }

            @Override
            public void visit(Direction node) { foundUnsafeOperands.add(node); }

            @Override
            public void visit(Volume node) { foundUnsafeOperands.add(node); }

            @Override
            public void visit(AttributeOf node) {
                if (node.getAttribute() instanceof AttributeFromFixed attrFixed) {
                    if (UNSAFE_ATTRIBUTE_TYPES.contains(attrFixed.getAttribute().getType())) {
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
        if (isSafeOperand(op1) && isSafeOperand(op2) && foundUnsafeOperands.size() >= 2) {
            ComparableExpr unsafe1 = cloner.apply(foundUnsafeOperands.get(0));
            ComparableExpr unsafe2 = cloner.apply(foundUnsafeOperands.get(1));
            replaceWith(node, n -> new Equals(unsafe1, unsafe2, n.getMetadata()));
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

    private void tryInjectComparisonReplacement(IfStmt node) {
        BoolExpr condition = node.getBoolExpr();

        if (!isUnsafeComparison(condition)) {
            return;
        }

        mutateClone(node, clone -> {
            BoolExpr clonedCondition = clone.getBoolExpr();

            @SuppressWarnings("unchecked")
            BinaryExpression<ComparableExpr, ComparableExpr> comparison =
                    (BinaryExpression<ComparableExpr, ComparableExpr>) clonedCondition;

            Equals equals = new Equals(
                    cloner.apply(comparison.getOperand1()),
                    cloner.apply(comparison.getOperand2()),
                    clonedCondition.getMetadata()
            );

            IfStmtBuilder builder = new IfStmtBuilder()
                    .withCondition(equals)
                    .withBody(clone.getThenStmts())
                    .withMetadata(clone.getMetadata());

            if (clone instanceof IfElseStmt ifElse) {
                builder.withElseBody(ifElse.getElseStmts());
            }

            replaceInParent(clone, builder.build());
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

    private boolean isSafeOperand(ComparableExpr operand) {
        if (UNSAFE_OPERAND_TYPES.contains(operand.getClass())) {
            return false;
        }
        if (operand instanceof AttributeOf attributeOf) {
            if (attributeOf.getAttribute() instanceof AttributeFromFixed attrFixed) {
                return !UNSAFE_ATTRIBUTE_TYPES.contains(attrFixed.getAttribute().getType());
            }
        }
        return true;
    }
}
