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
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This pattern is a specialised version of unmatched broadcast and receive blocks. It occurs
 * when there are blocks to send messages, but the When I receive message event handler is missing. Since no
 * handler reacts to this event, the message stays unnoticed.
 */
public class MessageNeverReceived extends AbstractIssueFinder {

    public static final String NAME = "message_never_received";
    private List<Pair<String>> messageSent = new ArrayList<>();
    private List<Pair<String>> messageReceived = new ArrayList<>();
    private boolean addComment = false;
    private Set<String> notReceivedMessages = new LinkedHashSet<>();

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        messageSent = new ArrayList<>();
        messageReceived = new ArrayList<>();
        program.accept(this);
        addComment = false;
        notReceivedMessages = new LinkedHashSet<>();

        final LinkedHashSet<Pair<String>> nonSyncedPairs = new LinkedHashSet<>();
        for (Pair<String> sent : messageSent) {
            boolean isReceived = false;
            for (Pair<String> received : messageReceived) {
                if (sent.getSnd().equalsIgnoreCase(received.getSnd())) {
                    isReceived = true;
                    break;
                }
            }
            if (!isReceived) {
                nonSyncedPairs.add(sent);
                notReceivedMessages.add(sent.getSnd());
            }
        }

        addComment = true;
        program.accept(this);

        final Set<String> actorNames = new LinkedHashSet<>();
        nonSyncedPairs.forEach(p -> actorNames.add(p.getFst()));
        return issues;
    }

    @Override
    public void visit(Broadcast node) {
        if (node.getMessage().getMessage() instanceof StringLiteral) {
            final String msgName = ((StringLiteral) node.getMessage().getMessage()).getText();
            if (!addComment) {
                final String actorName = currentActor.getIdent().getName();
                messageSent.add(new Pair<>(actorName, msgName));
            } else if (notReceivedMessages.contains(msgName)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_MESSAGE, ((StringLiteral) node.getMessage().getMessage()).getText());
                addIssue(node, node.getMetadata(), hint);
            }
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (node.getMessage().getMessage() instanceof StringLiteral) {
            final String msgName = ((StringLiteral) node.getMessage().getMessage()).getText();
            if (!addComment) {
                final String actorName = currentActor.getIdent().getName();
                messageSent.add(new Pair<>(actorName, msgName));
            } else if (notReceivedMessages.contains(msgName)) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_MESSAGE, ((StringLiteral) node.getMessage().getMessage()).getText());
                addIssue(node, node.getMetadata(), hint);
            }
        }
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (node.getMsg().getMessage() instanceof StringLiteral) {
            if (!addComment) {
                final String actorName = currentActor.getIdent().getName();
                final String msgName = ((StringLiteral) node.getMsg().getMessage()).getText();
                messageReceived.add(new Pair<>(actorName, msgName));
            }
        }
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }

        return first.getCodeLocation().equals(other.getCodeLocation());
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
