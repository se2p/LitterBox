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
package de.uni_passau.fim.se2.litterbox.ast.builder;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

import java.util.ArrayList;
import java.util.List;

public class IfStmtBuilder {

    private BoolExpr boolExpr;
    private StmtList thenStmts;
    private StmtList elseStmts;
    private BlockMetadata metadata;

    public IfStmtBuilder() {
        this.boolExpr = new UnspecifiedBoolExpr();
        this.thenStmts = new StmtList(new ArrayList<>());
        this.elseStmts = new StmtList(new ArrayList<>());
        this.metadata = new NoBlockMetadata();
    }

    public IfStmtBuilder withCondition(BoolExpr boolExpr) {
        this.boolExpr = boolExpr;
        return this;
    }

    public IfStmtBuilder withBody(StmtList thenStmts) {
        this.thenStmts = thenStmts;
        return this;
    }

    public IfStmtBuilder withBody(Stmt... stmts) {
        this.thenStmts = new StmtList(stmts);
        return this;
    }

    public IfStmtBuilder withBody(List<Stmt> stmts) {
        this.thenStmts = new StmtList(stmts);
        return this;
    }

    public IfStmtBuilder withElseBody(StmtList elseStmts) {
        this.elseStmts = elseStmts;
        return this;
    }

    public IfStmtBuilder withElseBody(Stmt... stmts) {
        this.elseStmts = new StmtList(stmts);
        return this;
    }

    public IfStmtBuilder withElseBody(List<Stmt> stmts) {
        this.elseStmts = new StmtList(stmts);
        return this;
    }

    public IfStmtBuilder withMetadata(BlockMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public IfStmt build() {
        if (elseStmts != null && !elseStmts.getStmts().isEmpty()) {
            IfElseStmt stmt = new IfElseStmt(boolExpr, thenStmts, elseStmts, metadata);
            boolExpr.setParentNode(stmt);
            thenStmts.setParentNode(stmt);
            elseStmts.setParentNode(stmt);
            return stmt;
        } else {
            // Default to IfThenStmt if no else part
            IfThenStmt stmt = new IfThenStmt(boolExpr, thenStmts, metadata);
            boolExpr.setParentNode(stmt);
            thenStmts.setParentNode(stmt);
            return stmt;
        }
    }
}
