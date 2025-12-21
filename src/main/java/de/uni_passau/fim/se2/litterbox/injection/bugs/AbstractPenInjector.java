/*
 * Copyright (C) 2019-2025 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenDownStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.pen.PenUpStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

import java.util.List;

/**
 * Abstract base class for pen-related bug injectors that remove pen statements.
 * <p>
 * Precondition: The program must have BOTH PenUp and PenDown statements.
 * Subclasses specify which statement type to remove.
 */
public abstract class AbstractPenInjector extends AbstractBugInjector {

    private boolean hasPenUp = false;
    private boolean hasPenDown = false;

    /**
     * Returns the class of the pen statement type to remove.
     *
     * @return PenUpStmt.class or PenDownStmt.class
     */
    protected abstract Class<? extends Stmt> getStatementTypeToRemove();

    @Override
    public List<Program> inject(Program program) {
        // Check precondition: both PenUp and PenDown must exist
        hasPenUp = false;
        hasPenDown = false;
        checkPenStatements(program);

        if (!hasPenUp || !hasPenDown) {
            // Bug already exists or no pen statements - skip injection
            return List.of();
        }

        return super.inject(program);
    }

    private void checkPenStatements(Program program) {
        checkNode(program);
    }

    private void checkNode(ASTNode node) {
        if (node instanceof PenUpStmt) {
            hasPenUp = true;
        } else if (node instanceof PenDownStmt) {
            hasPenDown = true;
        }
        for (var child : node.getChildren()) {
            checkNode(child);
        }
    }

    @Override
    public void visit(StmtList node) {
        Class<? extends Stmt> targetType = getStatementTypeToRemove();

        // Only create mutation if this StmtList contains the target statement type
        boolean hasTarget = false;
        for (Stmt stmt : node.getStmts()) {
            if (targetType.isInstance(stmt)) {
                hasTarget = true;
                break;
            }
        }

        if (hasTarget) {
            addMutation(node, (StmtList clone) -> {
                // Remove ALL occurrences of the target statement type
                clone.getStmts().removeIf(targetType::isInstance);
            });
        }
        super.visit(node);
    }
}
