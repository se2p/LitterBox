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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Username;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.AskAndWait;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ResetTimer;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetDragMode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class SensingBlockCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "sensing_block_count";

    private int count = 0;

    @Override
    public MetricResult calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return new MetricResult(NAME, count);
    }

    @Override
    public void visit(Touching node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(DistanceTo node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(AskAndWait node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Answer node) {
        count++;
    }

    @Override
    public void visit(IsKeyPressed node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(MouseX node) {
        count++;
    }

    @Override
    public void visit(MouseY node) {
        count++;
    }

    @Override
    public void visit(IsMouseDown node) {
        count++;
    }

    @Override
    public void visit(SetDragMode node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Loudness node) {
        count++;
    }

    @Override
    public void visit(Timer node) {
        count++;
    }

    @Override
    public void visit(ResetTimer node) {
        count++;
    }

    @Override
    public void visit(AttributeOf node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Current node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(DaysSince2000 node) {
        count++;
    }

    @Override
    public void visit(Username node) {
        count++;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
