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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Size;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Backdrop;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Costume;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SpriteLookStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class LooksBlockCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "looks_block_count";

    private int count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(SpriteLookStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(ActorLookStmt node) {
        //these nodes are an ActorLookStmt but not from Look category in Scratch
        if (
                node instanceof AskAndWait || node instanceof HideList || node instanceof HideVariable
                        || node instanceof ShowList || node instanceof ShowVariable
        ) {
            return;
        }
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Size node) {
        count++;
    }

    @Override
    public void visit(Backdrop node) {
        count++;
    }

    @Override
    public void visit(Costume node) {
        count++;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
