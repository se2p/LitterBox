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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.DistanceTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

/**
 * Fix heuristic for MissingLoopSensing that checks if the sensing block
 * is now used in a WaitUntil followed by termination.
 */
public class MissingLoopSensingWaitFix extends AbstractFixHeuristic {

    private final IssueFinder relatedBugFinder = new MissingLoopSensing();

    public MissingLoopSensingWaitFix(String bugLocationBlockId) {
        super(bugLocationBlockId);
    }

    @Override
    protected IssueFinder getRelatedBugFinder() {
        return relatedBugFinder;
    }

    private void checkIfFixed(ASTNode node) {
        if (matchesBugLocation(node)) {
            if (node.getParentNode() instanceof WaitUntil) {
                StmtList stmtList = AstNodeUtil.findParent(node, StmtList.class);
                if (stmtList != null && stmtList.getNumberOfStatements() > 0) {
                    ASTNode lastNode = stmtList.getStatement(stmtList.getNumberOfStatements() - 1);
                    ASTNode stmtListParent = stmtList.getParentNode();
                    if (lastNode instanceof StopAll || stmtListParent instanceof ScriptEntity) {
                        addIssue(node, node.getMetadata());
                    }
                }
            }
        } else {
            visitChildren(node);
        }
    }

    @Override
    public void visit(IsKeyPressed node) {
        checkIfFixed(node);
    }

    @Override
    public void visit(Touching node) {
        checkIfFixed(node);
    }

    @Override
    public void visit(IsMouseDown node) {
        checkIfFixed(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        checkIfFixed(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        checkIfFixed(node);
    }

    @Override
    public void visit(DistanceTo node) {
        checkIfFixed(node);
    }
}
