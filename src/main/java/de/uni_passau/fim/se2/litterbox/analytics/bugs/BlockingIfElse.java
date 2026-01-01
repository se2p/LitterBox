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

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockingIfElse extends AbstractIssueFinder {
    public static final String NAME = "blocking_if_else";
    public static final String INSIDE_LOOP = "blocking_if_else_inside_loop";
    private int loopCounter;

    @Override
    public void visit(Script node) {
        loopCounter = 0;
        super.visit(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        loopCounter++;
        visitChildren(node);
        loopCounter--;
    }

    @Override
    public void visit(UntilStmt node) {
        loopCounter++;
        visitChildren(node);
        loopCounter--;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        loopCounter++;
        visitChildren(node);
        loopCounter--;
    }

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        for (int i = 0; i < stmts.size(); i++) {
            if ((loopCounter > 0 || i < stmts.size() - 1) && stmts.get(i) instanceof IfElseStmt ifElse) {
                if (isBlocking(ifElse.getThenStmts().getStmts())) {
                    if (isBlocking(ifElse.getElseStmts().getStmts())) {
                        Hint hint;
                        if (i < stmts.size() - 1) {
                            hint = Hint.fromKey(NAME);
                        } else {
                            hint = Hint.fromKey(INSIDE_LOOP);
                        }
                        addIssue(ifElse, ifElse.getMetadata(), IssueSeverity.MEDIUM, hint);
                    }
                }
            }
        }
        super.visit(node);
    }

    /**
     * Checks if a list of statements is "blocking", meaning execution cannot proceed past it.
     * A list is blocking if it contains a terminal statement (stop, delete clone, forever loop)
     * or an if-else statement where both branches block.
     *
     * @param subStmts The list of statements to check.
     * @return true if the statements block execution.
     */
    public static boolean isBlocking(List<Stmt> subStmts) {
        if (subStmts.isEmpty()) {
            return false;
        }

        for (Stmt subStmt : subStmts) {
            // Explicit terminal statements
            if (subStmt instanceof StopAll || subStmt instanceof StopThisScript
                    || subStmt instanceof DeleteClone || subStmt instanceof RepeatForeverStmt) {
                return true;
            }

            // Recursive check for if-else: blocks if both branches block
            if (subStmt instanceof IfElseStmt ifElseStmt) {
                if (isBlocking(ifElseStmt.getThenStmts().getStmts())
                        && isBlocking(ifElseStmt.getElseStmts().getStmts())) {
                    return true;
                }
            }
        }

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
        keys.add(INSIDE_LOOP);
        return keys;
    }
}
