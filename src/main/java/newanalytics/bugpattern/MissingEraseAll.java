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
package newanalytics.bugpattern;

import java.util.LinkedList;
import java.util.List;
import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.statement.pen.PenClearStmt;
import scratch.ast.model.statement.pen.PenDownStmt;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

public class MissingEraseAll implements IssueFinder {

    public static final String NAME = "missing_erase_all";
    public static final String SHORT_NAME = "msserase";

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);

        int count = 0;
        List<String> actors = new LinkedList<>();

        final List<ActorDefinition> defintions = program.getActorDefinitionList().getDefintions();
        for (ActorDefinition defintion : defintions) {
            // Should we also check procedure definitions? Maybe they have a pen up?
            final List<Script> scriptList = defintion.getScripts().getScriptList();
            CheckVisitor visitor = new CheckVisitor();
            for (Script script : scriptList) {
                script.getStmtList().getStmts().accept(visitor);
                if (visitor.getResult()) {
                    count++;
                    actors.add(defintion.getIdent().getName());
                    break;
                }
            }
        }

        return new IssueReport(NAME, count, actors, "");
    }

    @Override
    public String getName() {
        return NAME;
    }

    private class CheckVisitor implements ScratchVisitor {

        private boolean penClearSet = false;
        private boolean penDownSet = false;

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

        public void reset() {
            penClearSet = false;
            penDownSet = false;
        }

        public boolean getResult() {
            return penDownSet && !penClearSet;
        }
    }
}
