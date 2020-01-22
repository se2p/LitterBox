/*
 * Copyright (C) 2019 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.analytics.IssueTool;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for missing statements in repeat-until blocks.
 */
public class MissingTerminationCondition implements IssueFinder {
    public static final String NAME = "missing_termination";
    public static final String SHORT_NAME = "mssTerm";
    private List<String> found;
    private int counter;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = new ArrayList<>();
        counter = 0;
        List<ActorDefinition> actorDefs = program.getActorDefinitionList().getDefintions();
        for (ActorDefinition actorDef : actorDefs) {
            List<Script> scripts = actorDef.getScripts().getScriptList();
            for (Script script : scripts) {
                List<Stmt> stmts = script.getStmtList().getStmts().getListOfStmt();
                if (stmts.size() > 0) {
                    checkMissTermination(stmts, actorDef.getIdent().getName());
                }
            }
        }

        String notes = "All 'repeat until' blocks terminating correctly.";
        if (counter > 0) {
            notes = "Some 'repeat until' blocks have no termination statement.";
        }
        return new IssueReport(NAME, counter, IssueTool.getOnlyUniqueActorList(found), notes);
    }

    private void checkMissTermination(List<Stmt> stmts, String actorName) {
        for (Stmt stmt : stmts) {
            if (stmt instanceof UntilStmt) {
                if (((UntilStmt) stmt).getBoolExpr() instanceof UnspecifiedBoolExpr) {
                    counter++;
                    found.add(actorName);
                }
            } else if (stmt instanceof IfThenStmt) {
                checkMissTermination(((IfThenStmt) stmt).getThenStmts().getStmts().getListOfStmt(), actorName);
            } else if (stmt instanceof IfElseStmt) {
                checkMissTermination(((IfElseStmt) stmt).getStmtList().getStmts().getListOfStmt(), actorName);
                checkMissTermination(((IfElseStmt) stmt).getElseStmts().getStmts().getListOfStmt(), actorName);
            } else if (stmt instanceof RepeatTimesStmt) {
                checkMissTermination(((RepeatTimesStmt) stmt).getStmtList().getStmts().getListOfStmt(),
                        actorName);
            } else if (stmt instanceof RepeatForeverStmt) {
                checkMissTermination(((RepeatForeverStmt) stmt).getStmtList().getStmts().getListOfStmt(),
                        actorName);
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
