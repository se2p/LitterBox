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
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RecursiveCloning;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.DeleteClone;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

/**
 * Injects the RecursiveCloning bug by adding a "create clone of myself" block
 * at the start of "when I start as a clone" scripts.
 *
 * <p>
 * Only applies to scripts that do NOT already contain a "delete this clone" block,
 * since the finder ignores scripts with DeleteClone (no infinite recursion possible).
 */
public class RecursiveCloningInjector extends AbstractBugInjector {

    private final IssueFinder issueFinder = new RecursiveCloning();

    @Override
    public IssueFinder getIssueFinder() {
        return issueFinder;
    }

    @Override
    public void visit(Script node) {
        // Only inject if it's a StartedAsClone event and doesn't already delete the clone
        if (node.getEvent() instanceof StartedAsClone && !containsDeleteClone(node)) {
            modifyClonedNode(node, clonedNode -> {
                StmtList stmtList = clonedNode.getStmtList();

                // create MySelf
                Myself myself = new Myself(NonDataBlockMetadata.emptyNonBlockMetadata());

                // create CreateCloneOf
                CreateCloneOf createClone = new CreateCloneOf(myself, NonDataBlockMetadata.emptyNonBlockMetadata());

                myself.setParentNode(createClone); // Set parent for Myself
                createClone.setParentNode(stmtList); // Set parent for CreateCloneOf

                // Insert at the beginning
                stmtList.getStmts().addFirst(createClone);
            });
        }
        super.visit(node);
    }

    /**
     * Checks if the script contains a DeleteClone block.
     * The finder ignores scripts with DeleteClone since they can't cause infinite recursion.
     */
    private boolean containsDeleteClone(Script script) {
        DeleteCloneFinder finder = new DeleteCloneFinder();
        script.accept(finder);
        return finder.found;
    }

    private static class DeleteCloneFinder implements ScratchVisitor {
        boolean found = false;

        @Override
        public void visit(DeleteClone node) {
            found = true;
        }
    }
}
