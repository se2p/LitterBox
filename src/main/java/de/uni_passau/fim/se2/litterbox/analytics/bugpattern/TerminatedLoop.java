/*
 * Copyright (C) 2020 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;

import java.util.List;

/**
 * TerminatedLoop is a bug pattern which occurs if a loop contains a StopAll or StopThisScript block which
 * is not guarded by some condition. This can be identified simply looking at the last child in the statement list of a
 * loop and checking if it is a stop block. If that is the case the loop will only execute once and all blocks
 * succeeding the loop (in the case of a "repeat times" or "repeat until") will not be executed either.
 */
public class TerminatedLoop extends AbstractIssueFinder {

    private static final String NAME = "terminated_loop";

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        StmtList children = node.getStmtList();
        checkLoopChildren(children);
        visitChildren(node);
    }

    private void checkLoopChildren(StmtList children) {
        if (children.hasStatements()) {
            List<Stmt> stmts = children.getStmts();
            Stmt last = stmts.get(stmts.size() - 1);
            if (last instanceof StopAll) {
                addIssue(last, ((StopAll) last).getMetadata());
            } else if (last instanceof StopThisScript) {
                addIssue(last, ((StopThisScript) last).getMetadata());
            }
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        StmtList children = node.getStmtList();
        checkLoopChildren(children);
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        StmtList children = node.getStmtList();
        checkLoopChildren(children);
        visitChildren(node);
    }
}
