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
package analytics.bugpattern;

import analytics.IssueFinder;
import analytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.statement.pen.PenClearStmt;
import scratch.ast.model.statement.pen.PenDownStmt;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

public class MissingEraseAll implements IssueFinder {

    public static final String NAME = "missing_erase_all";
    public static final String SHORT_NAME = "mssEraseAll";

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

    private class CheckVisitor implements ScratchVisitor {

        private boolean penClearSet = false;
        private boolean penDownSet = false;
        private int count = 0;
        private List<String> actorNames = new LinkedList<>();
        private ActorDefinition currentActor;

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
        public void visit(PenClearStmt node) {
            penClearSet = true;
            if (!node.getChildren().isEmpty()) {
                for (ASTNode child : node.getChildren()) {
                    child.accept(this);
                }
            }
        }


        void reset() {
            penClearSet = false;
            penDownSet = false;
            currentActor = null;
        }

        public boolean getResult() {
            return penDownSet && !penClearSet;
        }
    }
}
