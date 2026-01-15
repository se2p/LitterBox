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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CustomBlockWithTermination;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;

import java.util.List;

/**
 * Injects the CustomBlockWithTermination bug by appending StopAll to procedure bodies.
 * Only targets procedures that are called in non-tail position.
 */
public class CustomBlockWithTerminationInjector extends AbstractCustomBlockInjector {

    private final IssueFinder issueFinder = new CustomBlockWithTermination();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    protected boolean shouldMutate(ProcedureDefinition def) {
        // Skip if last statement is already a termination or forever loop
        List<Stmt> stmts = def.getStmtList().getStmts();
        if (!stmts.isEmpty()) {
            Stmt lastStmt = stmts.getLast();
            return !(lastStmt instanceof TerminationStmt) && !(lastStmt instanceof RepeatForeverStmt);
        }
        return true;
    }

    @Override
    protected void applyMutation(ProcedureDefinition clonedDef, String proccode) {
        Stmt stopAll = new StopAll(NonDataBlockMetadata.emptyNonBlockMetadata());
        clonedDef.getStmtList().getStmts().add(stopAll);
        stopAll.setParentNode(clonedDef.getStmtList());
    }
}

