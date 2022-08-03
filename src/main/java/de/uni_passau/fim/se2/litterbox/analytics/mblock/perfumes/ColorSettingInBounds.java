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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RGBValue;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.led.RGBValuesPosition;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;

public class ColorSettingInBounds extends AbstractRobotFinder {
    private static final String NAME = "color_setting_in_bounds";
    private static final int COLOR_MAX = 255;
    private static final int COLOR_MIN = 0;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(RGBValue node) {
        if (colorOutOfBounds(node.getValue())) {
            addIssue(node, node.getMetadata());
        }
    }

    @Override
    public void visit(RGBValuesPosition node) {
        if (colorOutOfBounds(node.getBlue()) && colorOutOfBounds(node.getGreen()) && colorOutOfBounds(node.getRed())) {
            addIssue(node, node.getMetadata());
        }
    }

    private boolean colorOutOfBounds(NumExpr node) {
        if (node instanceof NumberLiteral) {
            double value = ((NumberLiteral) node).getValue();
            return value >= COLOR_MIN && value <= COLOR_MAX;
        }
        return false;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
