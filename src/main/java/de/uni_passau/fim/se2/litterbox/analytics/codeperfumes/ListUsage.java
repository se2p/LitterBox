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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;

/**
 * List as the only collection in Scratch is important to understand and to manipulate it is a sign for good
 * computational understanding.
 */
public class ListUsage extends AbstractIssueFinder {

    public static final String NAME = "list_usage";

    @Override
    public void visit(AddTo node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    @Override
    public void visit(DeleteAllOf node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    @Override
    public void visit(DeleteOf node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    @Override
    public void visit(InsertAt node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    @Override
    public void visit(ReplaceItem node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    @Override
    public void visit(HideList node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    @Override
    public void visit(ShowList node) {
        addIssue(node, node.getMetadata(), IssueSeverity.HIGH);
        visitChildren(node);
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
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
