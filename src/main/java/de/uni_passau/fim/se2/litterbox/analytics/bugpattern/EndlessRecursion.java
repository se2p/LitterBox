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

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

/**
 * If a custom block calls itself inside its body and has no condition to stop the recursion, it will run for an
 * indefinite amount of time.
 */
public class EndlessRecursion extends AbstractIssueFinder {
    public static final String NAME = "endless_recursion";
    public static final String SHORT_NAME = "endlRec";
    public static final String HINT_TEXT = "endless recursion";
    private String currentProcedureName;
    private boolean insideProcedure;
    private int loopIfCounter;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        loopIfCounter = 0;
        super.visit(actor);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentProcedureName = procMap.get(node.getIdent()).getName();
        visitChildren(node);
        insideProcedure = false;
    }

    @Override
    public void visit(CallStmt node) {
        if (insideProcedure && loopIfCounter == 0) {
            String call = node.getIdent().getName();
            if (call.equals(currentProcedureName)) {
                issues.add(new Issue(this, currentActor, node,
                        HINT_TEXT, node.getMetadata()));
            }
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        loopIfCounter++;
        visitChildren(node);
        loopIfCounter--;
    }

    @Override
    public void visit(IfThenStmt node) {
        loopIfCounter++;
        visitChildren(node);
        loopIfCounter--;
    }
}
