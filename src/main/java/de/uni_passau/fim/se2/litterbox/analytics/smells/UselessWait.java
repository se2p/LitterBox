/*
 * Copyright (C) 2019-2022 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;

/**
 * This finder detects the useless wait smell. A wait seconds block without any following blocks
 * (or blocks before ir in case it is inside a loop) is useless as the waiting does not contribute to the project.
 */
public class UselessWait extends AbstractIssueFinder {
    public static final String NAME = "useless_wait";
    private int loopCount = 0;
    private int ifCount = 0;

    @Override
    public void visit(WaitSeconds node) {
        StmtList stmtList = (StmtList) node.getParentNode();
        if (stmtList.getNumberOfStatements() == 1) {

            if (loopCount > 0 || ifCount > 0) {
                if (!hasOtherBlocks(stmtList.getParentNode(), stmtList)) {
                    addIssue(node, node.getMetadata());
                }
            } else {
                addIssue(node, node.getMetadata());
            }
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        loopCount++;
        visitChildren(node);
        loopCount--;
    }

    @Override
    public void visit(UntilStmt node) {
        loopCount++;
        visitChildren(node);
        loopCount--;
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        loopCount++;
        visitChildren(node);
        loopCount--;
    }

    @Override
    public void visit(IfElseStmt node) {
        ifCount++;
        visitChildren(node);
        ifCount--;
    }

    @Override
    public void visit(IfThenStmt node) {
        ifCount++;
        visitChildren(node);
        ifCount--;
    }

    private boolean hasOtherBlocks(ASTNode node, StmtList stmtList) {
        if (node instanceof ScriptEntity) {
            return false;
        } else if (node instanceof IfElseStmt ifElseStmt && hasOtherBlocks(ifElseStmt, stmtList)) {
            return true;
        }
        StmtList parentStmtList = (StmtList) node.getParentNode();
        boolean hasOtherStmts = checkStmtList(parentStmtList);
        if (hasOtherStmts) {
            return !canOtherStmtsBeIgnored(node, parentStmtList);
        } else {
            return hasOtherBlocks(parentStmtList.getParentNode(), parentStmtList);
        }
    }

    /**
     * It should be checked if either half of the if-else has something meaningful.
     *
     * @param node     IfElseStmt that should be checked
     * @param stmtList the stmtList that was already inspected
     * @return if the other stmtList of the IfElseStmt contains something meaningful
     */
    private boolean hasOtherBlocks(IfElseStmt node, StmtList stmtList) {
        if (stmtList != node.getThenStmts()) {
            return checkStmtListOfIfElse(node.getThenStmts());
        } else if (stmtList != node.getElseStmts()) {
            return checkStmtListOfIfElse(node.getElseStmts());
        } else {
            throw new IllegalStateException("An If-Else-block only has two stmtLists so one has to fit and you should never reach this point.");
        }
    }

    /**
     * Leading statements on top level should be ignored, as they have already been processed at runtime.
     *
     * @param node           the node that should be inspected if it is in last place in the ScriptEntity
     * @param parentStmtList the stmtList that contains the node
     * @return if the node is at last place in the ScriptEntity
     */
    private boolean canOtherStmtsBeIgnored(ASTNode node, StmtList parentStmtList) {
        if (parentStmtList.getParentNode() instanceof ScriptEntity) {
            ASTNode lastNode = parentStmtList.getStatement(parentStmtList.getNumberOfStatements() - 1);
            return lastNode == node;
        } else {
            return false;
        }
    }

    private boolean checkStmtListOfIfElse(StmtList stmtList) {
        if (stmtList.getNumberOfStatements() > 1) {
            return true;
        } else if (stmtList.getNumberOfStatements() == 0) {
            return false;
        } else {
            Stmt stmt = stmtList.getStatement(0);
            return !(stmt instanceof WaitSeconds || stmt instanceof WaitUntil);
        }
    }

    private boolean checkStmtList(StmtList stmtList) {
        return stmtList.getNumberOfStatements() > 1;
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
