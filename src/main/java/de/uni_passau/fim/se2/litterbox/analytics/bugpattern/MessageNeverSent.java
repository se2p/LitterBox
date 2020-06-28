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

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addBlockComment;


import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import java.util.*;

/**
 * This pattern is a specialised version of unmatched broadcast and receive blocks. When
 * there are blocks to receive messages but the corresponding broadcast message block is missing, that
 * script will never be executed.
 */
public class MessageNeverSent implements IssueFinder, ScratchVisitor {

    public static final String NAME = "message_Never_Sent";
    public static final String SHORT_NAME = "messNeverSent";
    public static final String HINT_TEXT = "message Never Sent";
    private List<Pair<String>> messageSent = new ArrayList<>();
    private List<Pair<String>> messageReceived = new ArrayList<>();
    private ActorDefinition currentActor;
    private int identifierCounter;
    private boolean addComment;
    private Set<Issue> issues = new LinkedHashSet<>();
    private Set<String> notSentMessages;


    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        messageSent = new ArrayList<>();
        messageReceived = new ArrayList<>();
        program.accept(this);
        identifierCounter = 1;
        addComment = false;
        notSentMessages = new LinkedHashSet<>();

        final LinkedHashSet<Pair<String>> nonSyncedPairs = new LinkedHashSet<>();
        for (Pair<String> received : messageReceived) {
            boolean isReceived = false;
            for (Pair<String> sent : messageSent) {
                if (received.getSnd().equalsIgnoreCase(sent.getSnd())) {
                    isReceived = true;
                    break;
                }
            }
            if (!isReceived) {
                nonSyncedPairs.add(received);
                notSentMessages.add(received.getSnd());
            }
        }

        addComment = true;
        program.accept(this);

        final Set<String> actorNames = new LinkedHashSet<>();
        nonSyncedPairs.forEach(p -> actorNames.add(p.getFst()));

        return issues;
        // return new IssueReport(NAME, nonSyncedPairs.size(), new LinkedList<>(actorNames), "");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(Broadcast node) {
        if (node.getMessage().getMessage() instanceof StringLiteral) {
            if (!addComment) {
                final String actorName = currentActor.getIdent().getName();
                final String msgName = ((StringLiteral) node.getMessage().getMessage()).getText();
                messageSent.add(new Pair<>(actorName, msgName));
            }
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (node.getMessage().getMessage() instanceof StringLiteral) {
            if (!addComment) {
                final String actorName = currentActor.getIdent().getName();
                final String msgName = ((StringLiteral) node.getMessage().getMessage()).getText();
                messageSent.add(new Pair<>(actorName, msgName));
            }
        }
    }

    @Override
    public void visit(Script node) {
        if (node.getStmtList().getStmts().size() > 0 && node.getEvent() instanceof ReceptionOfMessage) {
            ReceptionOfMessage event = (ReceptionOfMessage) node.getEvent();
            if (event.getMsg().getMessage() instanceof StringLiteral) {
                final String msgName = ((StringLiteral) event.getMsg().getMessage()).getText();
                if (!addComment) {
                    final String actorName = currentActor.getIdent().getName();
                    messageReceived.add(new Pair<>(actorName, msgName));
                } else if (notSentMessages.contains(msgName)) {
                    addBlockComment((NonDataBlockMetadata) event.getMetadata(), currentActor, HINT_TEXT,
                            SHORT_NAME + identifierCounter);
                    identifierCounter++;
                    issues.add(new Issue(this, currentActor, node));
                }
            }
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
