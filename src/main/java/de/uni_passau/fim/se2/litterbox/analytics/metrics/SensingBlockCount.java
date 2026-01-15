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
package de.uni_passau.fim.se2.litterbox.analytics.metrics;

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

    /**
     * Option to calculate the proportion of sensing blocks in a program rather than their absolute count.
     * The name "average" comes from readability research (e.g. [1]) and was retained to comply with literature.
     *
     * <p>[1] Scalabrino et al.: A comprehensive model for code readability,
     * <a href="https://doi.org/10.1002/smr.1958">doi:10.1002/smr.1958</a>)
     */
    private final boolean average;
    private int count = 0;

    public SensingBlockCount() {
        this.average = false;
    }

    public SensingBlockCount(boolean average) {
        this.average = average;
    }

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        double totalCount = new BlockCount<T>().calculateMetric(node);
        if (totalCount == 0) {
            return 0.0;
        }
        count = 0;
        node.accept(this);
        return average ? count / totalCount : count;
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
        if (average) {
            return "average_" + NAME;
        }
        return NAME;
    }
}
