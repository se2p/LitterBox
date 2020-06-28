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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks for nested loops.
 */
public class NestedLoops implements IssueFinder, ScratchVisitor {

    public static final String NAME = "nested_loops";
    public static final String SHORT_NAME = "nestLoop";
   private ActorDefinition currentActor;
    private Set<Issue> issues = new LinkedHashSet<>();

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        program.accept(this);
        return issues;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        for (ASTNode child : actor.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(UntilStmt node) {
        checkNested(node.getStmtList().getStmts());
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        checkNested(node.getStmtList().getStmts());
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    private void checkNested(List<Stmt> stmts) {
        if (stmts.size() == 1 && ((stmts.get(0) instanceof UntilStmt) || (stmts.get(0) instanceof RepeatTimesStmt) || (stmts.get(0) instanceof RepeatForeverStmt))) {
            // TODO: Cast is nasty
            issues.add(new Issue(this, currentActor, (AbstractNode) stmts.get(0)));
        }
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        checkNested(node.getStmtList().getStmts());
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }
}