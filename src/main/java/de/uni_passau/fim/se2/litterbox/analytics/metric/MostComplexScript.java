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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/**
 * Calculates the complexity of the most complex script based on the weighted method count.
 */
public class MostComplexScript<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "most_complex_script";
    int count = 0;
    int localCount = 0;

    @Override
    public MetricResult calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        this.count = 0;
        node.accept(this);
        return new MetricResult(NAME, count);
    }

    @Override
    public void visit(Script node) {
        localCount = 0;

        localCount++;
        visitChildren(node);
        if (localCount > count) {
            count = localCount;
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        localCount = 0;

        localCount++;
        visitChildren(node);
        if (localCount > count) {
            count = localCount;
        }
    }

    @Override
    public void visit(IfElseStmt node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        localCount++;
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
