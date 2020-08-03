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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class ProgramUsingPen implements MetricExtractor, ScratchVisitor {
    public static final String NAME = "using_pen";
    private boolean found = false;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        found = false;
        program.accept(this);
        return found ? 1 : 0;
    }

    @Override
    public void visit(PenDownStmt node) {
        found = true;
    }

    @Override
    public void visit(PenUpStmt node) {
        found = true;
    }

    @Override
    public void visit(PenClearStmt node) {
        found = true;
    }

    @Override
    public void visit(SetPenSizeTo node) {
        found = true;
    }

    @Override
    public void visit(ChangePenSizeBy node) {
        found = true;
    }

    @Override
    public void visit(SetPenColorToColorStmt node) {
        found = true;
    }

    @Override
    public void visit(PenStampStmt node) {
        found = true;
    }

    @Override
    public void visit(ChangePenColorParamBy node) {
        found = true;
    }

    @Override
    public void visit(SetPenColorParamTo node) {
        found = true;
    }
}
