/*
 * Copyright (C) 2019 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;

import java.util.Objects;

/**
 * An attribute of a sprite (e.g. position or colour)
 */
public class Attribute implements Defineable {

    public enum AttributeType {
        POSITION, ROTATION, COSTUME, SIZE //, BACKDROP // VOLUME?
        // VISIBILITY, LAYER, EFFECT
    };

    private ActorDefinition actor;

    private AttributeType attribute;

    public Attribute(ActorDefinition actor, AttributeType attribute) {
        this.actor = actor;
        this.attribute = attribute;
    }

    public AttributeType getAttributeType() {
        return attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;
        Attribute attribute1 = (Attribute) o;
        return Objects.equals(actor.getIdent(), attribute1.actor.getIdent()) &&
                Objects.equals(actor.getActorType(), attribute1.actor.getActorType()) &&
                attribute == attribute1.attribute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actor.getIdent(), actor.getActorType(), attribute);
    }

    public static Attribute positionOf(ActorDefinition actor) {
        return new Attribute(actor, AttributeType.POSITION);
    }

    public static Attribute rotationOf(ActorDefinition actor) {
        return new Attribute(actor, AttributeType.ROTATION);
    }

    public static Attribute costumeOf(ActorDefinition actor) {
        return new Attribute(actor, AttributeType.COSTUME);
    }

    public static Attribute sizeOf(ActorDefinition actor) {
        return new Attribute(actor, AttributeType.SIZE);
    }

//    public static Attribute backdropOf(ActorDefinition actor) {
//        return new Attribute(actor, AttributeType.BACKDROP);
//    }

}
