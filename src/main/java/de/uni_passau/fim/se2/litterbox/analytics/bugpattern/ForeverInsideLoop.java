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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueBuilder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * If two loops are nested and the inner loop is a forever loop, the inner loop will never terminate. Thus
 * the statements preceding the inner loop are only executed once. Furthermore, the statements following the outer
 * loop can never be reached.
 */
public class ForeverInsideLoop extends AbstractIssueFinder {
    public static final String NAME = "forever_inside_loop";
    private boolean blocksAfter;
    private boolean blocksBefore;
    private boolean insideIfElse;
    private Stack<LoopStmt> loopStack = new Stack<>();

    @Override
    public void visit(ActorDefinition actor) {
        blocksAfter = false;
        blocksBefore = false;
        loopStack.clear();
        super.visit(actor);
    }

    @Override
    public void visit(UntilStmt node) {
        checkPosition(node);
        loopStack.push(node);
        visitChildren(node);
        loopStack.pop();
    }

    private void checkPosition(ASTNode node) {
        ASTNode parent = node.getParentNode();
        assert parent instanceof StmtList;
        List<Stmt> list = ((StmtList) parent).getStmts();
        for (int i = 0; i < list.size(); i++) {
            if (node == list.get(i)) {
                //blocks before the first loop should not be counted because they would never be repeated and have already been executed
                if (!loopStack.isEmpty() && i > 0) {
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
        checkPosition(node);
        if (!loopStack.isEmpty() && (blocksAfter || blocksBefore || insideIfElse)) {

            IssueBuilder builder = prepareIssueBuilder(node)
                    .withSeverity(IssueSeverity.HIGH)
                    .withHint(getName())
                    .withRefactoring(getRefactoring(node, loopStack.peek()));
            addIssue(builder);
        }
        loopStack.push(node);
        visitChildren(node);
        loopStack.pop();
    }

    private ScriptEntity getRefactoring(RepeatForeverStmt loop, LoopStmt outerLoop) {
        // W
        // Loop 1:
        //   X
        //   Forever loop:
        //     Y
        // Z
        //
        // Is replaced with:
        // W
        // X
        // Forever loop:
        //   Y

        StmtList outerList = (StmtList) outerLoop.getParentNode();


        List<Stmt> outerStatements = new ArrayList<>(outerList.getStmts());
        int pos = outerStatements.indexOf(outerLoop); // Location of Loop 1
        outerStatements = outerStatements.subList(0, pos); // Delete anything following Loop 1
        outerStatements.addAll(outerLoop.getStmtList().getStmts()); // Add X and Forever loop

        NodeReplacementVisitor visitor = new NodeReplacementVisitor(outerList, new StmtList(outerStatements));

        return visitor.apply(getCurrentScriptEntity());
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        loopStack.push(node);
        checkPosition(node);
        visitChildren(node);
        loopStack.pop();
    }

    @Override
    public void visit(IfThenStmt node) {
        if (!loopStack.isEmpty()) {
            checkPosition(node);
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        insideIfElse = true;
        if (!loopStack.isEmpty()) {
            checkPosition(node);
        }
        visitChildren(node);
        insideIfElse = false;
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
