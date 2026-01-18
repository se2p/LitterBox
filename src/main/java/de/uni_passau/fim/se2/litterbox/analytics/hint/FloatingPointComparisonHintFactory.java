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
package de.uni_passau.fim.se2.litterbox.analytics.hint;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.bugs.FloatingPointComparison;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AttributeOf;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class FloatingPointComparisonHintFactory {
    public static final String DISTANCE_ZERO_SPRITES = "position_equals_check_zero_sprites";
    public static final String DISTANCE_ZERO_MOUSE = "position_equals_check_zero_mouse";
    public static final String DISTANCE = "position_equals_check_dist";
    public static final String MOUSE = "floating_point_mouse";
    public static final String POSITION = "position_equals_check";
    public static final String DEFAULT = FloatingPointComparison.NAME;
    public static final String COORDINATE = "COORDINATE";

    public static Hint generateHint(Equals node) {
        Hint hint;
        if (node.getOperand1() instanceof DistanceTo) {
            hint = generateHintForDistance(node.getOperand1(), node.getOperand2());
        } else if (node.getOperand2() instanceof DistanceTo) {
            hint = generateHintForDistance(node.getOperand2(), node.getOperand1());
        } else if (node.getOperand1() instanceof MouseX || node.getOperand1() instanceof MouseY || node.getOperand2() instanceof MouseX || node.getOperand2() instanceof MouseY) {
            hint = Hint.fromKey(MOUSE);
            hint.setParameter(COORDINATE, getAttributeType(node));
        } else if (node.getOperand1() instanceof PositionX || node.getOperand1() instanceof PositionY || node.getOperand2() instanceof PositionX || node.getOperand2() instanceof PositionY) {
            hint = Hint.fromKey(POSITION);
            hint.setParameter(COORDINATE, getAttributeType(node));
        } else {
            hint = Hint.fromKey(DEFAULT);
            hint.setParameter(COORDINATE, getAttributeType(node));
        }
        return hint;
    }

    private static Hint generateHintForDistance(ASTNode operand1, ASTNode operand2) {
        Hint hint;
        if (operand2 instanceof NumberLiteral numberLiteral && numberLiteral.getValue() == 0) {
            if (isMousePosition(((DistanceTo) operand1).getPosition())) {
                hint = Hint.fromKey(DISTANCE_ZERO_MOUSE);
            } else {
                hint = Hint.fromKey(DISTANCE_ZERO_SPRITES);
            }
        } else {
            hint = Hint.fromKey(DISTANCE);
        }
        return hint;
    }

    private static String getAttributeType(Equals node) {
        if (checkType(node.getOperand1(), PositionX.class) || checkType(node.getOperand2(), PositionX.class) || checkType(node.getOperand1(), MouseX.class) || checkType(node.getOperand2(), MouseX.class)) {
            return "x";
        }
        if (checkType(node.getOperand1(), PositionY.class) || checkType(node.getOperand2(), PositionY.class) || checkType(node.getOperand1(), MouseY.class) || checkType(node.getOperand2(), MouseY.class)) {
            return "y";
        }
        if (checkType(node.getOperand1(), Direction.class) || checkType(node.getOperand2(), Direction.class)) {
            return "direction";
        }
        if (checkType(node.getOperand1(), Volume.class) || checkType(node.getOperand2(), Volume.class)) {
            return "volume";
        }
        if (checkType(node.getOperand1(), Timer.class) || checkType(node.getOperand2(), Timer.class)) {
            return "timer";
        }

        if (node.getOperand1() instanceof AttributeOf attributeOf) {
            String type = getAttributeTypeFromAttributeOf(attributeOf);
            if (type != null) {
                return type;
            }
        }
        if (node.getOperand2() instanceof AttributeOf attributeOf) {
            String type = getAttributeTypeFromAttributeOf(attributeOf);
            if (type != null) {
                return type;
            }
        }
        return "";
    }

    private static boolean checkType(ASTNode node, Class<?> clazz) {
        return clazz.isInstance(node);
    }

    private static String getAttributeTypeFromAttributeOf(AttributeOf attributeOf) {
        if (attributeOf.getAttribute() instanceof AttributeFromFixed attributeFromFixed) {
            if (attributeFromFixed.getAttribute().getType() == FixedAttribute.FixedAttributeType.X_POSITION) {
                return "x";
            }
            if (attributeFromFixed.getAttribute().getType() == FixedAttribute.FixedAttributeType.Y_POSITION) {
                return "y";
            }
            if (attributeFromFixed.getAttribute().getType() == FixedAttribute.FixedAttributeType.DIRECTION) {
                return "direction";
            }
            if (attributeFromFixed.getAttribute().getType() == FixedAttribute.FixedAttributeType.VOLUME) {
                return "volume";
            }
        }
        return null;
    }

    private static boolean isMousePosition(ASTNode node) {
        return node instanceof MousePos;
    }

    public static Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(DISTANCE_ZERO_SPRITES);
        keys.add(DISTANCE_ZERO_MOUSE);
        keys.add(DISTANCE);
        keys.add(DEFAULT);
        keys.add(POSITION);
        keys.add(MOUSE);
        return keys;
    }
}
