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
package de.uni_passau.fim.se2.litterbox.ast.model;

import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Objects;

public class ActorType extends AbstractNode implements ASTLeaf {

    private enum Type {
        STAGE,
        SPRITE;
    }

    private Type type;

    public ActorType(ActorType other) {
        this.type = other.type;
    }

    private ActorType(Type type) {
        this.type = type;
    }

    public static ActorType getStage() {
        return new ActorType(Type.STAGE);
    }

    public static ActorType getSprite() {
        return new ActorType(Type.SPRITE);
    }

    public Type getType() {
        return type;
    }

    public boolean isStage() {
        return type.equals(Type.STAGE);
    }

    public boolean isSprite() {
        return type.equals(Type.SPRITE);
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ASTNode accept(CloneVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getUniqueName() {
        return getClass().getSimpleName();
    }

    @Override
    public BlockMetadata getMetadata() {
        return new NoBlockMetadata();
    }

    @Override
    public String[] toSimpleStringArray() {
        String[] returnArray = {type.name()};
        return returnArray;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActorType)) return false;
        ActorType actorType = (ActorType) o;
        return type == actorType.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
