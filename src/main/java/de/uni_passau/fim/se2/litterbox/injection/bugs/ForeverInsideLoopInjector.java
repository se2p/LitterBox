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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ForeverInsideLoop;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects the ForeverInsideLoop bug by appending a forever loop inside outer loops.
 * Only loops that have successor statements are considered (otherwise the bug has no effect).
 */
public class ForeverInsideLoopInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new ForeverInsideLoop();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    private void inject(LoopStmt originalLoop) {
        // Only inject if the loop has successor statements
        if (!hasSuccessorStatements(originalLoop)) {
            return;
        }

        if (originalLoop.getStmtList().hasStatements()) {
            modifyClonedNode(originalLoop, clonedLoop -> {
                List<Stmt> stmts = clonedLoop.getStmtList().getStmts();

                // Wrap the last statement in a forever loop
                int lastIndex = stmts.size() - 1;
                Stmt lastStmt = stmts.remove(lastIndex);

                List<Stmt> foreverBodyStmts = new ArrayList<>();
                foreverBodyStmts.add(lastStmt);
                StmtList foreverBody = new StmtList(foreverBodyStmts);
                lastStmt.setParentNode(foreverBody);

                RepeatForeverStmt foreverStmt = nodeFactory.createForever(foreverBody);
                foreverBody.setParentNode(foreverStmt);

                stmts.add(foreverStmt);
                foreverStmt.setParentNode(clonedLoop.getStmtList());
            });
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        inject(node);
        super.visit(node);
    }

    @Override
    public void visit(UntilStmt node) {
        inject(node);
        super.visit(node);
    }
}
