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
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import static de.uni_passau.fim.se2.litterbox.analytics.CommentAdder.addBlockComment;


import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The repeat until blocks require a stopping condition.
 * If the condition is missing, the result is an infinite loop.
 * This will then prevent the execution of blocks following the loop in the script.
 */
public class MissingTerminationCondition implements IssueFinder, ScratchVisitor {
    public static final String NAME = "missing_termination";
    public static final String SHORT_NAME = "mssTerm";
    public static final String HINT_TEXT = "missing termination";
    private int count;
    private Set<Issue> issues = new LinkedHashSet<>();
    private Program program;
    private ActorDefinition currentActor;

    @Override
    public Set<Issue> check(Program program) {
        Preconditions.checkNotNull(program);
        count = 0;
        this.program = program;
        program.accept(this);
        return issues;
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
        if (node.getBoolExpr() instanceof UnspecifiedBoolExpr) {
            count++;
            issues.add(new Issue(this, currentActor, node));
            addBlockComment((NonDataBlockMetadata) node.getMetadata(), currentActor, HINT_TEXT,
                    SHORT_NAME + count);
        }
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
