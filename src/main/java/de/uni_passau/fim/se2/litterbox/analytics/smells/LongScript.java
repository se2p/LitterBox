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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

/**
 * Checks for scripts with more than 12 blocks.
 */
public class LongScript extends AbstractIssueFinder {

    public static final String NAME = "long_script";
    public static final String HINT_TEXT = "long_script_hint";
    private static final int NUMBER_TOO_LONG = 12;
    private int localCount = 0;

    @Override
    public void visit(Script node) {
        currentScript = node;
        localCount = 0;
        if (!(node.getEvent() instanceof Never)) {
            localCount++;
        }
        visitChildren(node);
        if (localCount > NUMBER_TOO_LONG) {
            issues.add(new Issue(this, currentActor, node));
        }
        currentScript = null;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        localCount = 0;

        //this is for counting the definition block itself
        localCount++;
        visitChildren(node);
        if (localCount > NUMBER_TOO_LONG) {
            issues.add(new Issue(this, currentActor, node));
        }
        currentProcedure = null;
    }

    @Override
    public void visit(StmtList node) {
        localCount = localCount + node.getStmts().size();
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }
}
