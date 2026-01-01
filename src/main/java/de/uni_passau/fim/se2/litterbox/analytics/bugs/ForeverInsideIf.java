/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.LoopStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;

/**
 * If a forever loop is nested inside an if statement, the inner loop will never terminate. Thus
 * the statements following the if statement can never be reached.
 */
public class ForeverInsideIf extends AbstractIssueFinder {
    public static final String NAME = "forever_inside_if";
    private int ifFollowingCounter;
    private int ifNestingLevel;

    @Override
    public void visit(Script node) {
        ifFollowingCounter = 0;
        ifNestingLevel = 0;
        super.visit(node);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        ifFollowingCounter = 0;
        ifNestingLevel = 0;
        super.visit(node);
    }

    @Override
    public void visit(StmtList node) {
        boolean inLoop = node.getParentNode() instanceof LoopStmt;
        int listSize = node.getStmts().size();

        int incrementedBy = 0;
        for (int i = 0; i < listSize; i++) {
            Stmt s = node.getStmts().get(i);
            if (s instanceof IfStmt) {
                // If it's not the last one, it has a successor in this list
                if (i < listSize - 1) {
                    ifFollowingCounter++;
                    incrementedBy++;
                } else if (inLoop) {
                    // If it IS the last one, check if we are in a loop (implicit successor)
                    ifFollowingCounter++;
                    incrementedBy++;
                }
            }
        }

        visitChildren(node);

        if (incrementedBy > 0) {
            ifFollowingCounter -= incrementedBy;
        }
    }

    @Override
    public void visit(IfStmt node) {
        ifNestingLevel++;
        visitChildren(node);
        ifNestingLevel--;
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (ifNestingLevel > 0 && ifFollowingCounter > 0) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
