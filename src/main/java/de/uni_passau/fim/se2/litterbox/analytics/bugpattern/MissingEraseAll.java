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
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenClearStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * If a sprite uses a pen down block but never an erase all block, then all drawings from a
 * previous execution might remain, making it impossible to get a blank background without reloading the scratch
 * project.
 */
public class MissingEraseAll implements IssueFinder {

    public static final String NAME = "missing_erase_all";
    public static final String SHORT_NAME = "mssEraseAll";
    public static final String HINT_TEXT = "missing erase all";

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        CheckVisitor visitor = new CheckVisitor(this);
        program.accept(visitor);
        return visitor.getIssues();
        // return new IssueReport(NAME, visitor.count, visitor.actorNames, "");
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static class CheckVisitor implements ScratchVisitor {

        private boolean penClearSet = false;
        private boolean penDownSet = false;
        private boolean addComment = false;
        private int count = 0;
        private List<String> actorNames = new LinkedList<>();
        private ActorDefinition currentActor;
        private Set<Issue> issues = new LinkedHashSet<>();
        private MissingEraseAll issueFinder;

        public CheckVisitor(MissingEraseAll issueFinder) {
            this.issueFinder = issueFinder;
        }

        public Set<Issue> getIssues() {
            return issues;
        }

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
            addComment = false;
            penClearSet = false;
            penDownSet = false;
            if (!actor.getChildren().isEmpty()) {
                for (ASTNode child : actor.getChildren()) {
                    child.accept(this);
                }
            }

            if (getResult()) {
                count++;
                issues.add(new Issue(issueFinder, currentActor, actor));
                actorNames.add(currentActor.getIdent().getName());
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
                if (!node.getChildren().isEmpty()) {
                    for (ASTNode child : node.getChildren()) {
                        child.accept(this);
                    }
                }
            } else if (getResult()) {
                addBlockComment((NonDataBlockMetadata) node.getMetadata(), currentActor, HINT_TEXT,
                        SHORT_NAME + count);
            }
        }

        @Override
        public void visit(PenClearStmt node) {
            if (!addComment) {
                penClearSet = true;
                if (!node.getChildren().isEmpty()) {
                    for (ASTNode child : node.getChildren()) {
                        child.accept(this);
                    }
                }
            }
        }

        void reset() {
            penClearSet = false;
            penDownSet = false;
            currentActor = null;
            addComment = false;
        }

        public boolean getResult() {
            return penDownSet && !penClearSet;
        }
    }
}
