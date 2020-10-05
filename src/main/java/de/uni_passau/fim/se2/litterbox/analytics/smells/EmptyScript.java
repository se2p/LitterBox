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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.*;

/**
 * Checks if all Sprites have a starting point.
 */
public class EmptyScript extends AbstractIssueFinder {

    public static final String NAME = "empty_script";
    private boolean isEmpty;

    @Override
    public void visit(Script node) {
        currentScript = node;
        isEmpty = false;
        if (!(node.getEvent() instanceof Never) && node.getStmtList().getStmts().size() == 0) {
            isEmpty = true;
        }
        visitChildren(node);
    }

    @Override
    public void visit(AttributeAboveValue node) {
        if (isEmpty) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(BackdropSwitchTo node) {
        if (isEmpty) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(GreenFlag node) {
        if (isEmpty) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(KeyPressed node) {
        if (isEmpty) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (isEmpty) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(SpriteClicked node) {
        if (isEmpty) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(StageClicked node) {
        if (isEmpty) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(StartedAsClone node) {
        if (isEmpty) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }
}

