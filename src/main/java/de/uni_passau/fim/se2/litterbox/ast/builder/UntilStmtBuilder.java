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

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;

import java.util.ArrayList;
import java.util.List;

public class UntilStmtBuilder {

    private BoolExpr boolExpr;
    private StmtList stmtList;
    private BlockMetadata metadata;

    public UntilStmtBuilder() {
        this.boolExpr = new UnspecifiedBoolExpr();
        this.stmtList = new StmtList(new ArrayList<>());
        this.metadata = new NoBlockMetadata();
    }

    public UntilStmtBuilder withCondition(BoolExpr boolExpr) {
        this.boolExpr = boolExpr;
        return this;
    }

    public UntilStmtBuilder withBody(StmtList stmtList) {
        this.stmtList = stmtList;
        return this;
    }

    public UntilStmtBuilder withBody(Stmt... stmts) {
        this.stmtList = new StmtList(stmts);
        return this;
    }

    public UntilStmtBuilder withBody(List<Stmt> stmts) {
        this.stmtList = new StmtList(stmts);
        return this;
    }

    public UntilStmtBuilder withMetadata(BlockMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public UntilStmt build() {
        UntilStmt stmt = new UntilStmt(boolExpr, stmtList, metadata);
        if (boolExpr instanceof AbstractNode) {
            boolExpr.setParentNode(stmt);
        }
        if (stmtList != null) {
            stmtList.setParentNode(stmt);
        }
        return stmt;
    }
}
