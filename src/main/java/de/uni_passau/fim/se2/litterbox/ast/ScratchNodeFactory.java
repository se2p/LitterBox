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
package de.uni_passau.fim.se2.litterbox.ast;

import de.uni_passau.fim.se2.litterbox.ast.builder.IfStmtBuilder;
import de.uni_passau.fim.se2.litterbox.ast.builder.UntilStmtBuilder;
import de.uni_passau.fim.se2.litterbox.ast.builder.WaitUntilBuilder;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;

import java.util.List;

public class ScratchNodeFactory {

    public ScratchNodeFactory() {
    }

    // --- Control Flow ---

    public IfStmtBuilder createIf() {
        return new IfStmtBuilder();
    }

    public UntilStmtBuilder createUntil() {
        return new UntilStmtBuilder();
    }

    public WaitUntilBuilder createWaitUntil() {
        return new WaitUntilBuilder();
    }

    public RepeatForeverStmt createForever(StmtList stmtList) {
        return new RepeatForeverStmt(
                stmtList,
                NonDataBlockMetadata.emptyNonBlockMetadata()
        );
    }

    // --- Expressions ---

    public NumberLiteral literal(double val) {
        return new NumberLiteral(val);
    }

    public WaitSeconds createWaitSeconds(double seconds) {
        return new WaitSeconds(
                literal(seconds),
                NonDataBlockMetadata.emptyNonBlockMetadata()
        );
    }

    public Equals equals(ComparableExpr left, ComparableExpr right) {
        return new Equals(left, right, new NoBlockMetadata());
    }

    /**
     * Creates an UnspecifiedBoolExpr, often used as a placeholder.
     */
    public UnspecifiedBoolExpr unspecifiedBool() {
        return new UnspecifiedBoolExpr();
    }

    // --- Statement Lists ---

    public StmtList createStmtList(Stmt... stmts) {
        return new StmtList(stmts);
    }

    public StmtList createStmtList(List<Stmt> stmts) {
        return new StmtList(stmts);
    }
}
