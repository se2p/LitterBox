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
package de.uni_passau.fim.se2.litterbox.ast.builder;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;

/**
 * Fluent builder for constructing comparison nodes (Equals, LessThan, BiggerThan).
 *
 * <p>
 * Simplifies comparison creation and modification in bug injectors.
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * Equals newEquals = new ComparisonBuilder()
 *     .from(existingEquals)
 *     .withOperand2(newOperand)
 *     .buildEquals();
 * }</pre>
 */
public class ComparisonBuilder {

    private ComparableExpr operand1;
    private ComparableExpr operand2;
    private BlockMetadata metadata;

    public ComparisonBuilder() {
        this.metadata = new NoBlockMetadata();
    }

    // ==================== Copy Methods ====================

    /**
     * Initializes the builder from an existing Equals comparison.
     */
    public ComparisonBuilder from(Equals node) {
        this.operand1 = node.getOperand1();
        this.operand2 = node.getOperand2();
        this.metadata = node.getMetadata();
        return this;
    }

    /**
     * Initializes the builder from an existing LessThan comparison.
     */
    public ComparisonBuilder from(LessThan node) {
        this.operand1 = node.getOperand1();
        this.operand2 = node.getOperand2();
        this.metadata = node.getMetadata();
        return this;
    }

    /**
     * Initializes the builder from an existing BiggerThan comparison.
     */
    public ComparisonBuilder from(BiggerThan node) {
        this.operand1 = node.getOperand1();
        this.operand2 = node.getOperand2();
        this.metadata = node.getMetadata();
        return this;
    }

    // ==================== With Methods ====================

    public ComparisonBuilder withOperand1(ComparableExpr op1) {
        this.operand1 = op1;
        return this;
    }

    public ComparisonBuilder withOperand2(ComparableExpr op2) {
        this.operand2 = op2;
        return this;
    }

    public ComparisonBuilder withMetadata(BlockMetadata meta) {
        this.metadata = meta;
        return this;
    }

    // ==================== Build Methods ====================

    public Equals buildEquals() {
        Equals result = new Equals(operand1, operand2, metadata);
        setParentNodes(result);
        return result;
    }

    public LessThan buildLessThan() {
        LessThan result = new LessThan(operand1, operand2, metadata);
        setParentNodes(result);
        return result;
    }

    public BiggerThan buildBiggerThan() {
        BiggerThan result = new BiggerThan(operand1, operand2, metadata);
        setParentNodes(result);
        return result;
    }

    private void setParentNodes(Object parent) {
        if (operand1 != null) {
            operand1.setParentNode((ASTNode) parent);
        }
        if (operand2 != null) {
            operand2.setParentNode((ASTNode) parent);
        }
    }

    // ==================== Static Utility Methods ====================

    /**
     * Checks if an expression is a literal value, including wrapped literals.
     * Real Scratch projects often store numbers as AsNumber(StringLiteral("10")).
     */
    public static boolean isLiteral(ComparableExpr expr) {
        // Direct literals
        if (expr instanceof NumberLiteral || expr instanceof StringLiteral) {
            return true;
        }
        // AsNumber wrapping a literal (common in real Scratch JSON)
        if (expr instanceof AsNumber asNum) {
            if (asNum.getOperand1() instanceof NumberLiteral) {
                return true;
            }
            if (asNum.getOperand1() instanceof StringLiteral sl) {
                // Check if string parses as a number
                try {
                    Double.parseDouble(sl.getText());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        // AsString wrapping a literal
        if (expr instanceof AsString asStr) {
            return asStr.getOperand1() instanceof StringLiteral
                    || asStr.getOperand1() instanceof NumberLiteral;
        }
        return false;
    }

    /**
     * Creates a matching literal for the given reference expression.
     * Handles both direct literals and wrapped literals.
     */
    public static ComparableExpr createMatchingLiteral(ComparableExpr reference) {
        // Direct literals
        if (reference instanceof NumberLiteral num) {
            return new NumberLiteral(num.getValue());
        }
        if (reference instanceof StringLiteral str) {
            return new StringLiteral(str.getText());
        }
        // AsNumber wrapping a literal
        if (reference instanceof AsNumber asNum) {
            if (asNum.getOperand1() instanceof NumberLiteral num) {
                return new NumberLiteral(num.getValue());
            }
            if (asNum.getOperand1() instanceof StringLiteral sl) {
                try {
                    double value = Double.parseDouble(sl.getText());
                    return new NumberLiteral(value);
                } catch (NumberFormatException e) {
                    return new StringLiteral(sl.getText());
                }
            }
        }
        // AsString wrapping a literal
        if (reference instanceof AsString asStr) {
            if (asStr.getOperand1() instanceof StringLiteral sl) {
                return new StringLiteral(sl.getText());
            }
            if (asStr.getOperand1() instanceof NumberLiteral num) {
                return new StringLiteral(String.valueOf(num.getValue()));
            }
        }
        return new NumberLiteral(0);
    }
}
