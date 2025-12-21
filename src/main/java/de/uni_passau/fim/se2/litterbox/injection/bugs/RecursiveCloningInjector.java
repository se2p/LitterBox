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

import de.uni_passau.fim.se2.litterbox.analytics.IssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.RecursiveCloning;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.clonechoice.Myself;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.CreateCloneOf;
import de.uni_passau.fim.se2.litterbox.injection.AbstractBugInjector;

public class RecursiveCloningInjector extends AbstractBugInjector {

    @Override
    public IssueFinder getIssueFinder() {
        return new RecursiveCloning();
    }

    @Override
    public void visit(Script node) {
        if (node.getEvent() instanceof StartedAsClone) {
            addMutation(node, clonedNode -> {
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
}
