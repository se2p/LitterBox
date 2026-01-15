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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageNeverReceived;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.util.TypoGenerator;

import java.util.*;

/**
 * Injects the "Message Never Received" bug by introducing a typo into ALL
 * "when I receive [message]" event handlers for a given message, causing them
 * to listen for a message that is never broadcast.
 *
 * <p>The MessageNeverReceived bug finder checks if a sent message is received
 * by ANY reception block. Therefore, we must change ALL reception blocks for
 * a given message in a single mutation to properly inject the bug.</p>
 */
public class MessageNeverReceivedInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new MessageNeverReceived();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Program node) {
        // Collect all sent messages (from Broadcast/BroadcastAndWait)
        Set<String> sentMessages = collectSentMessages(node);

        // Collect all ReceptionOfMessage events grouped by message name (case-insensitive)
        Map<String, List<ReceptionOfMessage>> receptionsByMessage = new HashMap<>();
        node.accept(new ScratchVisitor() {
            @Override
            public void visit(ReceptionOfMessage event) {
                if (event.getMsg().getMessage() instanceof StringLiteral literal) {
                    String msgName = literal.getText().toLowerCase();
                    receptionsByMessage.computeIfAbsent(msgName, k -> new ArrayList<>()).add(event);
                }
            }
        });

        // For each unique message that IS sent, create ONE mutation that changes ALL reception blocks
        // No point in changing receivers for messages that are already not sent
        for (Map.Entry<String, List<ReceptionOfMessage>> entry : receptionsByMessage.entrySet()) {
            if (sentMessages.contains(entry.getKey())) {
                List<ReceptionOfMessage> receptions = entry.getValue();
                modifyClonedNode(receptions.getFirst(), clonedFirst -> {
                    applyTypoToAllReceptions(clonedFirst);
                });
            }
        }
    }

    /**
     * Collects all message names that are broadcast (case-insensitive).
     */
    private Set<String> collectSentMessages(Program program) {
        Set<String> messages = new HashSet<>();
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(Broadcast node) {
                if (node.getMessage().getMessage() instanceof StringLiteral lit) {
                    messages.add(lit.getText().toLowerCase());
                }
            }

            @Override
            public void visit(BroadcastAndWait node) {
                if (node.getMessage().getMessage() instanceof StringLiteral lit) {
                    messages.add(lit.getText().toLowerCase());
                }
            }
        });
        return messages;
    }

    /**
     * Applies a typo to the first cloned reception, then finds and updates
     * all other reception blocks in the cloned program that listen for the same message.
     */
    private void applyTypoToAllReceptions(ReceptionOfMessage clonedFirst) {
        // Get the original message text from the cloned node
        if (!(clonedFirst.getMsg().getMessage() instanceof StringLiteral literal)) {
            return;
        }
        String originalMsg = literal.getText();
        String typoMsg = TypoGenerator.generateTypo(originalMsg);

        // Find the cloned Program root
        Program clonedProgram = findProgramRoot(clonedFirst);
        if (clonedProgram == null) {
            return;
        }

        // Collect all ReceptionOfMessage nodes in the cloned program that have this message
        List<ReceptionOfMessage> allClonedReceptions = new ArrayList<>();
        clonedProgram.accept(new ScratchVisitor() {
            @Override
            public void visit(ReceptionOfMessage event) {
                if (event.getMsg().getMessage() instanceof StringLiteral lit
                        && lit.getText().equalsIgnoreCase(originalMsg)) {
                    allClonedReceptions.add(event);
                }
            }
        });

        // Apply the typo to ALL matching reception blocks
        for (ReceptionOfMessage clonedEvent : allClonedReceptions) {
            applyTypoToSingleReception(clonedEvent, typoMsg);
        }
    }

    /**
     * Applies a typo to a single ReceptionOfMessage node.
     */
    private void applyTypoToSingleReception(ReceptionOfMessage clonedEvent, String typoMsg) {
        // Build the new message with typo
        StringLiteral typoMsgLiteral = new StringLiteral(typoMsg);
        Message newMessage = new Message(typoMsgLiteral);
        typoMsgLiteral.setParentNode(newMessage);

        // Create new ReceptionOfMessage with the typo message
        ReceptionOfMessage newEvent = new ReceptionOfMessage(newMessage, clonedEvent.getMetadata());
        newMessage.setParentNode(newEvent);

        // Navigate up to find the Script and ScriptList to perform the replacement
        ASTNode parent = clonedEvent.getParentNode();
        if (!(parent instanceof Script clonedScript)) {
            return;
        }

        ASTNode grandparent = clonedScript.getParentNode();
        if (!(grandparent instanceof ScriptList scriptList)) {
            return;
        }

        // Create new Script with the modified event (Script is immutable)
        Script newScript = new Script(newEvent, clonedScript.getStmtList());
        newEvent.setParentNode(newScript);
        clonedScript.getStmtList().setParentNode(newScript);
        newScript.setParentNode(scriptList);

        // Replace the old script with the new one in the ScriptList
        int index = scriptList.getScriptList().indexOf(clonedScript);
        if (index != -1) {
            scriptList.getScriptList().set(index, newScript);
        }
    }
}

