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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageNeverSent;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.util.BlockIdNodeFinder;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Fix heuristic for the MessageNeverSent bug.
 *
 * <p>The bug is reported when a message handler exists but the message is never sent.
 * The bug is fixed if the message is now sent.
 */
public class MessageNeverSentFix extends AbstractFixHeuristic {
    private final MessageNeverSent relatedBugFinder = new MessageNeverSent();

    public MessageNeverSentFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public Set<FixResult> check(Program program) {
        Preconditions.checkNotNull(program);

        Set<FixResult> results = super.check(program);
        if (!results.isEmpty()) {
            return results;
        }

        // 1. Find the buggy node using the block ID
        Optional<BlockIdNodeFinder.NodeContext> bugContextOpt =
                BlockIdNodeFinder.find(program, getBugLocationBlockId());

        // If not empty (handled by super), get node
        if (bugContextOpt.isPresent()) {
            ASTNode buggyNode = bugContextOpt.get().node();

            // Handle case where BlockIdNodeFinder returns the Script instead of the Event
            if (buggyNode instanceof Script script) {
                buggyNode = script.getEvent();
            }

            if (buggyNode instanceof ReceptionOfMessage receptionNode) {
                // 2. Extract the message name
                String targetMessage = null;
                if (scriptNotEmpty(receptionNode.getParentNode())) {
                    Expression msgExpr = receptionNode.getMsg().getMessage();
                    if (msgExpr instanceof StringLiteral text) {
                        targetMessage = text.getText();
                    } else if (msgExpr instanceof StrId strId) {
                        targetMessage = strId.getName();
                    }
                }

                if (targetMessage != null) {
                    // 3. Search for a valid sender for this specific message
                    ValidSenderFinder senderFinder = new ValidSenderFinder(targetMessage);
                    program.accept(senderFinder);

                    if (senderFinder.foundSender != null) {
                        ASTNode sender = senderFinder.foundSender;
                        currentActor = AstNodeUtil.findParent(sender, ActorDefinition.class);
                        ScriptEntity scriptEntity = AstNodeUtil.findParent(sender, ScriptEntity.class);

                        if (scriptEntity instanceof Script script) {
                            currentScript = script;
                            currentProcedure = null;
                        } else if (scriptEntity instanceof ProcedureDefinition proc) {
                            currentProcedure = proc;
                            currentScript = null;
                        }

                        addFix(receptionNode, receptionNode.getMetadata());
                    }
                }
            }
        }

        return Collections.unmodifiableSet(fixes);
    }

    private boolean scriptNotEmpty(ASTNode parentNode) {
        if (!(parentNode instanceof Script script)) {
            return false;
        }
        return script.getStmtList().hasStatements();
    }

    private static class ValidSenderFinder implements ScratchVisitor {
        private final String targetMessage;
        ASTNode foundSender = null;

        public ValidSenderFinder(String targetMessage) {
            this.targetMessage = targetMessage;
        }

        @Override
        public void visit(ASTNode node) {
            // Default traversal for all nodes
            if (foundSender == null) {
                visitChildren(node);
            }
        }

        @Override
        public void visit(Broadcast node) {
            if (foundSender == null && matchesMessage(node.getMessage().getMessage())) {
                foundSender = node;
            }
            // Don't traverse deeper into Broadcast's children
        }

        @Override
        public void visit(BroadcastAndWait node) {
            if (foundSender == null && matchesMessage(node.getMessage().getMessage())) {
                foundSender = node;
            }
            // Don't traverse deeper into BroadcastAndWait's children
        }

        private boolean matchesMessage(Expression msgExpr) {
            if (msgExpr instanceof StringLiteral text) {
                // Use case-insensitive comparison to match bug finder behavior
                return text.getText().equalsIgnoreCase(targetMessage);
            }
            // ScratchBlocks parser may produce StrId for broadcast (msg v) syntax
            if (msgExpr instanceof StrId strId) {
                return strId.getName().equalsIgnoreCase(targetMessage);
            }
            return false;
        }
    }
}
