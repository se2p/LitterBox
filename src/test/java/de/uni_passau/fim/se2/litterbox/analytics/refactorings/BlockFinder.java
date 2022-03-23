/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractRefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.DeleteControlBlock;

import java.util.List;

public class BlockFinder extends AbstractRefactoringFinder {

    private static final String NAME = "block_finder";

    @Override
    public void visit(ScriptList node) {
        final List<Script> scriptList = node.getScriptList();

        for (Script script : scriptList) {
            List<Stmt> stmts = script.getStmtList().getStmts();
            for (Stmt stmt : stmts) {
                if (stmt instanceof ControlStmt) {
                    refactorings.add(new DeleteControlBlock((ControlStmt) stmt));
                }
            }
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
