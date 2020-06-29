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

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CloneOfMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Script starting with a When I start as a clone event handler that contain a create clone of
 * myself block may result in an infinite recursion.
 */
public class RecursiveCloning implements ScratchVisitor, IssueFinder {
    public static final String NAME = "recursive_cloning";
    public static final String SHORT_NAME = "recClone";
    public static final String HINT_TEXT = "recursive cloning";
    private boolean startAsClone = false;
    private Set<Issue> issues = new LinkedHashSet<>();
    private ActorDefinition currentActor;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        startAsClone = false;
        program.accept(this);
        return issues;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        for (ASTNode child : actor.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof StartedAsClone) {
            startAsClone = true;
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        startAsClone = false;
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (startAsClone) {
            if (node.getStringExpr() instanceof AsString
                    && ((AsString) node.getStringExpr()).getOperand1() instanceof StrId) {

                final String spriteName = ((StrId) ((AsString) node.getStringExpr()).getOperand1()).getName();

                if (spriteName.equals("_myself_")) {
                    CloneOfMetadata metadata = (CloneOfMetadata) node.getMetadata();
                    issues.add(new Issue(this, currentActor, node,
                            HINT_TEXT, metadata.getCloneBlockMetadata()));
                }
            }
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }
}
