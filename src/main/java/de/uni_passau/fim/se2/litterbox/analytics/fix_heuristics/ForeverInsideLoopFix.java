/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.fix_heuristics;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

public class ForeverInsideLoopFix extends AbstractIssueFinder {
    public static final String NAME = "forever_inside_loop_fix";
    private final String spriteLocation;
    private boolean alreadyFound = false;
    private boolean insideScript = false;

    public ForeverInsideLoopFix(String spriteLocation) {
        this.spriteLocation = spriteLocation;
    }

    @Override
    public void visit(ActorDefinition actor) {
        if (actor.getIdent().getName().equals(spriteLocation)) {
            super.visit(actor);
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideScript = true;
        super.visit(node);
        insideScript = false;
    }

    @Override
    public void visit(Script node) {
        if (!(node.getEvent() instanceof Never)) {
            insideScript = true;
        }
        super.visit(node);
        insideScript = false;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (insideScript && !alreadyFound) {
            alreadyFound = true;
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(UntilStmt node) {
        if (insideScript && !alreadyFound) {
            alreadyFound = true;
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        if (insideScript && !alreadyFound) {
            alreadyFound = true;
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.FIX;
    }
}
