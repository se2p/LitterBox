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
package newanalytics.smells;

import java.util.ArrayList;
import java.util.List;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.event.GreenFlag;
import scratch.ast.model.expression.bool.BoolExpr;
import scratch.ast.model.expression.bool.IsKeyPressed;
import scratch.ast.model.expression.bool.IsMouseDown;
import scratch.ast.model.expression.bool.Touching;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.control.IfElseStmt;
import scratch.ast.model.statement.control.IfThenStmt;
import scratch.ast.model.statement.control.RepeatForeverStmt;
import scratch.ast.model.statement.control.RepeatTimesStmt;

/**
 * Checks for missing loops in event based actions.
 */
public class MissingForever implements IssueFinder {
//TODO Add check for Until loops
    private String name = "missing_forever_loop";
    private List<String> found;
    private int counter;

    public MissingForever() {
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
                if (stmts.size() > 0 && scripts.get(0).getEvent() instanceof GreenFlag) {

                    checkMissForever(stmts, actorDefs.get(i).getIdent().getName());
                }
            }
        }
        String note = "There is no fishy touching or keyPressed checks without a loop.";
        if (found.size() > 0) {
            note = "The project contains some fishy touching and / or keyPressed checks without a loop.";

        }
        return new IssueReport(name, counter, found, note);
    }

    private void checkMissForever(List<Stmt> stmts, String actorName) {
        for (int i = 0; i < stmts.size(); i++) {
            if (stmts.get(i) instanceof RepeatForeverStmt) {
                return;
            } else if (stmts.get(i) instanceof IfThenStmt) {
                BoolExpr bool = ((IfThenStmt) stmts.get(i)).getBoolExpr();
                if (bool instanceof IsKeyPressed || bool instanceof Touching || bool instanceof IsMouseDown) {
                    found.add(actorName);
                    counter++;
                } else {
                    checkMissForever(((IfThenStmt) stmts.get(i)).getThenStmts().getStmts().getListOfStmt(), actorName);
                }
            } else if (stmts.get(i) instanceof IfElseStmt) {
                BoolExpr bool = ((IfElseStmt) stmts.get(i)).getBoolExpr();
                if (bool instanceof IsKeyPressed || bool instanceof Touching || bool instanceof IsMouseDown) {
                    found.add(actorName);
                    counter++;
                } else {
                    checkMissForever(((IfElseStmt) stmts.get(i)).getStmtList().getStmts().getListOfStmt(), actorName);
                    checkMissForever(((IfElseStmt) stmts.get(i)).getElseStmts().getStmts().getListOfStmt(), actorName);
                }
            } else if (stmts.get(i) instanceof RepeatTimesStmt) {
                checkMissForever(((RepeatTimesStmt) stmts.get(i)).getStmtList().getStmts().getListOfStmt(),
                        actorName);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
