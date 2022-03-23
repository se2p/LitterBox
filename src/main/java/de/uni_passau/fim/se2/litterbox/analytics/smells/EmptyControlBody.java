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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

/**
 * Checks for empty if or else bodies.
 */
public class EmptyControlBody extends AbstractIssueFinder {
    public static final String NAME = "empty_control_body";
    private static final IssueSeverity severity = IssueSeverity.LOW;

    @Override
    public void visit(IfElseStmt node) {
        if (node.getThenStmts().getStmts().isEmpty()) {
            Hint hint = new Hint(NAME);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("if") + " < > " + IssueTranslator.getInstance().getInfo("then ") + IssueTranslator.getInstance().getInfo("else"));
            addIssue(node, node.getMetadata(), severity, hint);
        }
        if (node.getElseStmts().getStmts().isEmpty()) {
            Hint hint = new Hint(NAME);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("if") + " < > " + IssueTranslator.getInstance().getInfo("then ") + IssueTranslator.getInstance().getInfo("else"));
            addIssue(node, node.getMetadata(), severity, hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        if (node.getThenStmts().getStmts().isEmpty()) {
            Hint hint = new Hint(NAME);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("if") + " < > " + IssueTranslator.getInstance().getInfo("then "));
            addIssue(node, node.getMetadata(), severity, hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        if (node.getStmtList().getStmts().isEmpty()) {
            Hint hint = new Hint(NAME);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("until") + " < > ");
            addIssue(node, node.getMetadata(), severity, hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (node.getStmtList().getStmts().isEmpty()) {
            Hint hint = new Hint(NAME);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("forever"));
            addIssue(node, node.getMetadata(), severity, hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (node.getStmtList().getStmts().isEmpty()) {
            Hint hint = new Hint(NAME);
            hint.setParameter(Hint.BLOCK_NAME, IssueTranslator.getInstance().getInfo("repeat") + " ( )");
            addIssue(node, node.getMetadata(), severity, hint);
        }
        visitChildren(node);
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
