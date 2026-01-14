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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InvalidNumberString;
import de.uni_passau.fim.se2.litterbox.ast.builder.ComparisonBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Injects InvalidNumberString bug by replacing numeric parameters with invalid number strings.
 *
 * <p>
 * Creates invalid numbers by inserting extra decimal points into the original number.
 * Uses immutable transformations to maintain AST immutability.
 */
public class InvalidNumberStringInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new InvalidNumberString();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    // ==================== Primary Injection Point ====================

    /**
     * Handles AsNumber(StringLiteral) patterns where the string represents a valid number.
     * This is the primary injection point for real Scratch projects, which often store
     * numbers as strings in JSON (e.g., "10" becomes AsNumber(StringLiteral("10"))).
     */
    @Override
    public void visit(AsNumber node) {
        if (node.getOperand1() instanceof StringLiteral sl) {
            String text = sl.getText();
            if (isNumericString(text)) {
                String invalidNum = makeInvalidNumberString(text);
                replaceNodeImmutably(node, n -> new AsNumber(new StringLiteral(invalidNum)));
            }
        }
        super.visit(node);
    }

    // ==================== Binary Numeric Operations ====================

    @Override
    public void visit(Add node) {
        injectIntoBinaryNumericOp(node, node.getOperand1(), node.getOperand2(), Add::new);
        super.visit(node);
    }

    @Override
    public void visit(Minus node) {
        injectIntoBinaryNumericOp(node, node.getOperand1(), node.getOperand2(), Minus::new);
        super.visit(node);
    }

    @Override
    public void visit(Mult node) {
        injectIntoBinaryNumericOp(node, node.getOperand1(), node.getOperand2(), Mult::new);
        super.visit(node);
    }

    @Override
    public void visit(Div node) {
        injectIntoBinaryNumericOp(node, node.getOperand1(), node.getOperand2(), Div::new);
        super.visit(node);
    }

    @Override
    public void visit(Mod node) {
        injectIntoBinaryNumericOp(node, node.getOperand1(), node.getOperand2(), Mod::new);
        super.visit(node);
    }

    // ==================== Comparison Operations ====================

    @Override
    public void visit(Equals node) {
        injectIntoComparisonOp(node, node.getOperand1(), node.getOperand2(),
                (op1, op2) -> new ComparisonBuilder()
                        .from(node)
                        .withOperand1(op1)
                        .withOperand2(op2)
                        .buildEquals());
        super.visit(node);
    }

    @Override
    public void visit(LessThan node) {
        injectIntoComparisonOp(node, node.getOperand1(), node.getOperand2(),
                (op1, op2) -> new ComparisonBuilder()
                        .from(node)
                        .withOperand1(op1)
                        .withOperand2(op2)
                        .buildLessThan());
        super.visit(node);
    }

    @Override
    public void visit(BiggerThan node) {
        injectIntoComparisonOp(node, node.getOperand1(), node.getOperand2(),
                (op1, op2) -> new ComparisonBuilder()
                        .from(node)
                        .withOperand1(op1)
                        .withOperand2(op2)
                        .buildBiggerThan());
        super.visit(node);
    }

    // ==================== Motion Blocks ====================

    @Override
    public void visit(MoveSteps node) {
        injectIntoSingleNumericParam(node, node.getSteps(),
                replacement -> new MoveSteps(replacement, node.getMetadata()));
        super.visit(node);
    }

    @Override
    public void visit(SetXTo node) {
        injectIntoSingleNumericParam(node, node.getNum(),
                replacement -> new SetXTo(replacement, node.getMetadata()));
        super.visit(node);
    }

    @Override
    public void visit(SetYTo node) {
        injectIntoSingleNumericParam(node, node.getNum(),
                replacement -> new SetYTo(replacement, node.getMetadata()));
        super.visit(node);
    }

    @Override
    public void visit(PointInDirection node) {
        injectIntoSingleNumericParam(node, node.getDirection(),
                replacement -> new PointInDirection(replacement, node.getMetadata()));
        super.visit(node);
    }

    // ==================== Control/Common Blocks ====================

    @Override
    public void visit(WaitSeconds node) {
        injectIntoSingleNumericParam(node, node.getSeconds(),
                replacement -> new WaitSeconds(replacement, node.getMetadata()));
        super.visit(node);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        // Only inject in script/procedure context, not in SetStmtList (variable initialization)
        if (isInScriptContext(node) && node.getExpr() instanceof NumberLiteral numLit) {
            double value = numLit.getValue();
            if (isValidInjectionTarget(value)) {
                NumExpr replacement = createInvalidNumExpr(value);
                var clonedIdent = cloner.apply(node.getIdentifier());
                BlockMetadata clonedMeta = node.getMetadata();
                replaceNodeImmutably(node, n -> new ChangeVariableBy(clonedIdent, replacement, clonedMeta));
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(SetVariableTo node) {
        // Only inject in script/procedure context, not in SetStmtList (variable initialization)
        if (isInScriptContext(node) && node.getExpr() instanceof NumberLiteral numLit) {
            double value = numLit.getValue();
            if (isValidInjectionTarget(value)) {
                NumExpr replacement = createInvalidNumExpr(value);
                var clonedIdent = cloner.apply(node.getIdentifier());
                BlockMetadata clonedMeta = node.getMetadata();
                replaceNodeImmutably(node, n -> new SetVariableTo(clonedIdent, replacement, clonedMeta));
            }
        }
        super.visit(node);
    }

    // ==================== Helper Methods ====================

    /**
     * Injects invalid number into binary numeric operations (Add, Minus, Mult, Div, Mod).
     * Creates separate mutations for operand1 and operand2 if they are NumberLiterals.
     *
     * @param node        The original node to potentially replace
     * @param operand1    First operand
     * @param operand2    Second operand
     * @param constructor Function to construct the replacement node
     * @param <T>         The node type
     */
    private <T extends ASTNode> void injectIntoBinaryNumericOp(
            T node,
            NumExpr operand1,
            NumExpr operand2,
            TriFunction<NumExpr, NumExpr, BlockMetadata, T> constructor) {

        if (operand1 instanceof NumberLiteral numLit) {
            NumExpr replacement = createInvalidNumExpr(numLit.getValue());
            NumExpr clonedOp2 = cloner.apply(operand2);
            BlockMetadata clonedMeta = node.getMetadata();
            replaceNodeImmutably(node, n -> constructor.apply(replacement, clonedOp2, clonedMeta));
        }
        if (operand2 instanceof NumberLiteral numLit) {
            NumExpr replacement = createInvalidNumExpr(numLit.getValue());
            NumExpr clonedOp1 = cloner.apply(operand1);
            BlockMetadata clonedMeta = node.getMetadata();
            replaceNodeImmutably(node, n -> constructor.apply(clonedOp1, replacement, clonedMeta));
        }
    }

    @FunctionalInterface
    private interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    /**
     * Injects invalid number into comparison operations (Equals, LessThan, BiggerThan).
     * Creates separate mutations for operand1 and operand2 if they are NumberLiterals.
     *
     * @param node        The original node to potentially replace
     * @param operand1    First operand
     * @param operand2    Second operand
     * @param constructor Function to construct the replacement node
     * @param <T>         The node type
     */
    private <T extends ASTNode> void injectIntoComparisonOp(
            T node,
            ComparableExpr operand1,
            ComparableExpr operand2,
            BiFunction<ComparableExpr, ComparableExpr, T> constructor) {

        if (operand1 instanceof NumberLiteral numLit) {
            NumExpr replacement = createInvalidNumExpr(numLit.getValue());
            ComparableExpr clonedOp2 = cloner.apply(operand2);
            replaceNodeImmutably(node, n -> constructor.apply(replacement, clonedOp2));
        }
        if (operand2 instanceof NumberLiteral numLit) {
            NumExpr replacement = createInvalidNumExpr(numLit.getValue());
            ComparableExpr clonedOp1 = cloner.apply(operand1);
            replaceNodeImmutably(node, n -> constructor.apply(clonedOp1, replacement));
        }
    }

    /**
     * Injects invalid number into blocks with a single numeric parameter.
     *
     * @param node        The original node to potentially replace
     * @param numParam    The numeric parameter to check
     * @param constructor Function to construct the replacement node
     * @param <T>         The node type
     */
    private <T extends ASTNode> void injectIntoSingleNumericParam(
            T node,
            NumExpr numParam,
            Function<NumExpr, T> constructor) {

        if (numParam instanceof NumberLiteral numLit) {
            NumExpr replacement = createInvalidNumExpr(numLit.getValue());
            replaceNodeImmutably(node, n -> constructor.apply(replacement));
        }
    }

    /**
     * Creates an AsNumber expression wrapping an invalid number string.
     */
    private NumExpr createInvalidNumExpr(double value) {
        return new AsNumber(
                new StringLiteral(makeInvalidNumber(value)),
                new DataBlockMetadata(CloneVisitor.generateUID(), null, 0, 0)
        );
    }

    /**
     * Checks if a value is a valid target for injection (not Infinity or NaN).
     */
    private boolean isValidInjectionTarget(double value) {
        return !Double.isInfinite(value) && !Double.isNaN(value);
    }

    /**
     * Checks if a string can be parsed as a valid number.
     * Excludes special values like Infinity and NaN since corrupting them
     * produces strings with letters that the finder won't detect.
     */
    private boolean isNumericString(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            double value = Double.parseDouble(text);
            return isValidInjectionTarget(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Creates an invalid number string from a string representation.
     * Inserts extra decimal points to make the number unparseable.
     */
    private String makeInvalidNumberString(String numStr) {
        return insertExtraDecimalPoints(numStr);
    }

    /**
     * Creates an invalid number string by inserting extra decimal points.
     *
     * <p>
     * Examples:
     * <ul>
     *   <li>"10" → "1.0." (add two dots)</li>
     *   <li>"3.14" → "3.1.4" (add one dot)</li>
     * </ul>
     */
    private String makeInvalidNumber(double value) {
        return insertExtraDecimalPoints(String.valueOf(value));
    }

    /**
     * Core logic for inserting extra decimal points into a number string.
     */
    private String insertExtraDecimalPoints(String numStr) {
        long dotCount = numStr.chars().filter(c -> c == '.').count();

        if (dotCount == 0) {
            // No decimal point: insert two dots to make invalid
            if (numStr.length() >= 2) {
                int mid = numStr.length() / 2;
                return numStr.substring(0, mid) + "." + numStr.substring(mid) + ".";
            } else {
                return numStr + "..";
            }
        } else {
            // Has one decimal point: insert another dot
            int dotIndex = numStr.indexOf('.');
            int insertPos = dotIndex + 2;
            if (insertPos < numStr.length()) {
                return numStr.substring(0, insertPos) + "." + numStr.substring(insertPos);
            } else {
                return numStr + ".";
            }
        }
    }
}
