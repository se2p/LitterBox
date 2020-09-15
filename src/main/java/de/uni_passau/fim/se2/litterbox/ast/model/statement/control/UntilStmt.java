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
package de.uni_passau.fim.se2.litterbox.ast.model.statement.control;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class UntilStmt extends AbstractNode implements ControlStmt {

    private final BoolExpr boolExpr;
    private final StmtList stmtList;
    private final BlockMetadata metadata;

    public UntilStmt(BoolExpr boolExpr, StmtList stmtList, BlockMetadata metadata) {
        super(boolExpr, stmtList, metadata);
        this.boolExpr = Preconditions.checkNotNull(boolExpr);
        this.stmtList = Preconditions.checkNotNull(stmtList);
        this.metadata = metadata;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    public BoolExpr getBoolExpr() {
        return boolExpr;
    }

    public StmtList getStmtList() {
        return stmtList;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
