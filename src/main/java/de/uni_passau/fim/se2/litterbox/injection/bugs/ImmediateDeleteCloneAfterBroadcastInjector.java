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
package de.uni_passau.fim.se2.litterbox.injection.bugs;

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.ImmediateDeleteCloneAfterBroadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.List;

/**
 * Injects ImmediateDeleteCloneAfterBroadcast bug by inserting DeleteClone after Broadcast.
 *
 * <p>
 * Prerequisites:
 * - Program must have at least one CreateCloneOf statement
 * - Program must have at least one "when I start as a clone" hat block
 *
 * <p>
 * DeleteClone cannot have successor nodes, so statements after it are removed.
 */
public class ImmediateDeleteCloneAfterBroadcastInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new ImmediateDeleteCloneAfterBroadcast();

    private boolean hasCreateClone = false;
    private boolean hasStartedAsClone = false;

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        // Check prerequisites
        hasCreateClone = false;
        hasStartedAsClone = false;
        checkPrerequisites(program);

        if (!hasCreateClone || !hasStartedAsClone) {
            // Don't inject if prerequisites aren't met
            return List.of();
        }

        return super.inject(program);
    }

    private void checkPrerequisites(Program program) {
        program.accept(new ScratchVisitor() {
            @Override
            public void visit(CreateCloneOf node) {
                hasCreateClone = true;
            }

            @Override
            public void visit(StartedAsClone node) {
                hasStartedAsClone = true;
            }
        });
    }

    @Override
    public void visit(Broadcast node) {
        // Skip if already followed by DeleteClone
        if (isFollowedByDeleteClone(node)) {
            super.visit(node);
            return;
        }

        modifyClonedNode(node, clonedBroadcast -> {
            ASTNode parent = clonedBroadcast.getParentNode();
            if (parent instanceof StmtList stmtList) {
                List<Stmt> stmts = stmtList.getStmts();
                int index = stmts.indexOf(clonedBroadcast);
                if (index >= 0) {
                    // Remove all statements after the Broadcast
                    // (DeleteClone cannot have successors)
                    while (stmts.size() > index + 1) {
                        stmts.remove(index + 1);
                    }

                    // Insert DeleteClone after Broadcast
                    DeleteClone deleteClone = new DeleteClone(NonDataBlockMetadata.emptyNonBlockMetadata());
                    stmts.add(deleteClone);
                    deleteClone.setParentNode(stmtList);
                }
            }
        });

        super.visit(node);
    }

    private boolean isFollowedByDeleteClone(Broadcast node) {
        ASTNode parent = node.getParentNode();
        if (parent instanceof StmtList stmtList) {
            List<Stmt> stmts = stmtList.getStmts();
            int index = stmts.indexOf(node);
            if (index >= 0 && index + 1 < stmts.size()) {
                return stmts.get(index + 1) instanceof DeleteClone;
            }
        }
        return false;
    }
}
