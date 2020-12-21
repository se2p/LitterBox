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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

import java.util.List;

/**
 * If two loops are nested and the inner loop is a forever loop, the inner loop will never terminate. Thus
 * the statements preceeding the inner loop are only executed once. Furthermore, the statements following the outer
 * loop can never be reached.
 */
public class ForeverInsideLoop extends AbstractIssueFinder {
    public static final String NAME = "forever_inside_loop";
    private int loopcounter;
    private boolean blocksAfter;
    private boolean blocksBefore;

    @Override
    public void visit(ActorDefinition actor) {
        loopcounter = 0;
        blocksAfter = false;
        blocksBefore = false;
        super.visit(actor);
    }

    @Override
    public void visit(UntilStmt node) {
        loopcounter++;
        checkPosition(node);
        visitChildren(node);
        loopcounter--;
    }

    private void checkPosition(ASTNode node) {
        ASTNode parent = node.getParentNode();
        assert parent instanceof StmtList;
        List<Stmt> list = ((StmtList) parent).getStmts();
        for (int i = 0; i < list.size(); i++) {
            if (node == list.get(i)) {
                //blocks before the first loop should not be counted because they would never be repeated and have already been executed
                if (loopcounter == 1 && i > 0) {
                    blocksBefore = true;
                }
                if (i < list.size() - 1) {
                    blocksAfter = true;
                }
                return;
            }
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (loopcounter > 0 && (blocksAfter || blocksBefore)) {
            addIssue(node, node.getMetadata());
        }
        loopcounter++;
        checkPosition(node);
        visitChildren(node);
        loopcounter--;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        loopcounter++;
        checkPosition(node);
        visitChildren(node);
        loopcounter--;
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
