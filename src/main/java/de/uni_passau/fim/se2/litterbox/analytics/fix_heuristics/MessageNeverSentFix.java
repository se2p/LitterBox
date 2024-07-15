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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class MessageNeverSentFix extends AbstractIssueFinder {
    public static final String NAME = "message_never_sent_fix";
    private final String bugLocationBlockId;
    private boolean firstRun;
    private String message = null;
    private boolean alreadyFound = false;

    public MessageNeverSentFix(String bugLocationBlockId) {
        this.bugLocationBlockId = bugLocationBlockId;
    }

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        firstRun = true;
        program.accept(this);
        firstRun = false;
        if (message != null) {
            program.accept(this);
        }
        return Collections.unmodifiableSet(issues);
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (firstRun && AstNodeUtil.hasBlockId(node, bugLocationBlockId)) {
            if (scriptNotEmpty(node.getParentNode())) {
                if (node.getMsg().getMessage() instanceof StringLiteral text) {
                    message = text.getText();
                }
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(Broadcast node) {
        if (!firstRun) {
            if (node.getMessage().getMessage() instanceof StringLiteral text) {
                if (!alreadyFound && text.getText().equals(message) && scriptHasHead(node)) {
                    alreadyFound = true;
                    addIssue(node, node.getMetadata());
                }
            }
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (!firstRun) {
            if (node.getMessage().getMessage() instanceof StringLiteral text) {
                if (!alreadyFound && text.getText().equals(message) && scriptHasHead(node)) {
                    alreadyFound = true;
                    addIssue(node, node.getMetadata());
                }
            }
        } else {
            visitChildren(node);
        }
    }

    private boolean scriptHasHead(ASTNode node) {
        ScriptEntity script = AstNodeUtil.findParent(node, ScriptEntity.class);
        if (script instanceof Script scr) {
            return !(scr.getEvent() instanceof Never);
        } else {
            return true;
        }
    }

    private boolean scriptNotEmpty(ASTNode parentNode) {
        assert (parentNode instanceof Script);
        return ((Script) parentNode).getStmtList().hasStatements();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.FIX;
    }
}
