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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.metric;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.event.MBlockEvent;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.bool.MBlockBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.MBlockNumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.string.MBlockStringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.MBlockStmt;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class MBlockCount<T extends ASTNode> extends AbstractRobotMetric<T> {
    public static final String NAME = "m_block_count";
    private int count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(MBlockEvent node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(MBlockStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(MBlockStringExpr node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(MBlockNumExpr node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(MBlockBoolExpr node) {
        count++;
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
