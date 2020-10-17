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
package de.uni_passau.fim.se2.litterbox.analytics.hint;

import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.bugpattern.PositionEqualsCheck;
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

public abstract class PositionEqualsCheckHintFactory {
    public static final String DISTANCE_ZERO_SPRITES = "position_equals_check_zero_sprites";
    public static final String DISTANCE_ZERO_MOUSE = "position_equals_check_zero_mouse";
    public static final String DISTANCE = "position_equals_check_dist";
    public static final String DEFAULT = PositionEqualsCheck.NAME;
    public static final String COORDINATE = "COORDINATE";

    public static Hint generateHint(Equals node) {
        Hint hint;
        if (node.getOperand1() instanceof DistanceTo) {
            hint = generateHintForDistance(node.getOperand1(), node.getOperand2());
        } else if (node.getOperand2() instanceof DistanceTo) {
            hint = generateHintForDistance(node.getOperand2(), node.getOperand1());
        } else {
            hint = new Hint(DEFAULT);
            hint.setParameter(COORDINATE, getCoordinate(node));
        }
        return hint;
    }

    private static Hint generateHintForDistance(ASTNode operand1, ASTNode operand2) {
        Hint hint;
        if (operand2 instanceof NumberLiteral && ((NumberLiteral) operand2).getValue() == 0) {
            if (isMousePosition(((DistanceTo) operand1).getPosition())) {
                hint = new Hint(DISTANCE_ZERO_MOUSE);
            } else {
                hint = new Hint(DISTANCE_ZERO_SPRITES);
            }
        } else {
            hint = new Hint(DISTANCE);
        }
        return hint;
    }

    private static String getCoordinate(Equals node) {
        if (node.getOperand1() instanceof PositionX || node.getOperand1() instanceof MouseX
                || node.getOperand2() instanceof PositionX || node.getOperand2() instanceof MouseX) {
            return "x";
        }
        if (node.getOperand1() instanceof PositionY || node.getOperand1() instanceof MouseY
                || node.getOperand2() instanceof PositionY || node.getOperand2() instanceof MouseY) {
            return "y";
        }
        if (node.getOperand1() instanceof AttributeOf) {
            if (((AttributeOf) node.getOperand1()).getAttribute() instanceof AttributeFromFixed
                    && ((AttributeFromFixed) ((AttributeOf) node.getOperand1()).getAttribute()).getAttribute().getType() == FixedAttribute.FixedAttributeType.X_POSITION) {
                return "x";
            }
            if (((AttributeOf) node.getOperand1()).getAttribute() instanceof AttributeFromFixed
                    && ((AttributeFromFixed) ((AttributeOf) node.getOperand1()).getAttribute()).getAttribute().getType() == FixedAttribute.FixedAttributeType.Y_POSITION) {
                return "y";
            }
        }
        if (node.getOperand2() instanceof AttributeOf) {
            if (((AttributeOf) node.getOperand2()).getAttribute() instanceof AttributeFromFixed
                    && ((AttributeFromFixed) ((AttributeOf) node.getOperand2()).getAttribute()).getAttribute().getType() == FixedAttribute.FixedAttributeType.X_POSITION) {
                return "x";
            }
            if (((AttributeOf) node.getOperand2()).getAttribute() instanceof AttributeFromFixed
                    && ((AttributeFromFixed) ((AttributeOf) node.getOperand2()).getAttribute()).getAttribute().getType() == FixedAttribute.FixedAttributeType.Y_POSITION) {
                return "y";
            }
        }
        return "";
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
        return keys;
    }
}
