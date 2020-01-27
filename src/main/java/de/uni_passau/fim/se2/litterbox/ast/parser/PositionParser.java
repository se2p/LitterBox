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

import static de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteMotionStmtOpcode.*;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.position.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.StrId;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteMotionStmtOpcode;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import java.util.ArrayList;

public class PositionParser {

    public static Position parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        if (current.get(Constants.INPUTS_KEY).has("X") && current.get(Constants.INPUTS_KEY).has("Y")) {
            return parseCoordinate(current, allBlocks);
        } else if (current.get(Constants.INPUTS_KEY).has("TO") ||
                current.get(Constants.INPUTS_KEY).has("TOWARDS") ||
                current.get(Constants.INPUTS_KEY).has("DISTANCETOMENU")) {
            return parseRelativePos(current, allBlocks);
        } else {
            throw new ParsingException("Could not parse block " + current.toString());
        }
    }

    private static Position parseRelativePos(JsonNode current, JsonNode allBlocks) throws ParsingException {
        ArrayList<JsonNode> inputs = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputs::add);

        JsonNode menuID;
        String opcodeString = current.get(Constants.OPCODE_KEY).asText();
        int positionInput = 0;
        if (SpriteMotionStmtOpcode.contains(opcodeString)) {
            SpriteMotionStmtOpcode opcode = valueOf(opcodeString);

            if (motion_goto.equals(opcode) || motion_pointtowards.equals(opcode)) {
                positionInput = 0;
                menuID = inputs.get(positionInput).get(Constants.POS_INPUT_VALUE);
            } else if (motion_glideto.equals(opcode)) {
                positionInput = 1;
                menuID = inputs.get(positionInput).get(Constants.POS_INPUT_VALUE);
            } else {
                throw new ParsingException(
                        "Cannot parse relative coordinates for a block with opcode " + current.get(Constants.OPCODE_KEY));
            }
        } else if (NumExprOpcode.sensing_distanceto.toString().equals(opcodeString)) {
            positionInput = 0;
            menuID = inputs.get(positionInput).get(Constants.POS_INPUT_VALUE);
        } else {
            throw new ParsingException(
                    "Cannot parse relative coordinates for a block with opcode " + current.get(Constants.OPCODE_KEY));
        }

        if (getShadowIndicator((ArrayNode) inputs.get(positionInput)) == 1) {
            ArrayList<JsonNode> fields = new ArrayList<>();
            allBlocks.get(menuID.asText()).get(Constants.FIELDS_KEY).elements().forEachRemaining(fields::add);
            String posString = fields.get(Constants.FIELD_VALUE).get(0).asText();

            if (posString.equals("_mouse_")) {
                return new MousePos();
            } else if (posString.equals("_random_")) {
                return new RandomPos();
            } else {
                return new PivotOf(new AsString(new StrId(posString)));
            }
        } else {
            String posName = "";
            if (motion_goto.toString().equals(opcodeString) || motion_glideto.toString().equals(opcodeString)) {
                posName = "TO";
            } else if (motion_pointtowards.toString().equals(opcodeString)) {
                posName = "TOWARDS";
            } else if (NumExprOpcode.sensing_distanceto.toString().equals(opcodeString)) {
                posName = "DISTANCETOMENU";
            }


            final StringExpr stringExpr = StringExprParser.parseStringExpr(current, posName, allBlocks);
            return new PivotOf(stringExpr);
        }
    }

    private static Position parseCoordinate(JsonNode current, JsonNode allBlocks) throws ParsingException {
        SpriteMotionStmtOpcode spriteMotionStmtOpcode = valueOf(current.get(Constants.OPCODE_KEY).asText());
        if (motion_glidesecstoxy.equals(spriteMotionStmtOpcode)) {
            NumExpr xExpr = NumExprParser.parseNumExpr(current, 1, allBlocks);
            NumExpr yExpr = NumExprParser.parseNumExpr(current, 2, allBlocks);
            return new CoordinatePosition(xExpr, yExpr);
        } else if (motion_gotoxy.equals(spriteMotionStmtOpcode)) {
            NumExpr xExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
            NumExpr yExpr = NumExprParser.parseNumExpr(current, 1, allBlocks);
            return new CoordinatePosition(xExpr, yExpr);
        } else {
            throw new ParsingException(
                    "Cannot parse x and y coordinates for a block with opcode " + current.get(Constants.OPCODE_KEY));
        }

    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }


}
