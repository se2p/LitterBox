/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.RunningState;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RockyLight;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RockyLightOff;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RockyLightStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;

import java.util.LinkedList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RunningState.*;

public class RockyLightOffScriptMissing extends AbstractRobotFinder {

    private static final String NAME = "rocky_light_off_script_missing";
    private final List<RockyLightStmt> lastStmtList = new LinkedList<>();
    private RunningState state = NEVER;
    private RockyLightStmt lastStmt = null;
    private boolean forever = false;

    @Override
    public void visit(Program program) {
        putProceduresinScript = true;
        parseProcedureDefinitions = false;
        ignoreLooseBlocks = true;
        super.visit(program);
    }

    @Override
    public void visit(ScriptList node) {
        for (Script script : node.getScriptList()) {
            script.accept(this);
            if (state == RUNNING) {
                lastStmtList.add(lastStmt);
            } else if (state == STOPPED) {
                state = NEVER;
                lastStmt = null;
                forever = false;
                lastStmtList.clear();
                return;
            }
            state = NEVER;
            lastStmt = null;
            forever = false;
        }
        for (RockyLightStmt stmt : lastStmtList) {
            addIssue(stmt);
        }
        lastStmtList.clear();
    }

    @Override
    public void visit(Script node) {
        currentScript = node;
        proceduresInScript.put(node, new LinkedList<>());
        currentProcedure = null;
        if (!(node.getEvent() instanceof Never)) {
            node.getStmtList().accept(this);
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        forever = true;
        visit((LoopStmt) node);
    }

    @Override
    public void visit(RockyLight node) {
        state = RUNNING;
        lastStmt = node;
    }

    @Override
    public void visit(RockyLightOff node) {
        state = STOPPED;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
