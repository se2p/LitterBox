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
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.SpriteClicked;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CloneOfMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    private List<String> whenStartsAsCloneActors = new ArrayList<>();
    private List<String> clonedActors = new ArrayList<>();
    private boolean addComment;
    private Set<String> notClonedActor;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        this.program = program;
        issues = new LinkedHashSet<>();
        whenStartsAsCloneActors = new ArrayList<>();
        clonedActors = new ArrayList<>();
        addComment = false;
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
                addIssue(node, ((CloneOfMetadata) node.getMetadata()).getCloneBlockMetadata());
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

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
