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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.parser.KeyCode;

import java.util.List;

public class KeySetPosition extends AbstractIssueFinder {
    public static final String NAME = "key_set_position";
    private boolean inCondition = false;
    private NumberLiteral lastKeyValue = null;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            return;
        }

        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            inCondition = false;
            lastKeyValue = null;
            super.visit(node);
        } else if (node.getEvent() instanceof KeyPressed event && !node.getStmtList().getStmts().isEmpty()) {
            currentScript = node;
            int key = resolveKey(event.getKey());
            if (isSupportedKey(key)) {
                checkStmts(node.getStmtList().getStmts(), key);
            }
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        inCondition = true;
        node.getBoolExpr().accept(this);
        inCondition = false;

        if (lastKeyValue != null) {
            int key = (int) lastKeyValue.getValue();
            if (isSupportedKey(key)) {
                checkStmts(node.getThenStmts().getStmts(), key);
            }
        }

        lastKeyValue = null;
        node.getThenStmts().accept(this);
    }

    private void checkStmts(List<Stmt> stmts, int key) {
        // Finder matches only the first 2 statements of a script/then-branch
        for (int i = 0; i < Math.min(stmts.size(), 2); i++) {
            if (reportIfBuggy(stmts.get(i), key)) {
                break;
            }
        }
    }

    private boolean reportIfBuggy(Stmt stmt, int key) {
        String axis = null;
        String keyLabel = null;

        if (isVerticalKey(key) && isVerticalMotion(stmt)) {
            axis = "y";
            keyLabel = getVerticalKeyLabel(key);
        } else if (isHorizontalKey(key) && isHorizontalMotion(stmt)) {
            axis = "x";
            keyLabel = getHorizontalKeyLabel(key);
        }

        if (axis != null) {
            Hint hint = Hint.fromKey(NAME);
            hint.setParameter("XY", axis);
            hint.setParameter(Hint.HINT_KEY, new HintPlaceholder.Value(keyLabel));
            addIssue(stmt, stmt.getMetadata(), hint);
            return true;
        }
        return false;
    }

    private boolean isVerticalMotion(Stmt stmt) {
        // Only flag absolute positioning - relative motion (ChangeYBy) is the correct approach
        return stmt instanceof SetYTo;
    }

    private boolean isHorizontalMotion(Stmt stmt) {
        // Only flag absolute positioning - relative motion (ChangeXBy) is the correct approach
        return stmt instanceof SetXTo;
    }

    private boolean isVerticalKey(int key) {
        return key == KeyCode.UP_ARROW.getKeycode() || key == KeyCode.DOWN_ARROW.getKeycode()
                || key == 'w' || key == 'W' || key == 's' || key == 'S';
    }

    private boolean isHorizontalKey(int key) {
        return key == KeyCode.LEFT_ARROW.getKeycode() || key == KeyCode.RIGHT_ARROW.getKeycode()
                || key == 'a' || key == 'A' || key == 'd' || key == 'D';
    }

    private String getVerticalKeyLabel(int key) {
        if (key == KeyCode.UP_ARROW.getKeycode()) {
            return "up arrow";
        }
        if (key == KeyCode.DOWN_ARROW.getKeycode()) {
            return "down arrow";
        }
        return String.valueOf((char) key).toLowerCase();
    }

    private String getHorizontalKeyLabel(int key) {
        if (key == KeyCode.LEFT_ARROW.getKeycode()) {
            return "left arrow";
        }
        if (key == KeyCode.RIGHT_ARROW.getKeycode()) {
            return "right arrow";
        }
        return String.valueOf((char) key).toLowerCase();
    }

    private int resolveKey(Key keyNode) {
        ASTNode expr = keyNode.getKey();
        if (expr instanceof AsNumber asNumber) {
            expr = asNumber.getOperand1();
        }

        if (expr instanceof NumberLiteral num) {
            return (int) num.getValue();
        }
        if (expr instanceof StringLiteral str) {
            return getKeyCodeFromName(str.getText());
        }
        return -1;
    }

    @Override
    public void visit(Key node) {
        if (inCondition) {
            int key = resolveKey(node);
            if (key != -1) {
                lastKeyValue = new NumberLiteral(key);
            }
        }
    }

    private int getKeyCodeFromName(String keyName) {
        return switch (keyName.toLowerCase()) {
            case "up arrow" -> KeyCode.UP_ARROW.getKeycode();
            case "down arrow" -> KeyCode.DOWN_ARROW.getKeycode();
            case "left arrow" -> KeyCode.LEFT_ARROW.getKeycode();
            case "right arrow" -> KeyCode.RIGHT_ARROW.getKeycode();
            default -> keyName.length() == 1 ? keyName.toLowerCase().charAt(0) : -1;
        };
    }

    private boolean isSupportedKey(int key) {
        return isVerticalKey(key) || isHorizontalKey(key);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        // NOP
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
    public boolean isSubsumedBy(Issue first, Issue other) {
        if (first.getFinder() != this) {
            return super.isSubsumedBy(first, other);
        }

        if (other.getFinder() instanceof MissingLoopSensing) {
            ASTNode node = first.getCodeLocation().getParentNode().getParentNode();
            if (node instanceof IfThenStmt ifThenStmt) {
                return ifThenStmt.getBoolExpr() == (other.getCodeLocation());
            }
        }
        return false;
    }
}
