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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.ControlStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class TopLevelControlStmtCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "top_level_control_stmt_count";

    private int count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public void visit(ControlStmt node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
            visitChildren(node);
        }
    }

    @Override
    public void visit(WaitUntil node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public void visit(WaitSeconds node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public void visit(TerminationStmt node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public void visit(CreateCloneOf node) {
        //The parent is a StmtList, so the parent of the StmtList has to be checked
        if (node.getParentNode().getParentNode() instanceof Script || node.getParentNode().getParentNode() instanceof ProcedureDefinition) {
            count++;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
