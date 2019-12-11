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
import newanalytics.IssueTool;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.event.Never;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.control.*;
import utils.Preconditions;

/**
 * Checks for scripts with more than 12 blocks.
 */
public class LongScript implements IssueFinder {

    public static final String NAME = "long_script";
    public static final String SHORT_NAME = "lngscr";
    private static final String NOTE1 = "There are no long scripts.";
    private static final String NOTE2 = "Some scripts are very long.";
    private static final int NUMBER_TOO_LONG = 12;

    public LongScript() {
    }

    @Override
    public IssueReport check(Program program) {

        Preconditions.checkNotNull(program);

        List<String> found = new ArrayList<>();
        final List<ActorDefinition> definitions = program.getActorDefinitionList().getDefintions();

        for (ActorDefinition actor : definitions) {
            List<Script> scripts = actor.getScripts().getScriptList();
            for (Script current : scripts) {
                //if a event is used only 11 blocks have to be in the remaining script, only statements are considered, expressions not
                if ((!(current.getEvent() instanceof Never) && current.getStmtList().getStmts().getListOfStmt().size() >= NUMBER_TOO_LONG - 1)
                        || ((current.getEvent() instanceof Never) && current.getStmtList().getStmts().getListOfStmt().size() >= NUMBER_TOO_LONG)) {
                    found.add(actor.getIdent().getName());
                } else {
                    int count;
                    if (!(current.getEvent() instanceof Never)) {
                        count = 1;
                    } else {
                        count = 0;
                    }
                    if (lookAtStmts(current.getStmtList().getStmts().getListOfStmt(), count) >= NUMBER_TOO_LONG) {
                        found.add(actor.getIdent().getName());
                    }
                }
            }
        }
        String notes = NOTE1;
        if (found.size() > 0) {
            notes = NOTE2;
        }

        return new IssueReport(NAME, found.size(), IssueTool.getOnlyUniqueActorList(found), notes);


    }

    private int lookAtStmts(List<Stmt> listOfStmt, int count) {
        for (Stmt stmt : listOfStmt) {
            count++;
            if (count >= NUMBER_TOO_LONG) {
                return count;
            }
            if (stmt instanceof RepeatForeverStmt) {
                if (count + ((RepeatForeverStmt) stmt).getStmtList().getStmts().getListOfStmt().size() >= NUMBER_TOO_LONG) {
                    return count + ((RepeatForeverStmt) stmt).getStmtList().getStmts().getListOfStmt().size();
                }
                count = lookAtStmts(((RepeatForeverStmt) stmt).getStmtList().getStmts().getListOfStmt(), count);
            } else if (stmt instanceof RepeatTimesStmt) {
                if (count + ((RepeatTimesStmt) stmt).getStmtList().getStmts().getListOfStmt().size() >= NUMBER_TOO_LONG) {
                    return count + ((RepeatTimesStmt) stmt).getStmtList().getStmts().getListOfStmt().size();
                }
                count = lookAtStmts(((RepeatTimesStmt) stmt).getStmtList().getStmts().getListOfStmt(), count);
            } else if (stmt instanceof IfElseStmt) {
                if (count + ((IfElseStmt) stmt).getStmtList().getStmts().getListOfStmt().size() >= NUMBER_TOO_LONG) {
                    return count + ((IfElseStmt) stmt).getStmtList().getStmts().getListOfStmt().size();
                }
                if (count + ((IfElseStmt) stmt).getElseStmts().getStmts().getListOfStmt().size() >= NUMBER_TOO_LONG) {
                    return count + ((IfElseStmt) stmt).getStmtList().getStmts().getListOfStmt().size();
                }
                count = lookAtStmts(((IfElseStmt) stmt).getStmtList().getStmts().getListOfStmt(), count);
                count = lookAtStmts(((IfElseStmt) stmt).getElseStmts().getStmts().getListOfStmt(), count);
            } else if (stmt instanceof UntilStmt) {
                if (count + ((UntilStmt) stmt).getStmtList().getStmts().getListOfStmt().size() >= NUMBER_TOO_LONG) {
                    return count + ((UntilStmt) stmt).getStmtList().getStmts().getListOfStmt().size();
                }
                count = lookAtStmts(((UntilStmt) stmt).getStmtList().getStmts().getListOfStmt(), count);
            } else if (stmt instanceof IfThenStmt) {
                if (count + ((IfThenStmt) stmt).getThenStmts().getStmts().getListOfStmt().size() >= NUMBER_TOO_LONG) {
                    return count + ((IfThenStmt) stmt).getThenStmts().getStmts().getListOfStmt().size();
                }
                count = lookAtStmts(((IfThenStmt) stmt).getThenStmts().getStmts().getListOfStmt(), count);
            }
            if (count >= NUMBER_TOO_LONG) {
                return count;
            }
        }
        return count;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
