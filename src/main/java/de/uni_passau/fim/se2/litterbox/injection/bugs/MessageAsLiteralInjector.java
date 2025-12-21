/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.AddTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.InsertAt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.ReplaceItem;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Think;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ThinkForSecs;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Injects the "Message As Literal" bug pattern by replacing a StringLiteral in an existing
 * block with a message name, then removing the broadcast that sends that message.
 *
 * <p>This injector:
 * <ol>
 *   <li>Finds a broadcast statement with a message name</li>
 *   <li>Finds an existing block with a StringLiteral parameter (comparisons, list operations, etc.)</li>
 *   <li>Replaces that StringLiteral with the message name</li>
 *   <li>Removes the broadcast statement</li>
 * </ol>
 *
 * <p>Candidate blocks are prioritized as:
 * <ul>
 *   <li>High priority: Comparisons (Equals), List operations, SetVariableTo</li>
 *   <li>Low priority: Say, SayForSecs, Think, ThinkForSecs, AskAndWait</li>
 * </ul>
 *
 * <p>This creates a bug because the message is defined (via "when I receive") but never sent,
 * and the message name appears as a StringLiteral somewhere in the code.
 */
public class MessageAsLiteralInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new MessageAsLiteral();
    }

    @Override
    public List<Program> inject(Program program) {
        // Collect broadcasts and candidate StringLiterals (prioritized)
        List<BroadcastInfo> broadcasts = new ArrayList<>();
        List<StringLiteral> highPriorityCandidates = new ArrayList<>();  // List ops, Ask
        List<StringLiteral> lowPriorityCandidates = new ArrayList<>();   // Say, Think

        program.accept(new CollectorVisitor(broadcasts, highPriorityCandidates, lowPriorityCandidates));

        if (broadcasts.isEmpty()) {
            return Collections.emptyList();
        }

        // Use high priority candidates first, fall back to low priority
        List<StringLiteral> candidates = highPriorityCandidates.isEmpty()
                ? lowPriorityCandidates
                : highPriorityCandidates;

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // Use the first broadcast and first candidate literal
        BroadcastInfo broadcast = broadcasts.getFirst();
        StringLiteral targetLiteral = candidates.getFirst();

        // Clone with transformation
        TransformingCloneVisitor visitor = new TransformingCloneVisitor(
                broadcast.stmt, targetLiteral, broadcast.message);
        Program mutated = (Program) program.accept(visitor);

        return Collections.singletonList(mutated);
    }

    private String extractMessageContent(StringExpr expr) {
        if (expr instanceof StringLiteral literal) {
            return literal.getText();
        } else if (expr instanceof LocalIdentifier identifier) {
            return identifier.getName();
        }
        return null;
    }

    private record BroadcastInfo(Stmt stmt, String message) {
    }

    /**
     * Visitor to collect broadcasts and candidate StringLiterals (prioritized).
     */
    private class CollectorVisitor implements ScratchVisitor {
        private final List<BroadcastInfo> broadcasts;
        private final List<StringLiteral> highPriority;
        private final List<StringLiteral> lowPriority;

        CollectorVisitor(List<BroadcastInfo> broadcasts,
                         List<StringLiteral> highPriority,
                         List<StringLiteral> lowPriority) {
            this.broadcasts = broadcasts;
            this.highPriority = highPriority;
            this.lowPriority = lowPriority;
        }

        @Override
        public void visit(Broadcast node) {
            String msg = extractMessageContent(node.getMessage().getMessage());
            if (msg != null) {
                broadcasts.add(new BroadcastInfo(node, msg));
            }
            // Don't visit children - we don't want the message literal itself
        }

        @Override
        public void visit(BroadcastAndWait node) {
            String msg = extractMessageContent(node.getMessage().getMessage());
            if (msg != null) {
                broadcasts.add(new BroadcastInfo(node, msg));
            }
        }

        // High priority: List operations
        @Override
        public void visit(AddTo node) {
            if (node.getString() instanceof StringLiteral lit) {
                highPriority.add(lit);
            }
            visitChildren(node);
        }

        @Override
        public void visit(InsertAt node) {
            if (node.getString() instanceof StringLiteral lit) {
                highPriority.add(lit);
            }
            visitChildren(node);
        }

        @Override
        public void visit(ReplaceItem node) {
            if (node.getString() instanceof StringLiteral lit) {
                highPriority.add(lit);
            }
            visitChildren(node);
        }

        // High priority: SetVariableTo
        @Override
        public void visit(SetVariableTo node) {
            if (node.getExpr() instanceof StringLiteral lit) {
                highPriority.add(lit);
            }
            visitChildren(node);
        }

        // Low priority: Say/Think variants
        @Override
        public void visit(Say node) {
            if (node.getString() instanceof StringLiteral lit) {
                lowPriority.add(lit);
            }
            visitChildren(node);
        }

        @Override
        public void visit(SayForSecs node) {
            if (node.getString() instanceof StringLiteral lit) {
                lowPriority.add(lit);
            }
            visitChildren(node);
        }

        @Override
        public void visit(Think node) {
            if (node.getThought() instanceof StringLiteral lit) {
                lowPriority.add(lit);
            }
            visitChildren(node);
        }

        @Override
        public void visit(ThinkForSecs node) {
            if (node.getThought() instanceof StringLiteral lit) {
                lowPriority.add(lit);
            }
            visitChildren(node);
        }

        // Low priority: Ask
        @Override
        public void visit(AskAndWait node) {
            if (node.getQuestion() instanceof StringLiteral lit) {
                lowPriority.add(lit);
            }
            visitChildren(node);
        }

        // High priority: Comparisons with StringLiterals
        @Override
        public void visit(Equals node) {
            if (node.getOperand1() instanceof StringLiteral lit) {
                highPriority.add(lit);
            }
            if (node.getOperand2() instanceof StringLiteral lit) {
                highPriority.add(lit);
            }
            visitChildren(node);
        }
    }

    /**
     * Clone visitor that removes the broadcast and replaces the target literal's text.
     */
    private static class TransformingCloneVisitor extends CloneVisitor {
        private final Stmt broadcastToRemove;
        private final StringLiteral literalToReplace;
        private final String newText;

        TransformingCloneVisitor(Stmt broadcastToRemove, StringLiteral literalToReplace, String newText) {
            this.broadcastToRemove = broadcastToRemove;
            this.literalToReplace = literalToReplace;
            this.newText = newText;
        }

        @Override
        public ASTNode visit(StmtList node) {
            List<Stmt> newStmts = new ArrayList<>();
            for (Stmt stmt : node.getStmts()) {
                // Skip the broadcast we want to remove
                if (stmt == broadcastToRemove) {
                    continue;
                }
                newStmts.add(apply(stmt));
            }
            return new StmtList(newStmts);
        }

        @Override
        public ASTNode visit(StringLiteral node) {
            if (node == literalToReplace) {
                return new StringLiteral(newText);
            }
            return super.visit(node);
        }
    }
}
