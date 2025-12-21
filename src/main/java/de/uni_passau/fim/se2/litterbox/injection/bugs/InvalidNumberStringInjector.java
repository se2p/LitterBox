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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.PointInDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects InvalidNumberString bug by replacing numeric parameters with invalid number strings.
 * <p>
 * Creates invalid numbers by inserting extra decimal points into the original number.
 * Uses immutable transformations to maintain AST immutability.
 */
public class InvalidNumberStringInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new InvalidNumberString();
    }

    // Binary numeric operations
    @Override
    public void visit(Add node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Add(replacement, n.getOperand2(), n.getMetadata()));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Add(n.getOperand1(), replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(Minus node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Minus(replacement, n.getOperand2(), n.getMetadata()));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Minus(n.getOperand1(), replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(Mult node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Mult(replacement, n.getOperand2(), n.getMetadata()));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Mult(n.getOperand1(), replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(Div node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Div(replacement, n.getOperand2(), n.getMetadata()));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Div(n.getOperand1(), replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(Mod node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Mod(replacement, n.getOperand2(), n.getMetadata()));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Mod(n.getOperand1(), replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    // Comparisons
    @Override
    public void visit(Equals node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Equals(replacement, n.getOperand2(), n.getMetadata()));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new Equals(n.getOperand1(), replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(LessThan node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new LessThan(replacement, n.getOperand2(), n.getMetadata()));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new LessThan(n.getOperand1(), replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(BiggerThan node) {
        if (node.getOperand1() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new BiggerThan(replacement, n.getOperand2(), n.getMetadata()));
        }
        if (node.getOperand2() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new BiggerThan(n.getOperand1(), replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    // Motion blocks
    @Override
    public void visit(MoveSteps node) {
        if (node.getSteps() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new MoveSteps(replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(SetXTo node) {
        if (node.getNum() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new SetXTo(replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(SetYTo node) {
        if (node.getNum() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new SetYTo(replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(PointInDirection node) {
        if (node.getDirection() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new PointInDirection(replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    // Control/common blocks
    @Override
    public void visit(WaitSeconds node) {
        if (node.getSeconds() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new WaitSeconds(replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        // Only inject in script/procedure context, not in SetStmtList (variable initialization)
        if (isInScriptContext(node) && node.getExpr() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new ChangeVariableBy(n.getIdentifier(), replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    @Override
    public void visit(SetVariableTo node) {
        // Only inject in script/procedure context, not in SetStmtList (variable initialization)
        if (isInScriptContext(node) && node.getExpr() instanceof NumberLiteral numLit) {
            NumExpr replacement = new AsNumber(new StringLiteral(makeInvalidNumber(numLit.getValue())));
            addImmutableMutation(node, n -> new SetVariableTo(n.getIdentifier(), replacement, n.getMetadata()));
        }
        super.visit(node);
    }

    /**
     * Checks if the node is within a Script or ProcedureDefinition context.
     * This prevents injecting bugs in variable initialization (SetStmtList).
     */
    private boolean isInScriptContext(ASTNode node) {
        ASTNode parent = node.getParentNode();
        while (parent != null) {
            if (parent instanceof Script || parent instanceof ProcedureDefinition) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
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
