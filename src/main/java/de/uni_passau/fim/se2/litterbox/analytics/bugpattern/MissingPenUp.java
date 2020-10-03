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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenUpStmt;

/**
 * A sprite that uses pen down blocks but never a pen up may draw right away, when the project is
 * restarted. This might not be intended.
 */
public class MissingPenUp extends AbstractIssueFinder {

    public static final String NAME = "missing_pen_up";

    private boolean penUpSet = false;
    private boolean penDownSet = false;
    private boolean addComment = false;

    @Override
    public void visit(ActorDefinition actor) {
        penUpSet = false;
        penDownSet = false;
        addComment = false;
        currentActor = actor;
        super.visit(actor);

        if (getResult()) {
            addComment = true;
            visitChildren(actor);
            reset();
        }
    }

    @Override
    public void visit(PenDownStmt node) {
        if (!addComment) {
            penDownSet = true;
            visitChildren(node);
        } else if (getResult()) {
            // TODO: Is this potentially added multiple times?
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(PenUpStmt node) {
        if (!addComment) {
            penUpSet = true;
            visitChildren(node);
        }
    }

    void reset() {
        penUpSet = false;
        penDownSet = false;
        addComment = false;
    }

    boolean getResult() {
        return penDownSet && !penUpSet;
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
