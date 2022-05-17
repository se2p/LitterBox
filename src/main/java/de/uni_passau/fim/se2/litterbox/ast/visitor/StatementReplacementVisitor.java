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
package de.uni_passau.fim.se2.litterbox.ast.visitor;

import com.google.common.collect.Sets;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class StatementReplacementVisitor extends OnlyCodeCloneVisitor {

    private final Stmt target;
    private final Set<Stmt> otherTargets = Sets.newIdentityHashSet();
    private final List<Stmt> replacementStatements;

    public StatementReplacementVisitor(Stmt target, List<Stmt> replacement) {
        this.target = target;
        this.replacementStatements = replacement;
    }

    public StatementReplacementVisitor(Stmt target, Stmt... replacement) {
        this.target = target;
        this.replacementStatements = Arrays.asList(replacement);
    }

    public StatementReplacementVisitor(Stmt target, List<Stmt> otherTargets, List<Stmt> replacement) {
        this.target = target;
        this.otherTargets.addAll(otherTargets);
        this.replacementStatements = replacement;
    }

    protected boolean isTargetStatement(Stmt node) {
        return node == target;
    }


    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = new ArrayList<>();
        for (Stmt stmt : node.getStmts()) {
            if (isTargetStatement(stmt)) {
                for (Stmt replacement : replacementStatements) {
                    statements.add(replacement);
                }
            } else if (!otherTargets.contains(stmt)) {
                statements.add(apply(stmt));
            }
        }
        return new StmtList(statements);
    }
}
