/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;

/**
 * Script starting with a When I start as a clone event handler that contain a create clone of
 * myself block may result in an infinite recursion.
 */
public class RecursiveCloning extends AbstractIssueFinder {
    public static final String NAME = "recursive_cloning";
    private boolean secondVisit;
    private boolean foundDelete;

    @Override
    public void visit(ProcedureDefinition node) {
        //NOP should not be detected in Procedure
    }

    @Override
    public void visit(Script node) {
        if (ignoreLooseBlocks && node.getEvent() instanceof Never) {
            // Ignore unconnected blocks
            return;
        }
        if (node.getEvent() instanceof StartedAsClone) {
            super.visit(node);
            /* the first visit is to make sure that the complete script is scanned for a delete clone
               in the second visit the create clone of blocks are collected in issues,
               but only if no delete was detected prior
             */
            if (!foundDelete) {
                secondVisit = true;
                super.visit(node);
                secondVisit = false;
            }
            foundDelete = false;
        }
    }

    @Override
    public void visit(CreateCloneOf node) {
        if (secondVisit) {
            if (node.getStringExpr() instanceof AsString asString && asString.getOperand1() instanceof StrId) {
                final String spriteName = ((StrId) ((AsString) node.getStringExpr()).getOperand1()).getName();

                if (spriteName.equals("_myself_") && !foundDelete) {
                    addIssue(node, node.getMetadata(), IssueSeverity.LOW);
                }
            }
        }
        visitChildren(node);
    }

    @Override
    public void visit(DeleteClone node) {
        foundDelete = true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
