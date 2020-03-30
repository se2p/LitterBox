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
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Checks for missing statements in repeat-until blocks.
 */
public class MissingWaitUntilCondition implements IssueFinder {

    public static final String NAME = "missing_wait_condition";
    public static final String SHORT_NAME = "mssWaitCond";

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        CheckVisitor visitor = new CheckVisitor();

        program.accept(visitor);
        String notes = "All 'wait until' blocks terminating correctly.";
        if (visitor.count > 0) {
            notes = "Some 'wait until' blocks have no condition.";
        }

        return new IssueReport(NAME, visitor.count, visitor.actorNames, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static class CheckVisitor implements ScratchVisitor {

        private int count = 0;
        private boolean patternFound = false;
        private List<String> actorNames = new LinkedList<>();

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
            patternFound = false;
            if (!actor.getChildren().isEmpty()) {
                for (ASTNode child : actor.getChildren()) {
                    child.accept(this);
                }
            }

            if (patternFound) {
                actorNames.add(actor.getIdent().getName());
            }
        }

        @Override
        public void visit(WaitUntil node) {
            if (node.getUntil() instanceof UnspecifiedBoolExpr) {
                patternFound = true;
                count++;
            }
        }

        @Override
        public void visit(Script node) {
            if (!(node.getEvent() instanceof Never)) {
                if (!node.getChildren().isEmpty()) {
                    for (ASTNode child : node.getChildren()) {
                        child.accept(this);
                    }
                }
            }
        }
    }
}
