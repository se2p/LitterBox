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

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addBlockComment;


import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * If the When I start as a clone event handler is used to start a script, but the sprite is never cloned,
 * the event will never be triggered and the script is dead.
 */
public class MissingCloneCall implements IssueFinder, ScratchVisitor {
    public static final String NAME = "missing_clone_call";
    public static final String SHORT_NAME = "mssCloneCll";
    public static final String HINT_TEXT = "missing clone call";
    private static final String NOTE1 = "There are no sprites with missing clone calls in your project.";
    private static final String NOTE2 = "Some of the sprites contain missing clone calls.";
    private List<String> whenStartsAsCloneActors = new ArrayList<>();
    private List<String> clonedActors = new ArrayList<>();
    private ActorDefinition currentActor;
    private Set<Issue> issues = new LinkedHashSet<>();
    private int identifierCounter;
    private boolean addComment;
    private Set<String> notClonedActor;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        whenStartsAsCloneActors = new ArrayList<>();
        clonedActors = new ArrayList<>();
        identifierCounter = 1;
        addComment = false;
        notClonedActor = new LinkedHashSet<>();
        program.accept(this);
        final List<String> uninitializingActors
                = whenStartsAsCloneActors.stream().filter(s -> !clonedActors.contains(s)).collect(Collectors.toList());
        notClonedActor = new LinkedHashSet<>(uninitializingActors);
        addComment = true;
        program.accept(this);

        return issues;
        // return new IssueReport(NAME, uninitializingActors.size(), uninitializingActors, "");
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
        if(!addComment) {
            if (node.getStringExpr() instanceof AsString
                    && ((AsString) node.getStringExpr()).getOperand1() instanceof StrId) {

                final String spriteName = ((StrId) ((AsString) node.getStringExpr()).getOperand1()).getName();
                if (spriteName.equals("_myself_")) {
                    clonedActors.add(currentActor.getIdent().getName());
                } else {
                    clonedActors.add(spriteName);
                }
            }
        }
    }

    @Override
    public void visit(Script node) {
        if (node.getStmtList().getStmts().size() > 0 && node.getEvent() instanceof StartedAsClone) {
            if(!addComment) {
                whenStartsAsCloneActors.add(currentActor.getIdent().getName());
            }else if (notClonedActor.contains(currentActor.getIdent().getName())) {
                StartedAsClone event= (StartedAsClone) node.getEvent();
                addBlockComment((NonDataBlockMetadata) event.getMetadata(), currentActor, HINT_TEXT,
                        SHORT_NAME + identifierCounter);
                identifierCounter++;
                issues.add(new Issue(this, currentActor, node));
            }
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
