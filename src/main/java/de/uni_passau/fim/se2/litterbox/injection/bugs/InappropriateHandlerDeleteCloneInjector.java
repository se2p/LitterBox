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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.InappropriateHandlerDeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.TerminationStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;
import de.uni_passau.fim.se2.litterbox.injection.InjectionResult;

import java.util.List;

/**
 * Injects InappropriateHandlerDeleteClone bug by adding DeleteClone to GreenFlag scripts.
 *
 * <p>
 * Only applies to programs that have both CreateCloneOf blocks and StartedAsClone event handlers,
 * ensuring the injected bug is meaningful in the context of clone management.
 *
 * <p>
 * DeleteClone cannot have successors, so it's inserted at a valid position:
 * - If the StmtList ends with a termination statement, replace it
 * - If the StmtList ends with a forever loop, insert into the loop (recursively)
 */
public class InappropriateHandlerDeleteCloneInjector extends AbstractBugInjector {

    private boolean hasCreateClone;
    private boolean hasStartedAsClone;

    private final IssueFinder issueFinder = new InappropriateHandlerDeleteClone();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public List<InjectionResult> inject(Program program) {
        // Check prerequisites: program must have both CreateClone and StartedAsClone
        hasCreateClone = false;
        hasStartedAsClone = false;
        checkPrerequisites(program);

        if (!hasCreateClone || !hasStartedAsClone) {
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
            public void visit(Script node) {
                if (node.getEvent() instanceof StartedAsClone) {
                    hasStartedAsClone = true;
                }
                visitChildren(node);
            }
        });
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof GreenFlag) {
            // Skip if script already has DeleteClone
            if (hasDeleteClone(node.getStmtList())) {
                super.visit(node);
                return;
            }

            modifyClonedNode(node, clonedScript -> {
                StmtList stmtList = clonedScript.getStmtList();
                insertDeleteClone(stmtList);
            });
        }
        super.visit(node);
    }

    /**
     * Inserts DeleteClone at a valid position in the given StmtList.
     * Handles termination statements by replacing them, and forever loops by recursing into them.
     */
    private void insertDeleteClone(StmtList stmtList) {
        List<Stmt> stmts = stmtList.getStmts();

        if (stmts.isEmpty()) {
            DeleteClone deleteClone = new DeleteClone(NonDataBlockMetadata.emptyNonBlockMetadata());
            stmts.add(deleteClone);
            deleteClone.setParentNode(stmtList);
            return;
        }

        Stmt lastStmt = stmts.getLast();

        if (lastStmt instanceof TerminationStmt) {
            // Replace termination statement with DeleteClone
            stmts.removeLast();
            DeleteClone deleteClone = new DeleteClone(NonDataBlockMetadata.emptyNonBlockMetadata());
            stmts.add(deleteClone);
            deleteClone.setParentNode(stmtList);
        } else if (lastStmt instanceof RepeatForeverStmt foreverStmt) {
            // Insert into the forever loop (recursively)
            insertDeleteClone(foreverStmt.getStmtList());
        } else {
            // Can safely append
            DeleteClone deleteClone = new DeleteClone(NonDataBlockMetadata.emptyNonBlockMetadata());
            stmts.add(deleteClone);
            deleteClone.setParentNode(stmtList);
        }
    }

    private boolean hasDeleteClone(StmtList stmtList) {
        // Use a visitor to recursively check ALL nested structures
        final boolean[] found = {false};
        stmtList.accept(new ScratchVisitor() {
            @Override
            public void visit(DeleteClone node) {
                found[0] = true;
            }
        });
        return found[0];
    }
}
