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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteMotionStmtOpcode;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;

import static de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteMotionStmtOpcode.*;

public class PositionParser {

    public static Position parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        if (current.get(Constants.INPUTS_KEY).has("TO") ||
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
        int positionInput;
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
                return new FromExpression(new AsString(new StrId(posString)));
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
            return new FromExpression(stringExpr);
        }
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }
}
