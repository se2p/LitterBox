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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

import java.util.List;

/**
 * This checks for advanced coordination in the program. Advanced coordination can be achieved by a broadcast and the
 * When I Receive block or a Wait Until.
 */
public class Coordination extends AbstractIssueFinder {
    public static final String NAME = "coordination";

    @Override
    public void visit(WaitUntil node) {
        if (!(node.getUntil() instanceof UnspecifiedBoolExpr)) {
            addIssue(node, node.getMetadata(), IssueSeverity.MEDIUM);
        }
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            return false;
        }
        if (first.getFinder() != other.getFinder()) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }

    @Override
    public boolean isSubsumedBy(Issue first, Issue other) {
        if (first.getFinder() != this) {
            return super.isSubsumedBy(first, other);
        }

        if (other.getFinder() instanceof WaitingCheckToStop) {
            ASTNode node = first.getCodeLocation();
            if (other.hasMultipleBlocks()) {
                List<ASTNode> nodesOther = ((MultiBlockIssue) other).getNodes();
                for (ASTNode current : nodesOther) {
                    if (current == node) {
                        return true;
                    }
                }
            }
            if (node instanceof IfThenStmt ifThenStmt) {
                return ifThenStmt.getBoolExpr() == (other.getCodeLocation());
            }
        }

        return false;
    }
}
