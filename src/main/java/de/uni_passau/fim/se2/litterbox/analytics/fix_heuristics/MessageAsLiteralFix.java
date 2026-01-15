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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageAsLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.report.IssueDTO;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fix heuristic for the MessageAsLiteral bug.
 *
 * <p>The bug is reported when a string literal matches a message name but isn't used in broadcast.
 * The bug is fixed if the message is now properly sent or the literal changed.
 */
public class MessageAsLiteralFix extends AbstractFixHeuristic {

    private Set<String> sentMessages;
    private StringLiteral foundLiteral;
    // Context captured when foundLiteral was found (for addFix)
    private ActorDefinition foundActor;
    private Script foundScript;
    private ProcedureDefinition foundProcedure;

    private final MessageAsLiteral relatedBugFinder = new MessageAsLiteral();

    public MessageAsLiteralFix(IssueDTO originalIssue) {
        super(originalIssue);
    }

    @Override
    protected void resetState() {
        sentMessages = new LinkedHashSet<>();
        foundLiteral = null;
        foundActor = null;
        foundScript = null;
        foundProcedure = null;
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    @Override
    public void visit(ActorDefinition node) {
        if (getBugLocationBlockId() == null && getOriginalIssue().sprite() != null
                && node.getIdent().getName().equals(getOriginalIssue().sprite())) {
            foundActor = node;
        }
        super.visit(node);
    }

    @Override
    public void visit(Broadcast node) {
        if (node.getMessage().getMessage() instanceof StringLiteral stringLiteral) {
            sentMessages.add(stringLiteral.getText());
        }
        visitChildren(node);
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (node.getMessage().getMessage() instanceof StringLiteral stringLiteral) {
            sentMessages.add(stringLiteral.getText());
        }
        visitChildren(node);
    }

    @Override
    public void visit(StringLiteral node) {
        boolean match = false;

        if (getBugLocationBlockId() != null) {
            // Bug finder reports on parent node (Say, Think, etc.) which has block ID,
            // not on StringLiteral which has no block ID. Check ancestors too.
            if (matchesBugLocationOrAncestor(node)) {
                match = true;
            }
        } else {
            // If block ID is null (e.g. ActorDefinition context or serialization issue),
            // check actor and message text from hint.
            if (currentActor != null && getOriginalIssue().sprite() != null
                    && currentActor.getIdent().getName().equals(getOriginalIssue().sprite())) {
                String bugText = getMessageFromHint();
                if (node.getText().equalsIgnoreCase(bugText)) {
                    match = true;
                }
            }
        }

        if (match) {
            foundLiteral = node;
            // Capture context for later use in addFix
            foundActor = currentActor;
            foundScript = currentScript;
            foundProcedure = currentProcedure;
        }
    }

    private String getMessageFromHint() {
        String hint = getOriginalIssue().hint();
        if (hint == null) {
            return null;
        }

        // Format 1: Raw toString() format - MESSAGE=Value[value=SOME MESSAGE]
        Pattern p1 = java.util.regex.Pattern.compile("MESSAGE=Value\\[value=(.*?)\\]");
        Matcher m1 = p1.matcher(hint);
        if (m1.find()) {
            return m1.group(1);
        }

        // Format 2: Translated format - broadcast (MESSAGE v) or broadcast (MESSAGE v) and wait
        Pattern p2 = java.util.regex.Pattern.compile("broadcast \\(([^)]+) v\\)");
        Matcher m2 = p2.matcher(hint);
        if (m2.find()) {
            return m2.group(1);
        }

        return null;
    }

    @Override
    public Set<FixResult> check(Program program) {
        Set<FixResult> results = super.check(program);
        if (!results.isEmpty()) {
            return results;
        }

        if (foundLiteral != null && program.getSymbolTable() != null
                && program.getSymbolTable().getMessages() != null) {
            String text = foundLiteral.getText();
            Set<String> definedMessages = program.getSymbolTable().getMessages().keySet();

            // Fixed if: !defined OR sent
            boolean isDefined = definedMessages.contains(text);
            boolean isSent = sentMessages.contains(text);

            if (!isDefined || isSent) {
                // Use explicit context overload instead of mutating parent fields
                addFix(foundLiteral, getMetadata(foundLiteral),
                        foundActor, foundScript != null ? foundScript : foundProcedure);
            }
        } else if (foundLiteral == null && foundActor != null && getBugLocationBlockId() == null
                && getMessageFromHint() != null) {
            // If we targeted a specific literal by hint, and didn't find it in the actor,
            // it implies the literal was removed or replaced (Fixed).
            addFix(foundActor, getMetadata(foundActor), foundActor, null);
        }

        return Collections.unmodifiableSet(fixes);
    }

    private Metadata getMetadata(ASTNode node) {
        if (node.getMetadata() != null) {
            return node.getMetadata();
        }
        if (node.getParentNode() != null) {
            return getMetadata(node.getParentNode());
        }
        return null;
    }
}
