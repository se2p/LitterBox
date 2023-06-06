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
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.MissingLoopSensing;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnnecessaryIfAfterUntil extends AbstractIssueFinder {
    public static final String NAME = "unnecessary_if_after_until";
    public static final String NAME_ELSE = "unnecessary_if_after_until_else";

    @Override
    public void visit(StmtList node) {
        List<Stmt> stmts = node.getStmts();
        for (int i = 0; i < stmts.size() - 1; i++) {
            if (stmts.get(i) instanceof UntilStmt until && stmts.get(i + 1) instanceof IfElseStmt ifElse) {
                if (until.getBoolExpr().equals(ifElse.getBoolExpr())) {
                    IssueBuilder builder = prepareIssueBuilder(ifElse)
                            .withSeverity(IssueSeverity.LOW)
                            .withHint(NAME_ELSE)
                            .withRefactoring(new StatementReplacementVisitor(ifElse, ifElse.getThenStmts().getStmts()).apply(getCurrentScriptEntity()));

                    addIssue(builder);
                }
            } else if (stmts.get(i) instanceof UntilStmt until && stmts.get(i + 1) instanceof IfThenStmt ifThen) {
                if (until.getBoolExpr().equals(ifThen.getBoolExpr())) {
                    IssueBuilder builder = prepareIssueBuilder(ifThen)
                            .withSeverity(IssueSeverity.LOW)
                            .withHint(NAME)
                            .withRefactoring(new StatementReplacementVisitor(ifThen, ifThen.getThenStmts().getStmts()).apply(getCurrentScriptEntity()));

                    addIssue(builder);
                }
            }
        }
        visitChildren(node);
    }

    @Override
    public boolean areCoupled(Issue first, Issue other) {
        if (first.getFinder() != this) {
            return super.areCoupled(first, other);
        }

        if (other.getFinder() instanceof MissingLoopSensing) {
            if (other.getActor().equals(first.getActor()) && other.getScriptOrProcedureDefinition().equals(first.getScriptOrProcedureDefinition())) {
                ASTNode otherLoc = other.getCodeLocation();
                while (!(otherLoc instanceof IfElseStmt) && !(otherLoc instanceof IfThenStmt)) {
                    otherLoc = otherLoc.getParentNode();
                }
                return otherLoc.equals(first.getCodeLocation());
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

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(NAME);
        keys.add(NAME_ELSE);
        return keys;
    }
}
