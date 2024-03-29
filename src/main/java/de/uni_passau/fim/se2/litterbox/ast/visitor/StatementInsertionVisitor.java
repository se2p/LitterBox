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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;

import java.util.Arrays;
import java.util.List;

public class StatementInsertionVisitor extends OnlyCodeCloneVisitor {
    private final ASTNode parent;
    private final List<Stmt> replacementStatements;
    private final int position;

    public StatementInsertionVisitor(ASTNode parent, int position, List<Stmt> replacement) {
        this.parent = parent;
        this.replacementStatements = replacement;
        this.position = position;
    }

    public StatementInsertionVisitor(ASTNode parent, int position, Stmt... replacement) {
        this.parent = parent;
        this.replacementStatements = Arrays.asList(replacement);
        this.position = position;
    }

    protected boolean isTargetStatement(ASTNode node) {
        return node == parent;
    }

    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = applyList(node.getStmts());
        if (isTargetStatement(node.getParentNode())) {
            statements.addAll(position, replacementStatements);
        }
        return new StmtList(statements);
    }
}
