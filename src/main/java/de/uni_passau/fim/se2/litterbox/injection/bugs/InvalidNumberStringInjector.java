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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InvalidNumberString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects InvalidNumberString bug by replacing numeric parameters with invalid number strings.
 * <p>
 * Creates invalid numbers by inserting extra decimal points into the original number.
 * Uses immutable transformations to maintain AST immutability.
 */
public class InvalidNumberStringInjector extends AbstractBugInjector {

    private final CloneVisitor cloner = new CloneVisitor();

    @Override
    public IssueFinder getIssueFinder() {
        return new InvalidNumberString();
    }

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
                replaceWith(node, n -> new AsNumber(new StringLiteral(invalidNum)));
            }
        }
        super.visit(node);
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
            // Skip Infinity and NaN - corrupting them produces strings with letters
            // that the finder won't detect (only detects [0-9eE.+-]+)
            return !Double.isInfinite(value) && !Double.isNaN(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Creates an invalid number string from a string representation.
     * Inserts extra decimal points to make the number unparseable.
     */
    private String makeInvalidNumberString(String numStr) {
        // Count existing decimal points
        long dotCount = numStr.chars().filter(c -> c == '.').count();

        if (dotCount == 0) {
            // No decimal point: insert two dots to make invalid
            // e.g., "10" -> "1.0."
            if (numStr.length() >= 2) {
                int mid = numStr.length() / 2;
                return numStr.substring(0, mid) + "." + numStr.substring(mid) + ".";
            } else {
                return numStr + "..";
            }
        } else {
            // Has one decimal point: insert another dot
            // e.g., "3.14" -> "3.1.4"
            int dotIndex = numStr.indexOf('.');
            int insertPos = dotIndex + 2;
            if (insertPos < numStr.length()) {
                return numStr.substring(0, insertPos) + "." + numStr.substring(insertPos);
            } else {
                return numStr + ".";
            }
        }
    }

    // Binary numeric operations
    @Override
    public void visit(Add node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            // Clone operand2 and metadata to avoid referencing original tree
            NumExpr clonedOp2 = cloner.apply(node.getOperand2());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Add(replacement, clonedOp2, clonedMeta));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            // Clone operand1 and metadata to avoid referencing original tree
            NumExpr clonedOp1 = cloner.apply(node.getOperand1());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Add(clonedOp1, replacement, clonedMeta));
        }
        super.visit(node);
    }

    @Override
    public void visit(Minus node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            NumExpr clonedOp2 = cloner.apply(node.getOperand2());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Minus(replacement, clonedOp2, clonedMeta));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            NumExpr clonedOp1 = cloner.apply(node.getOperand1());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Minus(clonedOp1, replacement, clonedMeta));
        }
        super.visit(node);
    }

    @Override
    public void visit(Mult node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            NumExpr clonedOp2 = cloner.apply(node.getOperand2());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Mult(replacement, clonedOp2, clonedMeta));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            NumExpr clonedOp1 = cloner.apply(node.getOperand1());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Mult(clonedOp1, replacement, clonedMeta));
        }
        super.visit(node);
    }

    @Override
    public void visit(Div node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            NumExpr clonedOp2 = cloner.apply(node.getOperand2());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Div(replacement, clonedOp2, clonedMeta));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            NumExpr clonedOp1 = cloner.apply(node.getOperand1());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Div(clonedOp1, replacement, clonedMeta));
        }
        super.visit(node);
    }

    @Override
    public void visit(Mod node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            NumExpr clonedOp2 = cloner.apply(node.getOperand2());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Mod(replacement, clonedOp2, clonedMeta));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            NumExpr clonedOp1 = cloner.apply(node.getOperand1());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Mod(clonedOp1, replacement, clonedMeta));
        }
        super.visit(node);
    }

    // Comparisons
    @Override
    public void visit(Equals node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            ComparableExpr clonedOp2 = cloner.apply(node.getOperand2());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Equals(replacement, clonedOp2, clonedMeta));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            ComparableExpr clonedOp1 = cloner.apply(node.getOperand1());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new Equals(clonedOp1, replacement, clonedMeta));
        }
        super.visit(node);
    }

    @Override
    public void visit(LessThan node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            ComparableExpr clonedOp2 = cloner.apply(node.getOperand2());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new LessThan(replacement, clonedOp2, clonedMeta));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            ComparableExpr clonedOp1 = cloner.apply(node.getOperand1());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new LessThan(clonedOp1, replacement, clonedMeta));
        }
        super.visit(node);
    }

    @Override
    public void visit(BiggerThan node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            ComparableExpr clonedOp2 = cloner.apply(node.getOperand2());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new BiggerThan(replacement, clonedOp2, clonedMeta));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            ComparableExpr clonedOp1 = cloner.apply(node.getOperand1());
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new BiggerThan(clonedOp1, replacement, clonedMeta));
        }
        super.visit(node);
    }

    // Motion blocks
    @Override
    public void visit(MoveSteps node) {
        if (node.getSteps() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new MoveSteps(replacement, clonedMeta));
        }
        super.visit(node);
    }

    @Override
    public void visit(SetXTo node) {
        if (node.getNum() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new SetXTo(replacement, clonedMeta));
        }
        super.visit(node);
    }

    @Override
    public void visit(SetYTo node) {
        if (node.getNum() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new SetYTo(replacement, clonedMeta));
        }
        super.visit(node);
    }

    @Override
    public void visit(PointInDirection node) {
        if (node.getDirection() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new PointInDirection(replacement, clonedMeta));
        }
        super.visit(node);
    }

    // Control/common blocks
    @Override
    public void visit(WaitSeconds node) {
        if (node.getSeconds() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
            replaceWith(node, n -> new WaitSeconds(replacement, clonedMeta));
        }
        super.visit(node);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        // Only inject in script/procedure context, not in SetStmtList (variable initialization)
        if (isInScriptContext(node) && node.getExpr() instanceof NumberLiteral numLit) {
            double value = numLit.getValue();
            // Skip Infinity and NaN - corrupting them produces strings with letters
            if (!Double.isInfinite(value) && !Double.isNaN(value)) {
                NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(value)));
                var clonedIdent = cloner.apply(node.getIdentifier());
                BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
                replaceWith(node, n -> new ChangeVariableBy(clonedIdent, replacement, clonedMeta));
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(SetVariableTo node) {
        // Only inject in script/procedure context, not in SetStmtList (variable initialization)
        if (isInScriptContext(node) && node.getExpr() instanceof NumberLiteral numLit) {
            double value = numLit.getValue();
            // Skip Infinity and NaN - corrupting them produces strings with letters
            if (!Double.isInfinite(value) && !Double.isNaN(value)) {
                NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(value)));
                var clonedIdent = cloner.apply(node.getIdentifier());
                BlockMetadata clonedMeta = cloner.apply(node.getMetadata());
                replaceWith(node, n -> new SetVariableTo(clonedIdent, replacement, clonedMeta));
            }
        }
        super.visit(node);
    }

    /**
     * Creates an invalid number string by inserting extra decimal points.
     * <p>
     * Examples:
     * - "10" -> "1.0." (add two dots)
     * - "3.14" -> "3.1.4" (add one dot)
     */
    private String makeInvalidNumber(double value) {
        String numStr = String.valueOf(value);

        // Count existing decimal points
        long dotCount = numStr.chars().filter(c -> c == '.').count();

        if (dotCount == 0) {
            // No decimal point: insert two dots to make invalid
            // e.g., "10" -> "1.0."
            if (numStr.length() >= 2) {
                int mid = numStr.length() / 2;
                return numStr.substring(0, mid) + "." + numStr.substring(mid) + ".";
            } else {
                return numStr + "..";
            }
        } else {
            // Has one decimal point: insert another dot
            // e.g., "3.14" -> "3.1.4"
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
