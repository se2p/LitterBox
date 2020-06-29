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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The empty script smell occurs if an event handler has no other blocks attached to it.
 * The dead code smell occurs when a script has no event handler to start it, so it can never be
 * executed automatically.
 * If both smells occur simultaneously without any other scripts in a sprite we consider it a bug.
 * We suppose that the complete script should consist of the event handler attached to the dead code.
 */
public class NoWorkingScripts implements IssueFinder, ScratchVisitor {
    public static final String NAME = "no_working_scripts";
    public static final String SHORT_NAME = "noWorkScript";
    public static final String HINT_TEXT = "no working scripts";
    private ActorDefinition currentActor;
    private boolean stillFullfilledEmptyScript = false;
    private boolean deadCodeFound = false;
    private boolean foundEvent = false;
    private Set<Issue> issues = new LinkedHashSet<>();

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        program.accept(this);
        return issues;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        stillFullfilledEmptyScript = true;
        deadCodeFound = false;
        foundEvent = false;
        for (ASTNode child : actor.getChildren()) {
            child.accept(this);
        }

        if (deadCodeFound && stillFullfilledEmptyScript && foundEvent) {
            issues.add(new Issue(this, currentActor, actor,
                    HINT_TEXT, null)); // TODO: Null to ensure loose comment
        }
    }

    @Override
    public void visit(Script node) {
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
    }
}
