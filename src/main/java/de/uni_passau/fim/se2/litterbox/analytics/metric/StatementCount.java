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
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class StatementCount implements MetricExtractor, ScratchVisitor {

    public static final String NAME = "statement_count";
    private boolean insideScript = false;
    private boolean insideProcedure = false;

    private int count = 0;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        insideProcedure = false;
        insideScript = false;
        program.accept(this);
        return count;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        insideScript = false;
        count++;
        visitChildren(node);
        insideProcedure = false;
    }

    @Override
    public void visit(Script node) {
        insideScript = true;
        insideProcedure = false;
        if (!(node.getEvent() instanceof Never)) {
            count++;
        }
        visitChildren(node);
        insideScript = false;
    }

    @Override
    public void visit(Stmt node) {
        if (! (insideProcedure || insideScript)) {
            return;
        }
        count++;
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
