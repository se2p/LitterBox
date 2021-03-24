/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class InsideControlVariablesStmtCount implements MetricExtractor<Program>, ScratchVisitor {
    public static final String NAME = "inside_control_variables_stmt_count";

    private int count = 0;
    private boolean insideScript = false;

    @Override
    public double calculateMetric(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        program.accept(this);
        return count;
    }

    @Override
    public void visit(Script node) {
        insideScript = true;
        visitChildren(node);
        insideScript = false;
    }

    @Override
    public void visit(SetVariableTo node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (insideScript && !(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public void visit(ChangeVariableBy node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public void visit(ShowVariable node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public void visit(HideVariable node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public void visit(AddTo node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public void visit(DeleteAllOf node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public void visit(DeleteOf node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public void visit(InsertAt node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public void visit(ReplaceItem node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public void visit(ShowList node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public void visit(HideList node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (!(node.getParentNode().getParentNode() instanceof Script) && !(node.getParentNode().getParentNode() instanceof ProcedureDefinition)) {
            count++;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
