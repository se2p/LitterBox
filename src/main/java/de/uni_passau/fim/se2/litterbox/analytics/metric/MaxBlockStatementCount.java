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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class MaxBlockStatementCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {

    public static final String NAME = "max_block_statement_count";

    private double maxBlocks;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        maxBlocks = 0;
        node.accept(this);
        return maxBlocks;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        visitChildren(node);
    }

    @Override
    public void visit(Script node) {
        visitChildren(node);
    }

    public boolean isLoopOrBranch(ASTNode node) {
        return node instanceof ControlStmt;
    }

    @Override
    public void visit(Event node) {
        if (!(node instanceof Never)) {
            this.getBlockCount(node, false);
        }
    }

    public void visitControlStmts(ControlStmt controlStmt) {
        if (controlStmt instanceof IfThenStmt ifThenStmt) {
            this.getBlockCount(ifThenStmt.getBoolExpr(), true);
            for (Stmt stmt : ifThenStmt.getThenStmts().getStmts()) {
                visit(stmt);
            }
        } else if (controlStmt instanceof RepeatTimesStmt repeatTimesStmt) {
            this.getBlockCount(repeatTimesStmt.getTimes(), true);
            for (Stmt stmt : repeatTimesStmt.getStmtList().getStmts()) {
                visit(stmt);
            }
        } else if (controlStmt instanceof UntilStmt untilStmt) {
            this.getBlockCount(untilStmt.getBoolExpr(), true);
            for (Stmt stmt : untilStmt.getStmtList().getStmts()) {
                visit(stmt);
            }
        } else if (controlStmt instanceof IfElseStmt ifElseStmt) {
            this.getBlockCount(ifElseStmt.getBoolExpr(), true);
            for (Stmt stmt : ifElseStmt.getThenStmts().getStmts()) {
                visit(stmt);
            }

            for (Stmt stmt : ifElseStmt.getElseStmts().getStmts()) {
                visit(stmt);
            }
        } else if (controlStmt instanceof RepeatForeverStmt repeatForeverStmt) {
            for (Stmt stmt : repeatForeverStmt.getStmtList().getStmts()) {
                visit(stmt);
            }
        }
    }

    public void getBlockCount(ASTNode node, boolean increment) {
        double currentNumberOfBlocks;
        if (increment) {
            currentNumberOfBlocks = new BlockCount<>().calculateMetric(node);
        } else {
            currentNumberOfBlocks = new BlockCount<>().calculateMetric(node) + 1;
        }

        this.maxBlocks = Math.max(this.maxBlocks, currentNumberOfBlocks);
    }

    @Override
    public void visit(Stmt node) {
        if (isLoopOrBranch(node)) {
            visitControlStmts((ControlStmt) node);
        } else {
            this.getBlockCount(node, false);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}

