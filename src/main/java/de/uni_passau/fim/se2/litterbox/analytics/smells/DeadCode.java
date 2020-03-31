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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueReport;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Checks if the project has loose blocks without a head.
 */
public class DeadCode implements IssueFinder, ScratchVisitor {

    public static final String NAME = "dead_code";
    public static final String SHORT_NAME = "dcode";
    private static final String NOTE1 = "There are no loose blocks in your project.";
    private static final String NOTE2 = "Some of the Sprites have loose blocks!";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();

    public DeadCode() {
    }

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
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
    public void visit(ActorDefinition actor) {
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }
        if (found) {
            found = false;
            actorNames.add(actor.getIdent().getName());
        }
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof Never && node.getStmtList().getStmts().getListOfStmt().size() > 0) {
            count++;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}