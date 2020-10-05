/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;

/**
 * The empty script smell occurs if an event handler has no other blocks attached to it.
 * The dead code smell occurs when a script has no event handler to start it, so it can never be
 * executed automatically.
 * If both smells occur simultaneously without any other scripts in a sprite we consider it a bug.
 * We suppose that the complete script should consist of the event handler attached to the dead code.
 */
public class NoWorkingScripts extends AbstractIssueFinder {
    public static final String NAME = "no_working_scripts";
    private boolean stillFullfilledEmptyScript = false;
    private boolean deadCodeFound = false;
    private boolean foundEvent = false;

    @Override
    public void visit(ActorDefinition actor) {
        stillFullfilledEmptyScript = true;
        deadCodeFound = false;
        foundEvent = false;
        super.visit(actor);

        if (deadCodeFound && stillFullfilledEmptyScript && foundEvent) {
            addIssueWithLooseComment();
        }
    }

    @Override
    public void visit(Script node) {
        currentScript = node;
        if (stillFullfilledEmptyScript) {
            if (node.getEvent() instanceof Never) {
                if (node.getStmtList().getStmts().size() > 0) {
                    deadCodeFound = true;
                }
            } else {
                foundEvent = true;
                if (node.getStmtList().getStmts().size() > 0) {
                    stillFullfilledEmptyScript = false;
                }
            }
        }
        currentScript = null;
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
