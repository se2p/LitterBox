/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Set;

public interface DefinableCollector<T extends Defineable> extends ScratchVisitor {
    Set<T> getDefineables();

    default ActorDefinition getActorSprite(ActorDefinition actor) {
        if (actor.isStage()) {
            return actor;
        } else {
            ASTNode parent = actor.getParentNode();
            if (parent != null && parent.getParentNode() != null) {
                Program program = (Program) parent.getParentNode();
                return program.getActorDefinitionList().getDefinitions().getFirst();
            }
            // Fallback: return actor itself
            return actor;
        }
    }
}
