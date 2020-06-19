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
package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;

public class StatementNode extends CFGNode {

    private ASTNode node;

    public StatementNode(ASTNode node, ActorDefinition actor, ASTNode scriptOrProcedure) {
        this.node = node;
        this.actor = actor;
        this.scriptOrProcedure = scriptOrProcedure;
    }

    @Override
    public ASTNode getASTNode() {
        return node;
    }

    @Override
    public String toString() {
        return node.getClass().getSimpleName() + "@" + System.identityHashCode(node);
    }

    // No hashcode&equals on purpose, to ensure contract in Graph
}
