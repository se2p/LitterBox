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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Join;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.LetterOf;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class OperatorsBlockCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "operators_block_count";

    /**
     * Option to calculate the proportion of operator blocks in a program rather than their absolute count.
     * The name "average" comes from readability research (e.g. [1]) and was retained to comply with existing literature.
     * [1] Scalabrino et al.: A comprehensive model for code readability,
     * <a href="https://doi.org/10.1002/smr.1958">DOI: 10.1002/smr.1958</a>)
     */
    private final boolean average;
    private int count = 0;

    public OperatorsBlockCount() {
        this.average = false;
    }

    public OperatorsBlockCount(boolean average) {
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
    public void visit(Add node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Minus node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Mult node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Div node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(PickRandom node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(LessThan node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Equals node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(BiggerThan node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(And node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Or node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Not node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Join node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(LetterOf node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(LengthOfString node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(StringContains node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Mod node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Round node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(NumFunctOf node) {
        count++;
        visitChildren(node);
    }

    @Override
    public String getName() {
        if (average) {
            return "average_" + NAME;
        }
        return NAME;
    }
}
