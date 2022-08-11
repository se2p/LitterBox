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
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class VariablesBlockCountOnlyDirectVariable<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "variables_block_count_only_direct_variable";

    private int count = 0;
    private boolean insideScript = false;
    private boolean insideProcedure = false;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(Script node) {
        insideScript = true;
        visitChildren(node);
        insideScript = false;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        visitChildren(node);
        insideProcedure = false;
    }

    @Override
    public void visit(SetVariableTo node) {
        if (insideScript || insideProcedure) {
            node.getExpr().accept(this);
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ShowVariable node) {
        //NOP
    }

    @Override
    public void visit(HideVariable node) {
        //NOP
    }

    @Override
    public void visit(Variable node) {
        if (insideScript || insideProcedure) {
            count++;
        }
    }

    @Override
    public void visit(AttributeOf node) {
        node.getElementChoice().accept(this);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

