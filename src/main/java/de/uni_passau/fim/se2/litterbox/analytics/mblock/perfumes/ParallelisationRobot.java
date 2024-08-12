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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.RobotCode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This checks for starting of two scripts or more on the same event.
 */
public class ParallelisationRobot extends AbstractRobotFinder {
    public static final String NAME = "parallelisation_robot";
    private List<Event> events = new ArrayList<>();

    @Override
    public Set<Issue> check(Program program) {
        events = new ArrayList<>();
        return super.check(program);
    }

    @Override
    public void visit(BoardButtonAction node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(BoardShaken node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(BoardTilted node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(BrightnessLess node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(LaunchButton node) {
        checkEvents(node);
        events.add(node);
    }

    @Override
    public void visit(BoardLaunch node) {
        if (robot != RobotCode.MCORE) {
            checkEvents(node);
            events.add(node);
        }
    }

    private void checkEvents(AbstractNode event) {
        for (Event e : events) {
            if (e.equals(event)) {
                addIssue(event, event.getMetadata(), IssueSeverity.HIGH);
                break;
            }
        }
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        return first.getFinder() == other.getFinder();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
