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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenClearStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * If a sprite uses a pen down block but never an erase all block, then all drawings from a
 * previous execution might remain, making it impossible to get a blank background without reloading the scratch
 * project.
 */
public class MissingEraseAll extends AbstractIssueFinder {

    public static final String NAME = "missing_erase_all";

    private boolean penClearSet = false;
    private boolean penDownSet = false;
    private boolean addComment = false;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        addComment = false;
        penClearSet = false;
        penDownSet = false;
        program.accept(this);
        if (getResult()) {
            addComment = true;
            visitChildren(program);
            reset();
        }
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        visitChildren(actor);
    }

    @Override
    public void visit(PenDownStmt node) {
        if (!addComment) {
            penDownSet = true;
            visitChildren(node);
        } else if (getResult()) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(PenClearStmt node) {
        if (!addComment) {
            penClearSet = true;
            visitChildren(node);
        }
    }

    void reset() {
        penClearSet = false;
        penDownSet = false;
        addComment = false;
    }

    public boolean getResult() {
        return penDownSet && !penClearSet;
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
