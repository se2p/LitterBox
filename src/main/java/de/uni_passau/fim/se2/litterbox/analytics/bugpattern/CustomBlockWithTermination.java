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
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;

import java.util.ArrayList;
import java.util.List;

/**
 * If a custom block contains a Stop all or Delete this clone and the custom block is called in the
 * middle of another script, the script will never reach the blocks following the call.
 */
public class CustomBlockWithTermination extends AbstractIssueFinder {
    public static final String NAME = "custom_block_with_termination";
    private String currentProcedureName;
    private List<String> proceduresWithForever;
    private List<CallStmt> calledProcedures;
    private boolean insideProcedure;

    private void checkCalls() {
        for (CallStmt calledProcedure : calledProcedures) {
            if (proceduresWithForever.contains(calledProcedure.getIdent().getName())) {
                addIssue(calledProcedure, calledProcedure.getMetadata());
            }
        }
    }

    @Override
    public void visit(ActorDefinition actor) {
        calledProcedures = new ArrayList<>();
        proceduresWithForever = new ArrayList<>();
        super.visit(actor);
        checkCalls();
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentProcedureName = procMap.get(node.getIdent()).getName();
        super.visit(node);
        insideProcedure = false;
    }

    @Override
    public void visit(DeleteClone node) {
        if (insideProcedure) {
            proceduresWithForever.add(currentProcedureName);
        }
        visitChildren(node);
    }

    @Override
    public void visit(StopAll node) {
        if (insideProcedure) {
            proceduresWithForever.add(currentProcedureName);
        }
        visitChildren(node);
    }

    @Override
    public void visit(StmtList node) {
        for (Stmt stmt : node.getStmts()) {
            if (stmt instanceof CallStmt) {
                calledProcedures.add((CallStmt) stmt);
            }
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
