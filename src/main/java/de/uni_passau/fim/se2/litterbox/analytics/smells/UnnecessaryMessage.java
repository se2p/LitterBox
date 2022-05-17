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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * If a message is sent in the second block of a script the receiving script could also start with the event of the first script.
 */
public class UnnecessaryMessage extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_message";
    private Set<Message> messagesInFirstPlace;
    private Set<Message> messagesInOtherPlace;
    private boolean searching;

    @Override
    public void visit(Program node) {
        messagesInOtherPlace = new LinkedHashSet<>();
        messagesInFirstPlace = new LinkedHashSet<>();
        searching = true;
        super.visit(node);
        searching = false;
        super.visit(node);
    }

    @Override
    public void visit(Script node) {
        List<Stmt> stmts = node.getStmtList().getStmts();
        if (!stmts.isEmpty() && stmts.get(0) instanceof Broadcast) {
            Broadcast brd = (Broadcast) stmts.get(0);
            if (searching) {
                messagesInFirstPlace.add(brd.getMessage());
            } else {
                //messages that are sent in other parts of a scripts should not be counted
                if (!messagesInOtherPlace.contains(brd.getMessage())) {
                    currentScript = node;
                    currentProcedure = null;
                    addIssue(brd, brd.getMetadata());
                }
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(Broadcast node) {
        detectIfMessageInsideScript(node, node.getMessage());
    }

    @Override
    public void visit(BroadcastAndWait node) {
        detectIfMessageInsideScript(node, node.getMessage());
    }

    private void detectIfMessageInsideScript(ASTNode node, Message message) {
        if (searching) {
            ASTNode parentStmtList = node.getParentNode();
            StmtList stmtListNode = (StmtList) parentStmtList;
            ASTNode grandParentNode = stmtListNode.getParentNode();
            List<Stmt> stmts = stmtListNode.getStmts();
            int index = -1;
            for (int i = 0; i < stmts.size(); i++) {
                if (stmts.get(i) == node) {
                    index = i;
                    break;
                }
            }
            if (index > 0 || !(grandParentNode instanceof Script)) {
                messagesInOtherPlace.add(message);
            }
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
