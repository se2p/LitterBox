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
package de.uni_passau.fim.se2.litterbox.ast.model;

import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.List;
import java.util.Optional;

public class ActorDefinitionList extends AbstractNode {

    private final List<ActorDefinition> actorDefinitions;

    public ActorDefinitionList(List<ActorDefinition> actorDefinitions) {
        super(actorDefinitions);
        this.actorDefinitions = Preconditions.checkNotNull(actorDefinitions);
    }

    public List<ActorDefinition> getDefinitions() {
        return actorDefinitions;
    }

    public Optional<ActorDefinition> getActorDefinition(String id) {
        return actorDefinitions.stream().filter(actor -> actor.getIdent().getName().equals(id)).findFirst();
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }
}
