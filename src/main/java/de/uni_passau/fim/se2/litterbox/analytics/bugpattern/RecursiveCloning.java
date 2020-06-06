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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.CloneOfMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addComment;

/**
 * Script starting with a When I start as a clone event handler that contain a create clone of
 * myself block may result in an infinite recursion.
 */
public class RecursiveCloning implements ScratchVisitor, IssueFinder {
    public static final String NAME = "recursive_cloning";
    public static final String SHORT_NAME = "recClone";
    public static final String HINT_TEXT = "recursive cloning";
    private static final String NOTE1 = "There are no recursive cloning calls in your project.";
    private static final String NOTE2 = "Some of the sprites contain recursive cloning calls.";
    private boolean found = false;
    private boolean startAsClone = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        startAsClone = false;
        count = 0;
        actorNames = new LinkedList<>();
        program.accept(this);
        String notes = NOTE1;
        if (count > 0) {
            notes = NOTE2;
        }
        return new IssueReport(NAME, count, actorNames, notes);
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

        if (found) {
            found = false;
            actorNames.add(currentActor.getIdent().getName());
        }
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof StartedAsClone) {
            startAsClone = true;
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
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
                    count++;
                    found = true;
                    CloneOfMetadata metadata = (CloneOfMetadata) node.getMetadata();
                    addComment((NonDataBlockMetadata) metadata.getCloneBlockMetadata(), currentActor, HINT_TEXT,
                            SHORT_NAME + count);
                }
            }
        }
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
    }
}
