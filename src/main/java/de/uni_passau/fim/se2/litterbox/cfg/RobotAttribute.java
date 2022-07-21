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
        if (!(o instanceof RobotAttribute)) {
            return false;
        }
        RobotAttribute attribute1 = (RobotAttribute) o;
        return Objects.equals(actorIdentifier, attribute1.actorIdentifier)
                && attribute == attribute1.attribute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actorIdentifier, attribute);
    }

    public enum AttributeType {
        LED, ROCKY_LIGHT, MOTOR_POWER, MATRIX
    }
}
