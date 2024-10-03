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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.KeyPressed;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.BoardLaunch;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.EmotionStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.speaker.SpeakerStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;

import static de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity.MEDIUM;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.*;

public class CodeyUploadStopTimed extends AbstractRobotFinder {

    private static final String NAME = "codey_upload_stop_timed";
    private static final String POSSIBLE = "codey_upload_stop_timed_possible";
    private final Map<Script, StopAll> stopScripts = new HashMap<>();
    private final Set<Script> timedScripts = new HashSet<>();
    private boolean upload;
    private boolean uploadPossible;

    @Override
    public void visit(ActorDefinition node) {
        putProceduresinScript = true;
        parseProcedureDefinitions = false;
        ignoreLooseBlocks = true;
        Preconditions.checkNotNull(program);
        currentActor = node;
        procMap = program.getProcedureMapping().getProcedures().get(currentActor.getIdent().getName());
        String actorName = node.getIdent().getName();
        robot = getRobot(actorName, node.getSetStmtList());
        if (!(robot == CODEY || robot == MCORE)) {
            finishRobotVisit();
            return;
        }
        visitChildren(node);

        if (!stopScripts.isEmpty()) {
            Set<StopAll> issueList = new HashSet<>();
            for (Script stop : stopScripts.keySet()) {
                for (Script timed : timedScripts) {
                    if (!stop.equals(timed)) {
                        issueList.add(stopScripts.get(stop));
                    }
                }
            }
            for (StopAll issue : issueList) {
                if (uploadPossible) {
                    addIssue(issue, MEDIUM, new Hint(POSSIBLE));
                } else {
                    addIssue(issue, MEDIUM);
                }
            }
        }
        finishRobotVisit();
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        currentScript = node;
        proceduresInScript.put(node, new LinkedList<>());
        currentProcedure = null;
        if (robot == CODEY) {
            Event event = node.getEvent();
            if (event instanceof BoardLaunch) {
                upload = true;
            } else if (event instanceof GreenFlag || event instanceof KeyPressed) {
                upload = false;
            } else {
                upload = true;
                uploadPossible = true;
            }
            visitChildren(node.getStmtList());
            upload = false;
            uploadPossible = false;
        }
    }

    @Override
    public void visit(StopAll node) {
        if (upload) {
            stopScripts.put(currentScript, node);
        }
    }

    @Override
    public void visit(MBlockStmt node) {
        if (upload && node instanceof TimedStmt && !(node instanceof SpeakerStmt)) {
            timedScripts.add(currentScript);
        }
    }

    @Override
    public void visit(EmotionStmt node) {
        if (upload) {
            timedScripts.add(currentScript);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(POSSIBLE);
        return keys;
    }
}
