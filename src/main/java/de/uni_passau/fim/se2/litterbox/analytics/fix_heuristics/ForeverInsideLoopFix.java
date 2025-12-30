/*
 * Copyright (C) 2019-2025 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

/**
 * Fix heuristic for the ForeverInsideLoop bug.
 *
 * <p>The bug is reported when a RepeatForever/Until/RepeatTimes loop is nested
 * inside another loop, causing the outer loop to never iterate.
 *
 * <p>The bug is fixed if the loop identified by the block ID is no longer
 * nested inside another loop while still being inside a working script.
 */
public class ForeverInsideLoopFix extends AbstractFixHeuristic {
    public static final String NAME = "forever_inside_loop_fix";
    private boolean insideScript = false;
    private int loopDepth = 0;

    public ForeverInsideLoopFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
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
        checkLoopFix(node);
        loopDepth++;
        super.visit(node);
        loopDepth--;
    }

    @Override
    public void visit(UntilStmt node) {
        checkLoopFix(node);
        loopDepth++;
        super.visit(node);
        loopDepth--;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        checkLoopFix(node);
        loopDepth++;
        super.visit(node);
        loopDepth--;
    }

    /**
     * Check if this loop is the target and is now fixed.
     * Fixed means: still in a script but no longer nested inside another loop.
     */
    private void checkLoopFix(LoopStmt node) {
        if (matchesBugLocation(node) && insideScript && loopDepth == 0) {
            // The loop exists, is inside a working script, and is NOT nested
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
