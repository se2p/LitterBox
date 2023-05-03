/*
 * Copyright (C) 2019-2022 LitterBox contributors
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

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;

import java.util.Objects;

/**
 * An attribute of a sprite (e.g. position or colour)
 */
public class Attribute implements Defineable {

    // TODO: This should be replaced with a FixedAttribute?

    private LocalIdentifier actorIdentifier;
    private AttributeType attribute;

    public Attribute(LocalIdentifier actorIdentifier, AttributeType attribute) {
        this.actorIdentifier = actorIdentifier;
        this.attribute = attribute;
    }

    public static Attribute positionOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.POSITION);
    }

    public static Attribute rotationOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.ROTATION);
    }

    public static Attribute sizeOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.SIZE);
    }

    public static Attribute visibilityOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.VISIBILITY);
    }

    public static Attribute graphicEffectOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.GRAPHIC_EFFECT);
    }

    public static Attribute soundEffectOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.SOUND_EFFECT);
    }

    public static Attribute volumeOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.VOLUME);
    }

    public static Attribute layerOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.LAYER);
    }

    public static Attribute costumeOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.COSTUME);
    }

    public static Attribute backdropOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.BACKDROP);
    }

    public static Attribute timerOf(LocalIdentifier actorIdentifier) {
        return new Attribute(actorIdentifier, AttributeType.TIMER);
    }

    public LocalIdentifier getActorIdentifier() {
        return actorIdentifier;
    }

    public AttributeType getAttributeType() {
        return attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Attribute attribute1)) {
            return false;
        }
        return Objects.equals(actorIdentifier, attribute1.actorIdentifier)
                && attribute == attribute1.attribute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actorIdentifier, attribute);
    }

    public enum AttributeType {
        POSITION, ROTATION, SIZE, VISIBILITY, GRAPHIC_EFFECT, SOUND_EFFECT, VOLUME, LAYER, COSTUME, BACKDROP, TIMER //, BUBBLESTATE
    }
}
