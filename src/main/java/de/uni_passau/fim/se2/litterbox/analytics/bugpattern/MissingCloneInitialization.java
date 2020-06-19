/*
 * Copyright (C) 2019 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CloneOfMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addBlockComment;

/**
 * When a sprite creates a
 * clone of itself but has no scripts started by When I start as
 * a clone or When this sprite clicked events, clones will not
 * perform any actions. The clones remain frozen until they
 * are deleted by delete this clone blocks or the program is
 * restarted.
 */
public class MissingCloneInitialization implements IssueFinder, ScratchVisitor {

    public static final String NAME = "missing_clone_initialization";
    public static final String SHORT_NAME = "mssCloneInit";
    public static final String HINT_TEXT = "missing clone initialization";

    private List<String> whenStartsAsCloneActors = new ArrayList<>();
    private List<String> clonedActors = new ArrayList<>();
    private ActorDefinition currentActor;
    private int identifierCounter;
    private boolean addComment;
    private Set<String> notClonedActor;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        whenStartsAsCloneActors = new ArrayList<>();
        clonedActors = new ArrayList<>();
        identifierCounter = 1;
        addComment = false;
        notClonedActor = new LinkedHashSet<>();
        program.accept(this);
        final List<String> uninitializingActors
                = clonedActors.stream().filter(s -> !whenStartsAsCloneActors.contains(s)).collect(Collectors.toList());
        notClonedActor = new LinkedHashSet<>(uninitializingActors);
        addComment = true;
        program.accept(this);
        return new IssueReport(NAME, uninitializingActors.size(), uninitializingActors, "");
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
    public void visit(CreateCloneOf node) {
        if (node.getStringExpr() instanceof AsString && ((AsString) node.getStringExpr()).getOperand1() instanceof StrId) {
            final String spriteName = ((StrId) ((AsString) node.getStringExpr()).getOperand1()).getName();
            if (!addComment) {
                if (spriteName.equals("_myself_")) {
                    clonedActors.add(currentActor.getIdent().getName());
                } else {
                    clonedActors.add(spriteName);
                }
            } else if (notClonedActor.contains(spriteName)) {
                addBlockComment((NonDataBlockMetadata) ((CloneOfMetadata) node.getMetadata()).getCloneBlockMetadata(), currentActor, HINT_TEXT,
                        SHORT_NAME + identifierCounter);
                identifierCounter++;
            }
        }
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
}
