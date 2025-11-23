/*
 * Copyright (C) 2019-2024 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MessageAsLiteral extends AbstractIssueFinder {

    private Set<String> sentMessages = new LinkedHashSet<>();
    private List<StringLiteral> potentialLiterals = new ArrayList<>();

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        sentMessages = new LinkedHashSet<>();
        potentialLiterals = new ArrayList<>();

        program.accept(this);

        Set<String> definedMessages = new LinkedHashSet<>(program.getSymbolTable().getMessages().keySet());
        Set<String> unsentMessages = new LinkedHashSet<>(definedMessages);
        unsentMessages.removeAll(sentMessages);

        for (StringLiteral node : potentialLiterals) {
            String text = node.getText();
            if (unsentMessages.contains(text)) {
                IssueBuilder builder = prepareIssueBuilder().withSeverity(IssueSeverity.HIGH).withMetadata(getMetadata(node));
                Hint hint = Hint.fromKey(getName());
                hint.setParameter(Hint.HINT_MESSAGE, text);
                builder = builder.withCurrentNode(getCurrentReferencableNode(node));
                addIssue(builder.withHint(hint));
            }
        }

        return issues;
    }

    @Override
    public void visit(Broadcast node) {
        if (node.getMessage().getMessage() instanceof StringLiteral stringLiteral) {
            sentMessages.add(stringLiteral.getText());
        }
        // Do not visit children to avoid flagging the message name itself as a literal usage
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (node.getMessage().getMessage() instanceof StringLiteral stringLiteral) {
            sentMessages.add(stringLiteral.getText());
        }
        // Do not visit children
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        // Do not visit the message part
    }

    @Override
    public void visit(LocalIdentifier node) {
        // Do not visit children (StringLiteral) of identifiers
    }

    @Override
    public void visit(StringLiteral node) {
        // If we reach here, it's not inside a Broadcast/Receive message slot
        potentialLiterals.add(node);
    }

    private ASTNode getCurrentReferencableNode(ASTNode node) {
        if (node instanceof Program) {
            throw new IllegalArgumentException("should have found referencable node before Program node");
        }
        if (node instanceof ActorDefinition) {
            return node;
        }
        if (AstNodeUtil.getBlockId(node) != null) {
            return node;
        } else {
            return getCurrentReferencableNode(node.getParentNode());
        }
    }

    private Metadata getMetadata(ASTNode node) {
        if (node instanceof Program) {
            throw new IllegalArgumentException("should have found Metadata before Program node");
        }
        if (node instanceof ActorDefinition) {
            return ((ActorDefinition) node).getActorMetadata();
        }
        if (node.getMetadata() != null && !(node.getMetadata() instanceof NoBlockMetadata)) {
            return node.getMetadata();
        } else {
            return getMetadata(node.getParentNode());
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public String getName() {
        return "message_as_literal";
    }
}
