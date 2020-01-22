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
package de.uni_passau.fim.se2.litterbox.analytics.ctscore;

import java.util.ArrayList;
import java.util.List;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

/**
 * Evaluates the level of flow control of the Scratch program.
 */
public class FlowControl implements IssueFinder {

    private final int SCRIPT = 1;
    private final int REPEAT_FOREVER = 2;
    private final int UNTIL = 3;
    private String[] notes = new String[4];
    public final static String NAME = "flow_control";
    public final static String SHORT_NAME = "flow";
    private List<String> found;

    public FlowControl() {
        found = new ArrayList<>();
        notes[0] = "There is a sequence of blocks missing.";
        notes[1] = "Basic level. There is repeat or forever missing.";
        notes[2] = "Developing level. There is repeat until missing.";
        notes[3] = "Proficiency level. Good work!";
    }

    /**
     * {@inheritDoc}
     *
     * @param program
     */
    @Override
    public IssueReport check(Program program) {
        int level = 0;
        found = new ArrayList<>();
        List<ActorDefinition> actorDefs = program.getActorDefinitionList().getDefintions();
        level = checkIfScriptsUsed(actorDefs, level);
        level = checkIfForeverOrRepeat(actorDefs, level);
        level = checkIfUntil(actorDefs, level);

        return new IssueReport(NAME, level, found, notes[level]);
    }

    private int checkIfUntil(List<ActorDefinition> actorDefs, int level) {
        for (ActorDefinition actorDef : actorDefs) {
            List<Script> scripts = actorDef.getScripts().getScriptList();
            for (Script script : scripts) {
                List<Stmt> stmts = script.getStmtList().getStmts().getListOfStmt();
                int newLevel = checkStmts(stmts, level, UNTIL);
                if (newLevel == UNTIL) {
                    found.add(actorDef.getIdent().getName());
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
            } else if (stmt instanceof RepeatTimesStmt && rewardLevel == UNTIL) {
                int internalLevel = checkStmts(((RepeatTimesStmt) stmt).getStmtList().getStmts().getListOfStmt(),
                        level, rewardLevel);
                if (internalLevel == rewardLevel) {
                    return rewardLevel;
                }
            } else if (stmt instanceof RepeatForeverStmt && rewardLevel == UNTIL) {
                int internalLevel = checkStmts(((RepeatForeverStmt) stmt).getStmtList().getStmts().getListOfStmt(),
                        level, rewardLevel);
                if (internalLevel == rewardLevel) {
                    return rewardLevel;
                }
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
                    found.add(actorDef.getIdent().getName());
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
                    found.add(actorDef.getIdent().getName());
                    return SCRIPT;
                }
            }

        }
        return level;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
