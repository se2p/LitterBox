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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;


import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.ColorExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.color.FromNumber;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import java.util.Iterator;
import java.util.LinkedList;

public class ColorParser {

    public static ColorExpression parseColor(JsonNode current, int pos, JsonNode allBlocks) throws ParsingException {
        //FIXME parse inputs that are not a text color as a "FromNumber" color

        LinkedList<JsonNode> inputs = new LinkedList<>();
        final Iterator<JsonNode> elements = current.get(INPUTS_KEY).elements();
        while (elements.hasNext()) {
            JsonNode jsonNode = elements.next();
            inputs.add(jsonNode);
        }

        String rgbCode = "";
        if (inputs.get(pos).get(POS_DATA_ARRAY).isArray()) {
            rgbCode = inputs.get(pos).get(POS_DATA_ARRAY).get(POS_INPUT_VALUE).asText();
        }

        if (rgbCode.startsWith("#")) {

            long rNumber = Long.parseLong(rgbCode.substring(1, 3), 16);
            long gNumber = Long.parseLong(rgbCode.substring(3, 5), 16);
            long bNumber = Long.parseLong(rgbCode.substring(5, 7), 16);

            return new ColorLiteral(rNumber, gNumber, bNumber);
        } else {
            final NumExpr numExpr = NumExprParser.parseNumExpr(current, pos, allBlocks);
            return new FromNumber(numExpr);
        }
    }
}
