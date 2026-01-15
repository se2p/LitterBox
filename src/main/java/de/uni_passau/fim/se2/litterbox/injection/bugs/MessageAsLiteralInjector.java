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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.SetStmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
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
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final IssueFinder issueFinder = new MessageAsLiteral();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        List<BroadcastInfo> broadcasts = new ArrayList<>();
        List<StringLiteral> highPriority = new ArrayList<>();
        List<StringLiteral> lowPriority = new ArrayList<>();

        program.accept(new CollectorVisitor(broadcasts, highPriority, lowPriority));

        if (broadcasts.isEmpty()) {
            return Collections.emptyList();
        }

        // Find messages sent exactly once
        Map<String, List<BroadcastInfo>> byMessage = broadcasts.stream()
                .collect(Collectors.groupingBy(b -> b.message().toLowerCase()));

        BroadcastInfo uniqueBroadcast = byMessage.values().stream()
                .filter(list -> list.size() == 1)
                .map(List::getFirst)
                .findFirst()
                .orElse(null);

        if (uniqueBroadcast == null) {
            return Collections.emptyList();
        }

        List<StringLiteral> candidates = highPriority.isEmpty() ? lowPriority : highPriority;
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // Replace the first candidate literal and remove the broadcast
        StringLiteral targetLiteral = candidates.getFirst();
        LiteralReplacementVisitor visitor = new LiteralReplacementVisitor(targetLiteral, uniqueBroadcast.message());
        Program mutated = (Program) program.accept(visitor);

        // Fix for empty script output: Calculate context strings explicitly
        String originalScript = "";
        String mutatedScript = "";
        String spriteName = "";
        String originalScriptId = "";
        String mutatedScriptId = "";

        if (visitor.firstModifiedOriginal != null && visitor.firstModifiedReplacement != null) {
            // Establish parent links in the new tree
            mutated.accept(new ParentVisitor());

            spriteName = findContainingActorName(visitor.firstModifiedOriginal);
            originalScriptId = findContainingScriptId(visitor.firstModifiedOriginal);
            if (originalScriptId.isEmpty() && uniqueBroadcast.stmt() != null) {
                // Fallback to the broadcast script if the literal is not in a script (e.g. in variable init)
                originalScriptId = findContainingScriptId(uniqueBroadcast.stmt());
            }
            originalScript = safeGetScratchBlocks(findContextNode(visitor.firstModifiedOriginal));
            ASTNode mutatedContext = findContextNode(visitor.firstModifiedReplacement);
            mutatedScript = safeGetScratchBlocks(mutatedContext);
            mutatedScriptId = findContainingScriptId(visitor.firstModifiedReplacement);
        }

        return Collections.singletonList(
                new InjectionResult(mutated, spriteName, originalScriptId, mutatedScriptId, originalScript,
                        mutatedScript));
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Overridden to handle variable initializations in SetStmtList by returning
     * the containing Stmt for better context in ScratchBlocks rendering.
     */
    @Override
    protected ASTNode findContextNode(ASTNode node) {
        if (node == null) {
            return null;
        }
        // For nodes in SetStmtList (variable initialization context), use the containing Stmt
        SetStmtList setStmtList = AstNodeUtil.findParent(node, SetStmtList.class);
        if (setStmtList != null) {
            Stmt stmt = AstNodeUtil.findParent(node, Stmt.class);
            if (stmt != null) {
                return stmt;
            }
            return setStmtList;
        }
        // Default to ActorDefinition for broader context
        return findContainingActor(node);
    }

    private String getMessageText(Expression expr) {
        if (expr instanceof AsString asString) {
            return getMessageText(asString.getOperand1());
        }
        if (expr instanceof StringLiteral lit) {
            return lit.getText();
        }
        if (expr instanceof LocalIdentifier ident) {
            return ident.getName();
        }
        return null;
    }

    private record BroadcastInfo(Stmt stmt, String message) {
    }

    private class CollectorVisitor implements ScratchVisitor {
        private final List<BroadcastInfo> broadcasts;
        private final List<StringLiteral> highPriority;
        private final List<StringLiteral> lowPriority;

        CollectorVisitor(
                List<BroadcastInfo> broadcasts, List<StringLiteral> highPriority, List<StringLiteral> lowPriority) {
            this.broadcasts = broadcasts;
            this.highPriority = highPriority;
            this.lowPriority = lowPriority;
        }

        @Override
        public void visit(Broadcast node) {
            String msg = getMessageText(node.getMessage().getMessage());
            if (msg != null) {
                broadcasts.add(new BroadcastInfo(node, msg));
            }
        }

        @Override
        public void visit(BroadcastAndWait node) {
            String msg = getMessageText(node.getMessage().getMessage());
            if (msg != null) {
                broadcasts.add(new BroadcastInfo(node, msg));
            }
        }

        @Override
        public void visit(StringLiteral node) {
            ASTNode parent = node.getParentNode();
            if (parent instanceof AsString asString) {
                parent = asString.getParentNode();
            }

            if (isHighPrioritySite(parent)) {
                highPriority.add(node);
            } else if (isLowPrioritySite(parent)) {
                lowPriority.add(node);
            }
        }

        private boolean isHighPrioritySite(ASTNode parent) {
            return parent instanceof AddTo || parent instanceof InsertAt || parent instanceof ReplaceItem
                    || parent instanceof SetVariableTo || parent instanceof Equals;
        }

        private boolean isLowPrioritySite(ASTNode parent) {
            return parent instanceof Say || parent instanceof SayForSecs
                    || parent instanceof Think || parent instanceof ThinkForSecs
                    || parent instanceof AskAndWait;
        }

        @Override
        public void visit(LocalIdentifier node) {
            // Avoid child literals
        }

        @Override
        public void visit(ReceptionOfMessage node) {
            // Avoid message literals
        }
    }

    /**
     * Custom CloneVisitor that performs two operations in a single pass:
     * 1. Replaces a StringLiteral with a message name
     * 2. Removes the broadcast statement for that message
     *
     * <p>
     * This requires a custom visitor because the standard mutation patterns
     * ({@link #modifyClonedNode} and {@link #replaceNodeImmutably}) cannot perform
     * both operations atomically in a single clone operation.
     */
    private class LiteralReplacementVisitor extends CloneVisitor {
        private final String messageToRemove;
        private final String originalText;
        private boolean removed = false;
        private boolean replaced = false;

        // Track nodes to identify context for CSV output
        ASTNode firstModifiedOriginal = null;
        ASTNode firstModifiedReplacement = null;

        LiteralReplacementVisitor(StringLiteral literal, String newText) {
            this.messageToRemove = newText;
            this.originalText = literal.getText();
        }

        @Override
        public ASTNode visit(StmtList node) {
            List<Stmt> stmts = new ArrayList<>();
            for (Stmt s : node.getStmts()) {
                Stmt cloned = apply(s);
                if (cloned != null) {
                    stmts.add(cloned);
                }
            }
            return new StmtList(stmts);
        }

        @Override
        public ASTNode visit(Broadcast node) {
            if (shouldRemove(node.getMessage().getMessage())) {
                return null;
            }
            return new Broadcast(cloner.apply(node.getMessage()), apply(node.getMetadata()));
        }

        @Override
        public ASTNode visit(BroadcastAndWait node) {
            if (shouldRemove(node.getMessage().getMessage())) {
                return null;
            }
            return new BroadcastAndWait(cloner.apply(node.getMessage()), apply(node.getMetadata()));
        }

        private boolean shouldRemove(StringExpr msgExpr) {
            if (!removed && msgExpr instanceof StringLiteral lit && lit.getText().equalsIgnoreCase(messageToRemove)) {
                removed = true;
                return true;
            }
            // Add fallback for unwrapping AsString if needed here too, though Broadcast visitor logic likely covers it
            // but `node.getMessage().getMessage()` is passed here.
            return false;
        }

        @Override
        public ASTNode visit(StringLiteral node) {
            if (!replaced && node.getText().equals(originalText)) {
                replaced = true;
                StringLiteral replacement = new StringLiteral(messageToRemove);
                firstModifiedOriginal = node;
                firstModifiedReplacement = replacement;
                return replacement;
            }
            return super.visit(node);
        }
    }
}
