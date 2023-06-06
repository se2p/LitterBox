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
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;

import java.util.*;

public class UnnecessaryIf extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_if";

    @Override
    public void visit(StmtList node) {

        final List<Stmt> stmts = node.getStmts();
        StmtList lastList = null;
        IfThenStmt lastIf = null;
        for (Stmt s : stmts) {
            if (s instanceof IfThenStmt ifThenStmt) {
                if (lastList != null && lastList.equals(ifThenStmt.getThenStmts())) {
                    Or or = new Or(lastIf.getBoolExpr(), ifThenStmt.getBoolExpr(), lastIf.getMetadata());
                    IfThenStmt joinedIf = new IfThenStmt(or, lastIf.getThenStmts(), s.getMetadata());
                    StatementReplacementVisitor visitor = new StatementReplacementVisitor(
                            lastIf,
                            Arrays.asList(s),
                            Arrays.asList(joinedIf)
                    );
                    ScriptEntity refactored = visitor.apply(getCurrentScriptEntity());

                    MultiBlockIssue issue = new MultiBlockIssue(this, IssueSeverity.LOW, program, currentActor,
                            getCurrentScriptEntity(), Arrays.asList(s, lastIf), s.getMetadata(), new Hint(getName()));
                    issue.setRefactoredScriptOrProcedureDefinition(refactored);
                    addIssue(issue);
                }
                lastList = ifThenStmt.getThenStmts();
                lastIf = ifThenStmt;
            } else {
                // even if we already have a list from an ifstmt before, it only counts if a second ifstmt
                // follows directly after the first.
                lastList = null;
                lastIf = null;
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

        if (first instanceof MultiBlockIssue mbFirst) {
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
