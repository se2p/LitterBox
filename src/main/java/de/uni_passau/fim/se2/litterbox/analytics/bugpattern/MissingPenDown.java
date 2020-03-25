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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Scripts of a sprite using a pen up block but never a pen down block fall in this category.
 * We assume that this is a bug, because either the sprite is supposed to draw
 * something and does not, or later additions of pen down blocks may not lead to the desired results since remaining
 * pen up blocks could disrupt the project.
 */
public class MissingPenDown implements IssueFinder {

    public static final String NAME = "missing_pen_down";
    public static final String SHORT_NAME = "mssPenDown";

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        CheckVisitor visitor = new CheckVisitor();
        program.accept(visitor);
        return new IssueReport(NAME, visitor.count, visitor.actorNames, "");
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static class CheckVisitor implements ScratchVisitor {
        private int count = 0;
        private List<String> actorNames = new LinkedList<>();
        private ActorDefinition currentActor;
        private boolean penUpSet = false;
        private boolean penDownSet = false;

        @Override
        public void visit(ASTNode node) {
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }

        @Override
        public void visit(ActorDefinition actor) {
            currentActor = actor;
            penUpSet = false;
            penDownSet = false;
            if (!actor.getChildren().isEmpty()) {
                for (ASTNode child : actor.getChildren()) {
                    child.accept(this);
                }
            }

            if (getResult()) {
                count++;
                actorNames.add(currentActor.getIdent().getName());
                reset();
            }
        }

        @Override
        public void visit(PenDownStmt node) {
            penDownSet = true;
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }

        @Override
        public void visit(PenUpStmt node) {
            penUpSet = true;
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }

        void reset() {
            penUpSet = false;
            penDownSet = false;
            currentActor = null;
        }

        boolean getResult() {
            return !penDownSet && penUpSet;
        }
    }
}
