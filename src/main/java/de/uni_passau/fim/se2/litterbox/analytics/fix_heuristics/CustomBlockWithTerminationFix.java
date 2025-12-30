/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.CustomBlockWithTermination;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fix heuristic for the CustomBlockWithTermination bug.
 *
 * <p>The bug is reported when a call to a procedure containing StopAll/DeleteClone
 * is not in the last position, blocking subsequent code.
 * The bug is fixed if the procedure no longer terminates, or the call is now last.
 */
public class CustomBlockWithTerminationFix extends AbstractFixHeuristic {

    private String currentProcedureName;
    private Set<String> proceduresWithTermination;
    private CallStmt targetCall;
    private boolean insideProcedure;
    private boolean isLastStatement;
    private Script foundScript;
    private ProcedureDefinition foundProcedure;

    private final IssueFinder relatedBugFinder = new CustomBlockWithTermination();

    public CustomBlockWithTerminationFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public void visit(ActorDefinition actor) {
        proceduresWithTermination = new HashSet<>();
        targetCall = null;
        isLastStatement = false;
        super.visit(actor);

        if (targetCall != null) {
            String procName = targetCall.getIdent().getName();
            // Fixed if last statement OR procedure no longer terminates
            if (isLastStatement || !proceduresWithTermination.contains(procName)) {
                this.currentScript = foundScript;
                this.currentProcedure = foundProcedure;
                addIssue(targetCall, targetCall.getMetadata());
            }
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        if (procMap.containsKey(node.getIdent())) {
            currentProcedureName = procMap.get(node.getIdent()).getName();
        }
        super.visit(node);
        insideProcedure = false;
    }

    @Override
    public void visit(StopAll node) {
        if (insideProcedure && currentProcedureName != null) {
            proceduresWithTermination.add(currentProcedureName);
        }
        super.visit(node);
    }

    @Override
    public void visit(DeleteClone node) {
        if (insideProcedure && currentProcedureName != null) {
            proceduresWithTermination.add(currentProcedureName);
        }
        super.visit(node);
    }

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        for (int i = 0; i < stmts.size(); i++) {
            Stmt stmt = stmts.get(i);
            if (stmt instanceof CallStmt callStmt && matchesBugLocation(callStmt)) {
                targetCall = callStmt;
                isLastStatement = (i == stmts.size() - 1);
                foundScript = currentScript;
                foundProcedure = currentProcedure;
            }
        }
        super.visit(node);
    }
}
