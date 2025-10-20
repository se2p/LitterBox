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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetYTo;
import de.uni_passau.fim.se2.litterbox.ast.parser.KeyCode;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.List;

public class KeySetPosition extends AbstractIssueFinder {
    public static final String NAME = "key_set_position";
    private boolean inCondition = false;
    private NumberLiteral lastKeyValue = null;

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof GreenFlag || node.getEvent() instanceof StartedAsClone) {
            inCondition = false;
            lastKeyValue = null;
            super.visit(node);
        } else if (node.getEvent() instanceof KeyPressed keyPressed && !node.getStmtList().getStmts().isEmpty()) {
            currentScript = node;
            Key keyNode = keyPressed.getKey();
            NumberLiteral keyNum = (NumberLiteral) keyNode.getKey();
            int key = (int) keyNum.getValue();
            if (
                    key == KeyCode.DOWN_ARROW.getKeycode()
                            || key == KeyCode.UP_ARROW.getKeycode()
                            || key == KeyCode.LEFT_ARROW.getKeycode()
                            || key == KeyCode.RIGHT_ARROW.getKeycode()
            ) {
                if (node.getStmtList().getStmts().size() == 1) {
                    checkStmt(node.getStmtList().getStmts().getFirst(), key);
                } else {
                    checkNumberStmts(node.getStmtList().getStmts(), key, 2); //used 2 as a placeholder
                }
            }
        }
    }

    private void checkNumberStmts(List<Stmt> stmts, int key, int numberOfStmts) {
        boolean foundSet;
        for (int i = 0; i < numberOfStmts && i < stmts.size(); i++) {
            foundSet = checkStmt(stmts.get(i), key);
            if (foundSet) {
                break;
            }
        }
    }

    private boolean checkStmt(Stmt stmt, int key) {
        if (key == KeyCode.DOWN_ARROW.getKeycode() || key == KeyCode.UP_ARROW.getKeycode()) {
            if (stmt instanceof SetYTo) {
                Hint hint = Hint.fromKey(NAME);
                hint.setParameter("XY", "y");
                if (key == KeyCode.DOWN_ARROW.getKeycode()) {
                    hint.setParameter(Hint.HINT_KEY, IssueTranslator.getInstance().getInfo("down_arrow"));
                } else {
                    hint.setParameter(Hint.HINT_KEY, IssueTranslator.getInstance().getInfo("up_arrow"));
                }
                addIssue(stmt, stmt.getMetadata(), hint);
                return true;
            }
        } else if (key == KeyCode.LEFT_ARROW.getKeycode() || key == KeyCode.RIGHT_ARROW.getKeycode()) {
            if (stmt instanceof SetXTo) {
                Hint hint = Hint.fromKey(NAME);
                hint.setParameter("XY", "x");
                if (key == KeyCode.LEFT_ARROW.getKeycode()) {
                    hint.setParameter(Hint.HINT_KEY, IssueTranslator.getInstance().getInfo("left_arrow"));
                } else {
                    hint.setParameter(Hint.HINT_KEY, IssueTranslator.getInstance().getInfo("right_arrow"));
                }
                addIssue(stmt, stmt.getMetadata(), hint);
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        //NOP should not look in Procedures
    }

    @Override
    public void visit(IfThenStmt node) {
        inCondition = true;
        node.getBoolExpr().accept(this);
        inCondition = false;
        if (lastKeyValue != null) {
            if (node.getThenStmts().getStmts().size() == 1) {
                checkStmt(node.getThenStmts().getStmts().getFirst(), (int) lastKeyValue.getValue());
            } else {
                // used 2 as a placeholder
                checkNumberStmts(node.getThenStmts().getStmts(), (int) lastKeyValue.getValue(), 2);
            }
        }
        lastKeyValue = null;
        node.getThenStmts().accept(this);
    }

    @Override
    public void visit(Key node) {
        if (inCondition && node.getKey() instanceof NumberLiteral numberLiteral) {
            lastKeyValue = numberLiteral;
        }
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
