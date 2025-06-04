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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;

/**
 * The ControlledBroadcastOrStop pattern means inside a loop a certain condition is checked (if-statement). If the condition
 * is met, a stop-block or a broadcast is triggered.
 */
public class ControlledBroadcastOrStop extends AbstractIssueFinder {

    public static final String NAME = "controlled_broadcast_or_stop";
    private boolean inLoop = false;

    @Override
    public void visit(RepeatForeverStmt node) {
        inLoop = true;

        // makes sense only for loops that contain other blocks along with the if-stmt
        if (node.getStmtList().getStmts().size() > 1) {
            visitChildren(node);
        }
        inLoop = false;
    }

    @Override
    public void visit(IfElseStmt node) {
        if (inLoop) {
            checkStmtList(node.getThenStmts());
        }
    }

    @Override
    public void visit(IfThenStmt node) {
        if (inLoop) {
            checkStmtList(node.getThenStmts());
        }
    }

    private void checkStmtList(StmtList node) {
        node.getStmts().forEach(stmt -> {
            if (stmt instanceof StopAll || stmt instanceof StopThisScript
                    || stmt instanceof StopOtherScriptsInSprite || stmt instanceof Broadcast || stmt instanceof BroadcastAndWait) {
                addIssue(stmt, stmt.getMetadata(), IssueSeverity.LOW);
            }
        });
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
