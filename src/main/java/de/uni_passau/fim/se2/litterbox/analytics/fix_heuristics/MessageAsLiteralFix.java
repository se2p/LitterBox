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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MessageAsLiteral;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Fix heuristic for the MessageAsLiteral bug.
 *
 * <p>The bug is reported when a string literal matches a message name but isn't used in broadcast.
 * The bug is fixed if the message is now properly sent or the literal changed.
 */
public class MessageAsLiteralFix extends AbstractFixHeuristic {

    private Set<String> sentMessages;
    private StringLiteral foundLiteral;

    private final IssueFinder relatedBugFinder = new MessageAsLiteral();

    public MessageAsLiteralFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }


    @Override
    public void visit(Broadcast node) {
        if (node.getMessage().getMessage() instanceof StringLiteral stringLiteral) {
            sentMessages.add(stringLiteral.getText());
        }
        super.visit(node);
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (node.getMessage().getMessage() instanceof StringLiteral stringLiteral) {
            sentMessages.add(stringLiteral.getText());
        }
        super.visit(node);
    }

    @Override
    public void visit(StringLiteral node) {
        if (matchesBugLocation(node)) {
            foundLiteral = node;
        }
    }

    @Override
    public Set<Issue> check(Program program) {
        sentMessages = new LinkedHashSet<>();
        foundLiteral = null;

        super.check(program);

        if (foundLiteral != null) {
            String text = foundLiteral.getText();
            Set<String> definedMessages = program.getSymbolTable().getMessages().keySet();

            // Fixed if: !defined OR sent
            boolean isDefined = definedMessages.contains(text);
            boolean isSent = sentMessages.contains(text);

            if (!isDefined || isSent) {
                addIssue(foundLiteral, getMetadata(foundLiteral));
            }
        }

        return issues;
    }

    private de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata getMetadata(ASTNode node) {
        if (node.getMetadata() != null) {
            return node.getMetadata();
        }
        if (node.getParentNode() != null) {
            return getMetadata(node.getParentNode());
        }
        return null;
    }
}
