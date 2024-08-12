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
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
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
    private List<String> proceduresWithTermination;
    private List<CallStmt> calledProcedures;
    private boolean insideProcedure;
    private int ifAndIfElseCounter = 0;
    private boolean afterWaitUntil = false;

    private void checkCalls() {
        for (CallStmt calledProcedure : calledProcedures) {
            if (proceduresWithTermination.contains(calledProcedure.getIdent().getName())) {
                Hint hint = new Hint(NAME);
                hint.setParameter(Hint.METHOD, calledProcedure.getIdent().getName());
                addIssue(calledProcedure, calledProcedure.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(ActorDefinition actor) {
        calledProcedures = new ArrayList<>();
        proceduresWithTermination = new ArrayList<>();
        super.visit(actor);
        checkCalls();
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentProcedureName = procMap.get(node.getIdent()).getName();
        afterWaitUntil = false;
        super.visit(node);
        insideProcedure = false;
    }

    @Override
    public void visit(IfThenStmt node) {
        ifAndIfElseCounter++;
        visitChildren(node);
        ifAndIfElseCounter--;
    }

    @Override
    public void visit(IfElseStmt node) {
        ifAndIfElseCounter++;
        visitChildren(node);
        ifAndIfElseCounter--;
    }

    @Override
    public void visit(WaitUntil node) {
        afterWaitUntil = true;
        visitChildren(node);
    }

    @Override
    public void visit(DeleteClone node) {
        if (insideProcedure && ifAndIfElseCounter == 0 && !afterWaitUntil) {
            proceduresWithTermination.add(currentProcedureName);
        }
        visitChildren(node);
    }

    @Override
    public void visit(StopAll node) {
        if (insideProcedure && ifAndIfElseCounter == 0 && !afterWaitUntil) {
            proceduresWithTermination.add(currentProcedureName);
        }
        visitChildren(node);
    }

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        for (int i = 0; i < stmts.size() - 1; i++) {

            if (stmts.get(i) instanceof CallStmt callStmt) {
                calledProcedures.add(callStmt);
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
