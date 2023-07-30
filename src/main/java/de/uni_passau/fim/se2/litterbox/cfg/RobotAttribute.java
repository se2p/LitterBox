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

public class RobotAttribute implements Defineable {

    // TODO: This should be replaced with a FixedAttribute?

    private final LocalIdentifier actorIdentifier;
    private final AttributeType attribute;

    public RobotAttribute(LocalIdentifier actorIdentifier, AttributeType attribute) {
        this.actorIdentifier = actorIdentifier;
        this.attribute = attribute;
    }

    public static RobotAttribute ledColor(LocalIdentifier actorIdentifier) {
        return new RobotAttribute(actorIdentifier, AttributeType.LED);
    }

    public static RobotAttribute rockyLightColor(LocalIdentifier actorIdentifier) {
        return new RobotAttribute(actorIdentifier, AttributeType.ROCKY_LIGHT);
    }

    public static RobotAttribute motorPower(LocalIdentifier actorIdentifier) {
        return new RobotAttribute(actorIdentifier, AttributeType.MOTOR_POWER);
    }

    public static RobotAttribute matrix(LocalIdentifier actorIdentifier) {
        return new RobotAttribute(actorIdentifier, AttributeType.MATRIX);
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
        if (!(o instanceof RobotAttribute attribute1)) {
            return false;
        }
        return Objects.equals(actorIdentifier, attribute1.actorIdentifier) && attribute == attribute1.attribute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actorIdentifier, attribute);
    }

    public enum AttributeType {
        LED, ROCKY_LIGHT, MOTOR_POWER, MATRIX
    }
}
