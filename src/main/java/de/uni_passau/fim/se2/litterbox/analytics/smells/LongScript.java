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

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.ProcedureMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;

/**
 * Checks for scripts with more than 12 blocks.
 */
public class LongScript extends TopBlockFinder {

    public static final String NAME = "long_script";
    private static final int NUMBER_TOO_LONG = 12;
    private int localCount = 0;

    @Override
    public void visit(Script node) {
        currentScript = node;
        localCount = 0;
        setHint = false;
        if (!(node.getEvent() instanceof Never)) {
            localCount++;
        }
        visitChildren(node);
        if (localCount > NUMBER_TOO_LONG) {
            setHint = true;
            if (!(node.getEvent() instanceof Never)) {
                node.getEvent().accept(this);
            } else {
                node.getStmtList().accept(this);
            }
        }
        setHint = false;
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
            addIssue(node, ((ProcedureMetadata) node.getMetadata()).getDefinition());
        }
        currentProcedure = null;
    }

    @Override
    public void visit(StmtList node) {
        if (!setHint) {
            localCount = localCount + node.getStmts().size();
            visitChildren(node);
        } else {
            node.getStmts().get(0).accept(this);
        }
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
