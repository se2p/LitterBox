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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.MissingAsk;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.List;

/**
 * Injects MissingAsk bug by replacing AskAndWait with Say.
 *
 * <p>
 * The detector finds programs where Answer is used without any AskAndWait.
 * This injector replaces AskAndWait with Say (same question text), leaving
 * any Answer usages orphaned.
 *
 * <p>
 * Only injects if:
 * - Answer is actually used in the program
 * - There is exactly one AskAndWait (otherwise removing one would still leave others)
 */
public class MissingAskInjector extends AbstractBugInjector {

    private boolean canInject;

    private final IssueFinder issueFinder = new MissingAsk();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        canInject = checkCanInject(program);
        return super.inject(program);
    }

    /**
     * Checks if injection is possible: Answer must be used and there must be
     * exactly one AskAndWait block. If there are multiple AskAndWait blocks,
     * replacing one would still leave others, so the detector wouldn't find a bug.
     */
    private boolean checkCanInject(Program program) {
        class UsageChecker implements ScratchVisitor {
            boolean answerUsed = false;
            int askCount = 0;

            @Override
            public void visit(Answer node) {
                answerUsed = true;
            }

            @Override
            public void visit(AskAndWait node) {
                askCount++;
                // Continue visiting children
                ScratchVisitor.super.visit(node);
            }
        }

        UsageChecker checker = new UsageChecker();
        program.accept(checker);
        return checker.answerUsed && checker.askCount == 1;
    }

    @Override
    public void visit(AskAndWait node) {
        if (!canInject) {
            return;
        }

        modifyClonedNode(node, clonedAsk -> {
            ASTNode parent = clonedAsk.getParentNode();
            if (parent instanceof StmtList stmtList) {
                List<Stmt> stmts = stmtList.getStmts();
                int index = stmts.indexOf(clonedAsk);
                if (index >= 0) {
                    Say say = new Say(clonedAsk.getQuestion(), clonedAsk.getMetadata());
                    stmts.set(index, say);
                    say.setParentNode(stmtList);
                }
            }
        });
        super.visit(node);
    }
}
