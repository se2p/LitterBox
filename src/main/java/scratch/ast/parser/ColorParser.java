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
package scratch.ast.parser;

import static scratch.ast.Constants.INPUTS_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.ast.model.color.Color;
import scratch.ast.model.color.Rgba;
import scratch.ast.model.expression.num.Number;

public class ColorParser {

    public static Color parseColor(JsonNode current, int pos, JsonNode allBlocks) {
        String rgbCode = current.get(INPUTS_KEY).get("COLOR").get(1).get(1).asText(); //TODO replace magic nums
        Long i;
        Float f;
        i = Long.parseLong(rgbCode.substring(1, 3), 16);
        f = Float.intBitsToFloat(i.intValue());
        Number rNumber = new Number(f);

        i = Long.parseLong(rgbCode.substring(3, 5), 16);
        f = Float.intBitsToFloat(i.intValue());
        Number gNumber = new Number(f);

        i = Long.parseLong(rgbCode.substring(5, 7), 16);
        f = Float.intBitsToFloat(i.intValue());
        Number bNumber = new Number(f);

        //There is no alpha value
        Number aNumber = new Number(1);

        return new Rgba(rNumber, gNumber, bNumber, aNumber);
    }

}
