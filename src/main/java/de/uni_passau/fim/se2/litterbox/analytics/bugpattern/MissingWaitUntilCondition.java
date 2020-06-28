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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Checks for missing statements in repeat-until blocks.
 */
public class MissingWaitUntilCondition implements IssueFinder {

    public static final String NAME = "missing_wait_condition";
    public static final String SHORT_NAME = "mssWaitCond";
    public static final String HINT_TEXT = "missing wait condition";


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
        private ActorDefinition currentActor;
        private int count = 0;
        private Set<Issue> issues = new LinkedHashSet<>();
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
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }

        @Override
        public void visit(WaitUntil node) {
            if (node.getUntil() instanceof UnspecifiedBoolExpr) {
                count++;
                issues.add(new Issue(issueFinder, currentActor, node));
                addBlockComment((NonDataBlockMetadata) node.getMetadata(), currentActor, HINT_TEXT,
                        SHORT_NAME + count);
            }
        }

        @Override
        public void visit(Script node) {
            if (!(node.getEvent() instanceof Never)) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }
    }
}
