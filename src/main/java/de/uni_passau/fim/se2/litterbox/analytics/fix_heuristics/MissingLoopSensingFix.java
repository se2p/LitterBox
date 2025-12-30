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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

import java.util.Stack;

/**
 * Fix heuristic for the MissingLoopSensing bug.
 *
 * <p>The bug is reported when sensing blocks are used outside a loop.
 * The bug is fixed if they're now inside a loop or after a wait/ask.
 */
public class MissingLoopSensingFix extends AbstractFixHeuristic {
    public static final String NAME = "missing_loop_sensing_fix";

    private Stack<LoopStmt> loopStack;
    private boolean afterWaitUntil;
    private boolean afterAskAndWait;

    public MissingLoopSensingFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
        loopStack = new Stack<>();
    }

    @Override
    public void visit(Script node) {
        // Reset flags for each script to ensure proper scoping
        loopStack = new Stack<>();
        afterWaitUntil = false;
        afterAskAndWait = false;
        super.visit(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        loopStack.push(node);
        super.visit(node);
        loopStack.pop();
    }

    @Override
    public void visit(UntilStmt node) {
        loopStack.push(node);
        super.visit(node);
        loopStack.pop();
    }

    @Override
    public void visit(WaitUntil node) {
        afterWaitUntil = true;
        super.visit(node);
    }

    @Override
    public void visit(AskAndWait node) {
        afterAskAndWait = true;
        super.visit(node);
    }

    private void check(ASTNode node) {
        if (matchesBugLocation(node)) {
            // Fix: Inside a loop OR after a wait/ask
            if (!loopStack.isEmpty() || afterWaitUntil || afterAskAndWait) {
                addIssue(node, node.getMetadata());
            }
        }
    }

    @Override
    public void visit(IsKeyPressed node) {
        check(node);
        super.visit(node);
    }

    @Override
    public void visit(Touching node) {
        check(node);
        super.visit(node);
    }

    @Override
    public void visit(IsMouseDown node) {
        check(node);
        super.visit(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        check(node);
        super.visit(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        check(node);
        super.visit(node);
    }

    @Override
    public void visit(DistanceTo node) {
        check(node);
        super.visit(node);
    }



    @Override
    public String getName() {
        return NAME;
    }
}
