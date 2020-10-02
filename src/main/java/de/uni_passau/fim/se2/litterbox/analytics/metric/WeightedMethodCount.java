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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

/**
 * This visitor calculates the weighted method count as a metric for Scratch projects.
 */
public class WeightedMethodCount implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "weighted_method_count";
    int count = 0;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        this.count = 0;

        program.accept(this);
        return count;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ProcedureDefinition procedure) {
        count++;
        visitChildren(procedure);
    }

    @Override
    public void visit(Script node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(IfElseStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        count++;
        visitChildren(node);
    }
}
