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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnnecessaryIf extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_if";

    @Override
    public void visit(StmtList node) {

        final List<Stmt> stmts = node.getStmts();
        StmtList lastList = null;
        IfThenStmt lastIf = null;
        for (Stmt s : stmts) {
            if (s instanceof IfThenStmt) {

                if (lastList != null) {
                    if (lastList.equals(((IfThenStmt) s).getThenStmts())) {
                        MultiBlockIssue issue;
                        if (currentScript != null) {
                            issue = new MultiBlockIssue(this, IssueSeverity.LOW, program, currentActor, currentScript, Arrays.asList(s, lastIf), s.getMetadata(), new Hint(getName()));
                        } else {
                            issue = new MultiBlockIssue(this, IssueSeverity.LOW, program, currentActor, currentProcedure, Arrays.asList(s, lastIf), s.getMetadata(), new Hint(getName()));
                        }
                        addIssue(issue);
                    }
                }
                lastList = ((IfThenStmt) s).getThenStmts();
                lastIf = (IfThenStmt) s;
            } else {
                // even if we already have a list from an ifstmt before, it only counts if a second ifstmt
                // follows directly after the first.
                lastList = null;
            }
        }

        visitChildren(node);
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            // Don't check against self
            return false;
        }

        if (first.getFinder() != other.getFinder()) {
            // Can only be a duplicate if it's the same finder
            return false;
        }

        if ((first.getScriptOrProcedureDefinition() == null) || (other.getScriptOrProcedureDefinition() == null)) {
            // Need to refer to same script
            return false;
        }

        if (!first.getScriptOrProcedureDefinition().equals(other.getScriptOrProcedureDefinition())) {
            // Need to refer to same script
            return false;
        }

        if (first instanceof MultiBlockIssue) {
            MultiBlockIssue mbFirst = (MultiBlockIssue) first;
            MultiBlockIssue mbOther = (MultiBlockIssue) other;
            Set<ASTNode> nodes = new HashSet<>(mbFirst.getNodes());
            nodes.retainAll(mbOther.getNodes());
            // If there is an overlap, the issues overlap on one if
            if (!nodes.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
