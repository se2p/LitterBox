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
import scratch.ast.model.event.GreenFlag;
import scratch.ast.model.expression.bool.BoolExpr;
import scratch.ast.model.expression.bool.IsKeyPressed;
import scratch.ast.model.expression.bool.IsMouseDown;
import scratch.ast.model.expression.bool.Touching;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.control.*;

/**
 * Checks for missing loops in event based actions.
 */
public class MissingLoopSensing implements IssueFinder {
    public static final String NAME = "missing_loop";
    public static final String SHORT_NAME = "mssfrev";
    private List<String> found;
    private int counter;

    public MissingLoopSensing() {
        found = new ArrayList<>();
        counter = 0;
    }

    @Override
    public IssueReport check(Program program) {
        List<ActorDefinition> actorDefs = program.getActorDefinitionList().getDefintions();
        for (ActorDefinition actorDef : actorDefs) {
            List<Script> scripts = actorDef.getScripts().getScriptList();
            for (int j = 0; j < scripts.size(); j++) {
                List<Stmt> stmts = scripts.get(0).getStmtList().getStmts().getListOfStmt();
                if (stmts.size() > 0 && scripts.get(0).getEvent() instanceof GreenFlag) {

                    checkMissLoop(stmts, actorDef.getIdent().getName());
                }
            }
        }
        String note = "There is no fishy touching or keyPressed checks without a loop.";
        if (found.size() > 0) {
            note = "The project contains some fishy touching and / or keyPressed checks without a loop.";

        }
        return new IssueReport(NAME, counter, found, note);
    }

    private void checkMissLoop(List<Stmt> stmts, String actorName) {
        for (Stmt stmt : stmts) {
            if (stmt instanceof RepeatForeverStmt || stmt instanceof UntilStmt) {
                return;
            } else if (stmt instanceof IfThenStmt) {
                BoolExpr bool = ((IfThenStmt) stmt).getBoolExpr();
                if (bool instanceof IsKeyPressed || bool instanceof Touching || bool instanceof IsMouseDown) {
                    found.add(actorName);
                    counter++;
                } else {
                    checkMissLoop(((IfThenStmt) stmt).getThenStmts().getStmts().getListOfStmt(), actorName);
                }
            } else if (stmt instanceof IfElseStmt) {
                BoolExpr bool = ((IfElseStmt) stmt).getBoolExpr();
                if (bool instanceof IsKeyPressed || bool instanceof Touching || bool instanceof IsMouseDown) {
                    found.add(actorName);
                    counter++;
                } else {
                    checkMissLoop(((IfElseStmt) stmt).getStmtList().getStmts().getListOfStmt(), actorName);
                    checkMissLoop(((IfElseStmt) stmt).getElseStmts().getStmts().getListOfStmt(), actorName);
                }
            } else if (stmt instanceof RepeatTimesStmt) {
                checkMissLoop(((RepeatTimesStmt) stmt).getStmtList().getStmts().getListOfStmt(),
                        actorName);
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
