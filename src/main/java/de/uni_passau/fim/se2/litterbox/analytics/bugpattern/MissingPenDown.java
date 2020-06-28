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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Scripts of a sprite using a pen up block but never a pen down block fall in this category.
 * We assume that this is a bug, because either the sprite is supposed to draw
 * something and does not, or later additions of pen down blocks may not lead to the desired results since remaining
 * pen up blocks could disrupt the project.
 */
public class MissingPenDown implements IssueFinder {

    public static final String NAME = "missing_pen_down";
    public static final String SHORT_NAME = "mssPenDown";
    public static final String HINT_TEXT = "missing pen down";

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        CheckVisitor visitor = new CheckVisitor(this);
        program.accept(visitor);
        return visitor.getIssues();
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static class CheckVisitor implements ScratchVisitor {
        private int count = 0;
        private Set<Issue> issues = new LinkedHashSet<>();
        private ActorDefinition currentActor;
        private boolean penUpSet = false;
        private boolean penDownSet = false;
        private boolean addComment;
        private IssueFinder issueFinder;

        public CheckVisitor(IssueFinder issueFinder) {
            this.issueFinder = issueFinder;
        }

        public Set<Issue> getIssues() {
            return issues;
        }

        @Override
        public void visit(ActorDefinition actor) {
            currentActor = actor;
            penUpSet = false;
            penDownSet = false;
            addComment = false;
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }

            if (getResult()) {
                count++;
                addComment = true;
                for (ASTNode child : actor.getChildren()) {
                    child.accept(this);
                }
                reset();
            }
        }

        @Override
        public void visit(PenDownStmt node) {
            if (!addComment) {
                penDownSet = true;
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }

        @Override
        public void visit(PenUpStmt node) {
            if (!addComment) {
                penUpSet = true;
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            } else if(getResult()){
                issues.add(new Issue(issueFinder, currentActor, node,
                        HINT_TEXT, SHORT_NAME + count, node.getMetadata()));
            }
        }

        void reset() {
            penUpSet = false;
            penDownSet = false;
            currentActor = null;
            addComment = false;
        }

        boolean getResult() {
            return !penDownSet && penUpSet;
        }
    }
}
