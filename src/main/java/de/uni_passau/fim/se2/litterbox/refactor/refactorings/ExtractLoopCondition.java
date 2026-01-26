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
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Not;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.export.scratchblocks.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtractLoopCondition extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "extract_loop_condition";

    private final RepeatForeverStmt foreverLoop;
    private final Stmt conditionalStmt;
    private final List<Stmt> replacement = new ArrayList<>();

    public ExtractLoopCondition(RepeatForeverStmt foreverLoop, Stmt conditionalStmt) {
        this.foreverLoop = Preconditions.checkNotNull(foreverLoop);
        this.conditionalStmt = Preconditions.checkNotNull(conditionalStmt);

        BoolExpr condition;
        StmtList terminatingStmts;
        List<Stmt> loopBodyStmts = new ArrayList<>();

        if (conditionalStmt instanceof IfThenStmt ifThenStmt) {
            condition = ifThenStmt.getBoolExpr();
            terminatingStmts = ifThenStmt.getThenStmts();
        } else if (conditionalStmt instanceof IfElseStmt ifElseStmt) {
            condition = ifElseStmt.getBoolExpr();
            StmtList thenStmts = ifElseStmt.getThenStmts();
            StmtList elseStmts = ifElseStmt.getElseStmts();

            boolean thenTerminates = thenStmts.getNumberOfStatements() >= 1
                    && thenStmts.getStatement(thenStmts.getNumberOfStatements() - 1) instanceof TerminationStmt;

            if (thenTerminates) {
                terminatingStmts = thenStmts;
                loopBodyStmts.addAll(elseStmts.getStmts());
            } else {
                // Else terminates
                condition = new Not(condition, condition.getMetadata());
                terminatingStmts = elseStmts;
                loopBodyStmts.addAll(thenStmts.getStmts());
            }
        } else {
            throw new IllegalArgumentException("Conditional statement must be IfThenStmt or IfElseStmt");
        }

        int numStatements = terminatingStmts.getNumberOfStatements();
        TerminationStmt stopStmt = (TerminationStmt) terminatingStmts.getStatement(numStatements - 1);
        List<Stmt> precedingStmts = terminatingStmts.getStmts().subList(0, numStatements - 1);

        List<Stmt> remainingStmts = new ArrayList<>();
        for (Stmt stmt : foreverLoop.getStmtList().getStmts()) {
            if (stmt == conditionalStmt) {
                remainingStmts.addAll(loopBodyStmts);
            } else {
                remainingStmts.add(stmt);
            }
        }

        UntilStmt replacementLoop = new UntilStmt(
                apply(condition),
                new StmtList(applyList(remainingStmts)),
                apply(foreverLoop.getMetadata())
        );
        replacement.add(replacementLoop);
        replacement.addAll(applyList(precedingStmts));
        if (!(stopStmt instanceof StopThisScript)) {
            replacement.add(apply(stopStmt));
        }
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(new StatementReplacementVisitor(foreverLoop, replacement));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return String.format("""
                %s
                Replaced loop:
                %s
                with until loop:
                %s
                """,
                NAME,
                ScratchBlocksVisitor.of(foreverLoop),
                ScratchBlocksVisitor.of(replacement.getFirst())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExtractLoopCondition that = (ExtractLoopCondition) o;
        return Objects.equals(foreverLoop, that.foreverLoop)
                && Objects.equals(conditionalStmt, that.conditionalStmt)
                && Objects.equals(replacement, that.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreverLoop, conditionalStmt, replacement);
    }
}
