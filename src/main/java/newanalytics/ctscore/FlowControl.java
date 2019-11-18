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
package newanalytics.ctscore;

import java.util.ArrayList;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.newast.model.ActorDefinition;
import scratch.newast.model.Program;
import scratch.newast.model.Script;
import scratch.newast.model.event.Never;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.control.IfElseStmt;
import scratch.newast.model.statement.control.IfThenStmt;
import scratch.newast.model.statement.control.RepeatForeverStmt;
import scratch.newast.model.statement.control.RepeatTimesStmt;
import scratch.newast.model.statement.control.UntilStmt;

/**
 * Evaluates the level of flow control of the Scratch program.
 */
public class FlowControl implements IssueFinder {

    private final int SCRIPT = 1;
    private final int REPEAT_FOREVER = 2;
    private final int UNTIL = 3;
    private String[] notes = new String[4];
    private String name = "flow_control";
    private List<String> found;

    public FlowControl() {
        found = new ArrayList<>();
        notes[0] = "There is a sequence of blocks missing.";
        notes[1] = "Basic Level. There is repeat or forever missing.";
        notes[2] = "Developing Level. There is repeat until missing.";
        notes[3] = "Proficiency Level. Good work!";
    }

    /**
     * {@inheritDoc}
     *
     * @param program
     */
    @Override
    public IssueReport check(Program program) {
        int level = 0;
        List<ActorDefinition> actorDefs = program.getActorDefinitionList().getDefintions();
        level = checkIfScriptsUsed(actorDefs, level);
        level = checkIfForeverOrRepeat(actorDefs, level);
        level = checkIfUntil(actorDefs, level);

        return new IssueReport(name, level, found, notes[level]);
    }

    private int checkIfUntil(List<ActorDefinition> actorDefs, int level) {
        for (ActorDefinition actorDef : actorDefs) {
            List<Script> scripts = actorDef.getScripts().getScriptList();
            for (Script script : scripts) {
                List<Stmt> stmts = script.getStmtList().getStmts().getListOfStmt();
                int newLevel = checkStmts(stmts, level, UNTIL);
                if (newLevel == UNTIL) {
                    found.add(actorDef.getIdent().getValue());
                    return UNTIL;
                }
            }

        }
        return level;
    }

    private int checkStmts(List<Stmt> stmts, int level, int rewardLevel) {
        for (Stmt stmt : stmts) {
            if ((stmt instanceof RepeatTimesStmt || stmt instanceof RepeatForeverStmt) && rewardLevel == REPEAT_FOREVER) {
                return rewardLevel;
            } else if (stmt instanceof UntilStmt && rewardLevel == UNTIL) {
                return rewardLevel;
            } else if (stmt instanceof IfElseStmt) {
                int internLevel =
                    checkStmts(((IfElseStmt) stmt).getStmtList().getStmts().getListOfStmt(),
                        level, rewardLevel);
                int internLevel2 =
                    checkStmts(((IfElseStmt) stmt).getElseStmts().getStmts().getListOfStmt(),
                        level, rewardLevel);
                if (internLevel == rewardLevel || internLevel2 == rewardLevel) {
                    return rewardLevel;
                }
            } else if (stmt instanceof IfThenStmt) {
                int internLevel =
                    checkStmts((((IfThenStmt) stmt).getThenStmts().getStmts().getListOfStmt()),
                        level, rewardLevel);
                if (internLevel == rewardLevel) {
                    return rewardLevel;
                }
            }
        }
        return level;
    }

    private int checkIfForeverOrRepeat(List<ActorDefinition> actorDefs, int level) {
        for (ActorDefinition actorDef : actorDefs) {
            List<Script> scripts = actorDef.getScripts().getScriptList();
            for (Script script : scripts) {
                List<Stmt> stmts = script.getStmtList().getStmts().getListOfStmt();
                int newLevel = checkStmts(stmts, level, REPEAT_FOREVER);
                if (newLevel == REPEAT_FOREVER) {
                    found.add(actorDef.getIdent().getValue());
                    return REPEAT_FOREVER;
                }
            }

        }
        return level;
    }

    private int checkIfScriptsUsed(List<ActorDefinition> actorDefs, int level) {
        for (ActorDefinition actorDef : actorDefs) {
            List<Script> scripts = actorDef.getScripts().getScriptList();
            for (Script script : scripts) {
                List<Stmt> stmts = script.getStmtList().getStmts().getListOfStmt();
                if (stmts.size() >= 2 || (!(script.getEvent() instanceof Never) && stmts.size() >= 1)) {
                    found.add(actorDef.getIdent().getValue());
                    return SCRIPT;
                }
            }

        }
        return level;
    }

    @Override
    public String getName() {
        return name;
    }
}
