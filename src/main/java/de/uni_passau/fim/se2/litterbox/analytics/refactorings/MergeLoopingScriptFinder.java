/*
 * Copyright (C) 2019-2026 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptList;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.TimedStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeLoopingScript;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MergeLoopingScriptFinder extends AbstractDependencyRefactoringFinder {

    @Override
    public void visit(ActorDefinition actor) {
        ScriptList scriptList = actor.getScripts();
        List<Script> scripts = scriptList.getScriptList();

        for (int i = 0; i < scripts.size(); i++) {
            for (int j = i + 1; j < scripts.size(); j++) {
                Script s1 = scripts.get(i);
                Script s2 = scripts.get(j);

                if (canMerge(s1, s2)) {
                    refactorings.add(new MergeLoopingScript(s1, s2));
                }
            }
        }
    }

    private boolean canMerge(Script s1, Script s2) {
        if (s1.getEvent() instanceof Never || s2.getEvent() instanceof Never) {
            return false;
        }

        if (!s1.getEvent().equals(s2.getEvent())) {
            return false;
        }

        if (s1.getStmtList().getStmts().size() != 1 || s2.getStmtList().getStmts().size() != 1) {
            return false;
        }

        Stmt stmt1 = s1.getStmtList().getStmts().getFirst();
        Stmt stmt2 = s2.getStmtList().getStmts().getFirst();

        if (!(stmt1 instanceof LoopStmt) || !(stmt2 instanceof LoopStmt)) {
            return false;
        }

        if (!stmt1.getClass().equals(stmt2.getClass())) {
            return false;
        }

        // Check loop specific conditions
        if (stmt1 instanceof RepeatTimesStmt r1) {
            RepeatTimesStmt r2 = (RepeatTimesStmt) stmt2;
            if (!r1.getTimes().equals(r2.getTimes())) {
                return false;
            }
        } else if (stmt1 instanceof UntilStmt u1) {
            UntilStmt u2 = (UntilStmt) stmt2;
            if (!u1.getBoolExpr().equals(u2.getBoolExpr())) {
                return false;
            }
        }

        // Check for time-consuming statements in loop bodies
        if (containsTimeConsuming(((LoopStmt) stmt1).getStmtList())
                || containsTimeConsuming(((LoopStmt) stmt2).getStmtList())) {
            return false;
        }

        // Check for variable usage dependencies
        DependencyVisitor v1 = new DependencyVisitor();
        s1.accept(v1);

        DependencyVisitor v2 = new DependencyVisitor();
        s2.accept(v2);

        // Check for conflicts
        if (!Collections.disjoint(v1.writes, v2.writes)) {
            return false; // Write-Write conflict
        }
        if (!Collections.disjoint(v1.writes, v2.reads)) {
            return false; // Write-Read conflict
        }
        return Collections.disjoint(v1.reads, v2.writes); // Read-Write conflict
    }

    private static class DependencyVisitor implements ScratchVisitor {
        final Set<Identifier> reads = new HashSet<>();
        final Set<Identifier> writes = new HashSet<>();

        @Override
        public void visit(SetVariableTo node) {
            writes.add(node.getIdentifier());
            node.getExpr().accept(this);
        }

        @Override
        public void visit(ChangeVariableBy node) {
            writes.add(node.getIdentifier());
            node.getExpr().accept(this);
        }

        @Override
        public void visit(Variable node) {
            reads.add(node.getName());
        }
    }

    private boolean containsTimeConsuming(StmtList stmts) {
        for (Stmt s : stmts.getStmts()) {
            if (s instanceof TimedStmt) {
                return true;
            }
            if (s instanceof LoopStmt loop) {
                if (containsTimeConsuming(loop.getStmtList())) {
                    return true;
                }
            } else if (s instanceof IfThenStmt ifThen) {
                if (containsTimeConsuming(ifThen.getThenStmts())) {
                    return true;
                }
            } else if (s instanceof IfElseStmt ifElse) {
                if (containsTimeConsuming(ifElse.getThenStmts()) || containsTimeConsuming(ifElse.getElseStmts())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return MergeLoopingScript.NAME;
    }
}
