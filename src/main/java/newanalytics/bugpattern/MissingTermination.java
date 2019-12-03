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
package newanalytics.bugpattern;

import java.util.ArrayList;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.expression.bool.UnspecifiedBoolExpr;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.control.IfElseStmt;
import scratch.ast.model.statement.control.IfThenStmt;
import scratch.ast.model.statement.control.RepeatForeverStmt;
import scratch.ast.model.statement.control.RepeatTimesStmt;
import scratch.ast.model.statement.control.UntilStmt;

/**
 * Checks for missing statements in repeat-until blocks.
 */
public class MissingTermination implements IssueFinder {
    public static final String NAME = "missing_termination";
    public static final String SHORT_NAME = "msstrm";
    private List<String> found;
    private int counter;

    public MissingTermination() {
        found = new ArrayList<>();
        counter = 0;
    }

    @Override
    public IssueReport check(Program program) {
        List<ActorDefinition> actorDefs = program.getActorDefinitionList().getDefintions();
        for (int i = 0; i < actorDefs.size(); i++) {
            List<Script> scripts = actorDefs.get(i).getScripts().getScriptList();
            for (int j = 0; j < scripts.size(); j++) {
                List<Stmt> stmts = scripts.get(0).getStmtList().getStmts().getListOfStmt();
                if (stmts.size() > 0) {
                    checkMissTermination(stmts, actorDefs.get(i).getIdent().getName());
                }
            }
        }

        String notes = "All 'repeat until' blocks terminating correctly.";
        if (counter > 0) {
            notes = "Some 'repeat until' blocks have no termination statement.";
        }
        return new IssueReport(NAME, counter, found, notes);
    }

    private void checkMissTermination(List<Stmt> stmts, String actorName) {
        for (int i = 0; i < stmts.size(); i++) {
            if (stmts.get(i) instanceof UntilStmt) {
                if (((UntilStmt) stmts.get(i)).getBoolExpr() instanceof UnspecifiedBoolExpr) {
                    counter++;
                    found.add(actorName);
                }
            } else if (stmts.get(i) instanceof IfThenStmt) {
                checkMissTermination(((IfThenStmt) stmts.get(i)).getThenStmts().getStmts().getListOfStmt(), actorName);
            } else if (stmts.get(i) instanceof IfElseStmt) {
                checkMissTermination(((IfElseStmt) stmts.get(i)).getStmtList().getStmts().getListOfStmt(), actorName);
                checkMissTermination(((IfElseStmt) stmts.get(i)).getElseStmts().getStmts().getListOfStmt(), actorName);
            } else if (stmts.get(i) instanceof RepeatTimesStmt) {
                checkMissTermination(((RepeatTimesStmt) stmts.get(i)).getStmtList().getStmts().getListOfStmt(),
                        actorName);
            } else if (stmts.get(i) instanceof RepeatForeverStmt){
                checkMissTermination(((RepeatForeverStmt) stmts.get(i)).getStmtList().getStmts().getListOfStmt(),
                        actorName);
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
