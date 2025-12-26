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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
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

    /**
     * Expression types that produce floating-point values (unsafe for exact comparison).
     */
    private static final Set<Class<? extends Expression>> UNSAFE_OPERAND_TYPES = Set.of(
            MouseX.class, MouseY.class, DistanceTo.class,
            PositionX.class, PositionY.class, Direction.class,
            Volume.class, Timer.class, Loudness.class, DaysSince2000.class
    );

    /**
     * AttributeOf fixed attribute types that are unsafe for exact comparison.
     */
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

        // Fallback: if no unsafe operands found, create a synthetic PositionX expression
        // PositionX is more commonly used in Scratch projects than Timer
        if (foundUnsafeOperands.isEmpty()) {
            foundUnsafeOperands.add(new PositionX(NonDataBlockMetadata.emptyNonBlockMetadata()));
        }

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
                    if (UNSAFE_ATTRIBUTE_TYPES.contains(attrFixed.getAttribute().getType())) {
                        foundUnsafeOperands.add(node);
                    }
                }
            }
        });
    }

    @Override
    public void visit(Equals node) {
        // Only inject when Equals is part of an if/until/wait-until condition
        // The finder handles recursion via inCondition flag.
        if (!isInConditionContext(node)) {
            super.visit(node);
            return;
        }

        ComparableExpr op1 = node.getOperand1();
        ComparableExpr op2 = node.getOperand2();

        boolean safe1 = isSafeOperand(op1);
        boolean safe2 = isSafeOperand(op2);

        // Only inject if at least one operand is safe (otherwise bug already exists)
        if (safe1 || safe2) {
            ComparableExpr unsafeReplacement = cloner.apply(foundUnsafeOperands.getFirst());

            if (safe1 && safe2) {
                // Both safe: replace one operand with an unsafe one
                replaceWith(node, n -> new Equals(unsafeReplacement, op2, n.getMetadata()));
            } else if (safe1) {
                // Only op1 is safe: replace it, keep op2 (already unsafe)
                replaceWith(node, n -> new Equals(unsafeReplacement, op2, n.getMetadata()));
            } else {
                // Only op2 is safe: replace it, keep op1 (already unsafe)
                replaceWith(node, n -> new Equals(op1, unsafeReplacement, n.getMetadata()));
            }
        }

        super.visit(node);
    }


    @Override
    public void visit(LessThan node) {
        if (isInConditionContext(node) && isUnsafeComparison(node)) {
            replaceWith(node, n -> new Equals(cloner.apply(n.getOperand1()), cloner.apply(n.getOperand2()), n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(BiggerThan node) {
        if (isInConditionContext(node) && isUnsafeComparison(node)) {
            replaceWith(node, n -> new Equals(cloner.apply(n.getOperand1()), cloner.apply(n.getOperand2()), n.getMetadata()));
        }
        super.visit(node);
    }

    private boolean isUnsafeComparison(BinaryExpression<ComparableExpr, ComparableExpr> condition) {
        return !isSafeOperand(condition.getOperand1()) || !isSafeOperand(condition.getOperand2());
    }

    private boolean isSafeOperand(ComparableExpr operand) {
        if (UNSAFE_OPERAND_TYPES.contains(operand.getClass())) {
            return false;
        }
        if (operand instanceof AttributeOf attributeOf) {
            if (attributeOf.getAttribute() instanceof AttributeFromFixed attrFixed) {
                if (UNSAFE_ATTRIBUTE_TYPES.contains(attrFixed.getAttribute().getType())) {
                    return false;
                }
            }
        }

        // Recursively check children for unsafeness (e.g., Timer inside Add)
        for (ASTNode child : operand.getChildren()) {
            if (child instanceof ComparableExpr comparableChild) {
                if (!isSafeOperand(comparableChild)) {
                    return false;
                }
            }
        }

        return true;
    }
}
