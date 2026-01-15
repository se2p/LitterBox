/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageNeverSent;
import de.uni_passau.fim.se2.litterbox.ast.model.Message;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.util.TypoGenerator;

import java.util.*;

/**
 * Injects the "Message Never Sent" bug by introducing a typo into ALL
 * broadcast blocks for a given message, causing the message to never be sent.
 *
 * <p>The MessageNeverSent bug finder checks if a received message is sent
 * by ANY broadcast block. Therefore, we must change ALL broadcast blocks for
 * a given message in a single mutation to properly inject the bug.</p>
 */
public class MessageNeverSentInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new MessageNeverSent();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Program node) {
        // Collect all received messages
        Set<String> receivedMessages = collectReceivedMessages(node);

        // Collect all broadcasts grouped by message name
        Map<String, List<Stmt>> broadcastsByMessage = collectBroadcasts(node);

        // For each unique message that IS received, create ONE mutation that changes ALL broadcast blocks
        // No point in changing broadcasts for messages that are already not received
        for (Map.Entry<String, List<Stmt>> entry : broadcastsByMessage.entrySet()) {
            if (receivedMessages.contains(entry.getKey())) {
                // Only inject if at least one broadcast is reachable
                boolean hasReachableBroadcast = entry.getValue().stream().anyMatch(this::isReachable);
                if (hasReachableBroadcast) {
                    modifyClonedNode(entry.getValue().getFirst(), this::applyTypoToAllBroadcasts);
                }
            }
        }
    }

    private boolean isReachable(Stmt stmt) {
        // Traverse parents to find Script
        de.uni_passau.fim.se2.litterbox.ast.model.ASTNode current = stmt;
        while (current != null && !(current instanceof Script)) {
            current = current.getParentNode();
        }

        if (current instanceof Script script) {
            // Check if event is Never (detached)
            return !(script.getEvent() instanceof de.uni_passau.fim.se2.litterbox.ast.model.event.Never);
        }
        return false;
    }

    /**
     * Collects all message names that have ReceptionOfMessage handlers (case-insensitive).
     */
    private Set<String> collectReceivedMessages(Program program) {
        Set<String> messages = new HashSet<>();
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(Script node) {
                if (node.getEvent() instanceof ReceptionOfMessage event
                        && node.getStmtList().hasStatements()
                        && event.getMsg().getMessage() instanceof StringLiteral lit) {
                    messages.add(lit.getText().toLowerCase());
                }
                visitChildren(node);
            }
        });
        return messages;
    }

    /**
     * Collects all Broadcast/BroadcastAndWait statements grouped by message name.
     */
    private Map<String, List<Stmt>> collectBroadcasts(Program program) {
        BroadcastCollector collector = new BroadcastCollector();
        program.accept(collector);
        return collector.getBroadcastsByMessage();
    }

    /**
     * Applies a typo to all broadcast blocks in the cloned program that send the same message.
     */
    private void applyTypoToAllBroadcasts(Stmt clonedFirst) {
        String originalMsg = getMessageText(clonedFirst);
        if (originalMsg == null) {
            return;
        }

        Program clonedProgram = findProgramRoot(clonedFirst);
        if (clonedProgram == null) {
            return;
        }

        // The finder uses case-insensitive comparison, so we must ensure the typo
        // differs from the original ignoring case
        String typoMsg = TypoGenerator.generateTypo(originalMsg);
        int attempts = 0;
        while (typoMsg.equalsIgnoreCase(originalMsg) && attempts < 10) {
            typoMsg = TypoGenerator.generateTypo(originalMsg);
            attempts++;
        }

        List<Stmt> matchingBroadcasts = collectBroadcastsWithMessage(clonedProgram, originalMsg);

        for (Stmt broadcast : matchingBroadcasts) {
            replaceBroadcastMessage(broadcast, typoMsg);
        }
    }

    /**
     * Collects all broadcasts in the program that send the specified message.
     */
    private List<Stmt> collectBroadcastsWithMessage(Program program, String messageName) {
        BroadcastCollector collector = new BroadcastCollector(messageName);
        program.accept(collector);
        return collector.getMatchingBroadcasts();
    }

    /**
     * Replaces a broadcast statement with one that sends a typo message.
     */
    private void replaceBroadcastMessage(Stmt stmt, String typoMsg) {
        Message newMessage = createMessage(typoMsg);
        Stmt newStmt = createBroadcast(stmt, newMessage);
        if (newStmt == null) {
            return;
        }
        newMessage.setParentNode(newStmt);
        replaceInParent(stmt, newStmt);
    }

    private Message createMessage(String text) {
        StringLiteral literal = new StringLiteral(text);
        Message message = new Message(literal);
        literal.setParentNode(message);
        return message;
    }

    private Stmt createBroadcast(Stmt original, Message message) {
        if (original instanceof Broadcast) {
            return new Broadcast(message, original.getMetadata());
        } else if (original instanceof BroadcastAndWait) {
            return new BroadcastAndWait(message, original.getMetadata());
        }
        return null;
    }

    private String getMessageText(Stmt stmt) {
        Message msg = null;
        if (stmt instanceof Broadcast b) {
            msg = b.getMessage();
        } else if (stmt instanceof BroadcastAndWait b) {
            msg = b.getMessage();
        }
        if (msg != null && msg.getMessage() instanceof StringLiteral lit) {
            return lit.getText();
        }
        return null;
    }

    /**
     * Visitor that collects broadcast statements, optionally filtering by message name.
     */
    private static class BroadcastCollector implements ScratchVisitor {
        private final Map<String, List<Stmt>> broadcastsByMessage = new HashMap<>();
        private final List<Stmt> matchingBroadcasts = new ArrayList<>();
        private final String filterMessage;

        BroadcastCollector() {
            this.filterMessage = null;
        }

        BroadcastCollector(String filterMessage) {
            this.filterMessage = filterMessage;
        }

        @Override
        public void visit(Script node) {
            // Don't skip Never-event scripts - we need to collect ALL broadcasts
            // including loose/dead code blocks so we can mutate all instances
            visitChildren(node);
        }

        @Override
        public void visit(Broadcast node) {
            collectIfMatches(node, node.getMessage());
        }

        @Override
        public void visit(BroadcastAndWait node) {
            collectIfMatches(node, node.getMessage());
        }

        private void collectIfMatches(Stmt stmt, Message message) {
            if (!(message.getMessage() instanceof StringLiteral lit)) {
                return;
            }
            String msgName = lit.getText();

            if (filterMessage == null) {
                // Collecting all broadcasts grouped by message
                broadcastsByMessage.computeIfAbsent(msgName.toLowerCase(), k -> new ArrayList<>()).add(stmt);
            } else if (msgName.equalsIgnoreCase(filterMessage)) {
                // Collecting broadcasts matching specific message
                matchingBroadcasts.add(stmt);
            }
        }

        Map<String, List<Stmt>> getBroadcastsByMessage() {
            return broadcastsByMessage;
        }

        List<Stmt> getMatchingBroadcasts() {
            return matchingBroadcasts;
        }
    }
}
