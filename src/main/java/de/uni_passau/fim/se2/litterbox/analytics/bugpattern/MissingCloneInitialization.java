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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CloneOfMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * When a sprite creates a
 * clone of itself but has no scripts started by When I start as
 * a clone or When this sprite clicked events, clones will not
 * perform any actions. The clones remain frozen until they
 * are deleted by delete this clone blocks or the program is
 * restarted.
 */
public class MissingCloneInitialization extends AbstractIssueFinder {

    public static final String NAME = "missing_clone_initialization";
    public static final String HAS_DELETE_CLONE = "missing_clone_initialization_delete_clone";
    public static final String HAS_DELETE_CLONE_MESSAGE = "missing_clone_initialization_delete_clone_message";

    private List<String> whenStartsAsCloneActors = new ArrayList<>();
    private List<String> clonedActors = new ArrayList<>();
    private boolean addComment;
    private Set<String> notClonedActor;
    private boolean hasDeleteClone;
    private Map<String, Set<String>> deleteCloneSpritesAndMessages;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        whenStartsAsCloneActors = new ArrayList<>();
        clonedActors = new ArrayList<>();
        addComment = false;
        hasDeleteClone = false;
        deleteCloneSpritesAndMessages = new LinkedHashMap<>();
        notClonedActor = new LinkedHashSet<>();
        program.accept(this);
        final List<String> uninitializingActors
                = clonedActors.stream()
                .filter(s -> !whenStartsAsCloneActors.contains(s))
                .collect(Collectors.toList());
        notClonedActor = new LinkedHashSet<>(uninitializingActors);
        addComment = true;
        program.accept(this);
        return issues;
    }

    @Override
    public void visit(Script node) {
        super.visit(node);
        if (!addComment && hasDeleteClone) {
            Set<String> messages;
            if (!deleteCloneSpritesAndMessages.containsKey(currentActor.getIdent().getName())) {
                messages = new LinkedHashSet<>();
                deleteCloneSpritesAndMessages.put(currentActor.getIdent().getName(), messages);
            }
            if (node.getEvent() instanceof ReceptionOfMessage) {
                messages = deleteCloneSpritesAndMessages.get(currentActor.getIdent().getName());
                messages.add(((StringLiteral) ((ReceptionOfMessage) node.getEvent()).getMsg().getMessage()).getText());
            }
        }
        hasDeleteClone = false;
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (node.getStringExpr() instanceof AsString
                && ((AsString) node.getStringExpr()).getOperand1() instanceof StrId) {

            final String spriteName = ((StrId) ((AsString) node.getStringExpr()).getOperand1()).getName();
            if (!addComment) {
                if (spriteName.equals("_myself_")) {
                    clonedActors.add(currentActor.getIdent().getName());
                } else {
                    clonedActors.add(spriteName);
                }
            } else if (notClonedActor.contains(spriteName)) {
                Hint hint = generateHint(spriteName);
                addIssue(node, ((CloneOfMetadata) node.getMetadata()).getCloneBlockMetadata(), hint);
            } else if (spriteName.equals("_myself_") && notClonedActor.contains(currentActor.getIdent().getName())) {
                Hint hint = generateHint(currentActor.getIdent().getName());
                addIssue(node, ((CloneOfMetadata) node.getMetadata()).getCloneBlockMetadata(), hint);
            }
        }
    }

    private Hint generateHint(String actorName) {
        Hint hint;
        if (deleteCloneSpritesAndMessages.containsKey(actorName)) {
            Set<String> messages = deleteCloneSpritesAndMessages.get(actorName);
            if (messages.size() > 0) {
                hint = new Hint(HAS_DELETE_CLONE_MESSAGE);
                hint.setParameter(Hint.HINT_MESSAGE, generateMessageString(messages));
            } else {
                hint = new Hint(HAS_DELETE_CLONE);
            }
            hint.setParameter(Hint.HINT_SPRITE, actorName);
        } else {
            hint = new Hint(NAME);
        }
        return hint;
    }

    private String generateMessageString(Set<String> messages) {
        StringBuilder builder = new StringBuilder();
        int i = 1;
        for (String string : messages) {
            builder.append(string);
            if (i < messages.size()) {
                builder.append(", ");
            }
            i++;
        }
        return builder.toString();
    }

    @Override
    public void visit(StartedAsClone node) {
        if (!addComment) {
            whenStartsAsCloneActors.add(currentActor.getIdent().getName());
        }
    }

    @Override
    public void visit(SpriteClicked node) {
        if (!addComment) {
            whenStartsAsCloneActors.add(currentActor.getIdent().getName());
        }
    }

    @Override
    public void visit(DeleteClone node) {
        hasDeleteClone = true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(HAS_DELETE_CLONE);
        keys.add(HAS_DELETE_CLONE_MESSAGE);
        return keys;
    }
}
