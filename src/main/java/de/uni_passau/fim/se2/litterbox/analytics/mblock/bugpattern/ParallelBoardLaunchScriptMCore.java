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
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.BoardLaunch;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.MCORE;
import static de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode.NO_ROBOT;

public class ParallelBoardLaunchScriptMCore extends AbstractRobotFinder {

    private static final String NAME = "parallel_board_launch_script";
    private final List<ActorDefinition> actors = new ArrayList<>();
    private final List<ScriptEntity> scripts = new ArrayList<>();
    private final List<ASTNode> nodes = new ArrayList<>();
    private final List<Metadata> multipleMetadata = new ArrayList<>();
    private int launchCount = 0;

    @Override
    public void visit(Script script) {
        if (script.getStmtList().hasStatements()) {
            ignoreLooseBlocks = true;
            super.visit(script);
        }
    }

    @Override
    public void visit(BoardLaunch node) {
        if (robot == MCORE) {
            launchCount++;
            actors.add(currentActor);
            scripts.add(currentScript);
            nodes.add(node);
            multipleMetadata.add(node.getMetadata());
        }
    }

    @Override
    protected void finishRobotVisit() {
        if (robot == MCORE) {
            if (launchCount >= 2) {
                MultiBlockIssue issue = new MultiBlockIssue(this, IssueSeverity.HIGH, program, currentActor, scripts, nodes, nodes.get(0).getMetadata(), new Hint(getName()));
                addIssue(issue);
            }
            actors.clear();
            scripts.clear();
            nodes.clear();
            multipleMetadata.clear();
        }
        launchCount = 0;
        robot = NO_ROBOT;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
