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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.AsString;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.FromExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteMotionStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteMotionStmtOpcode.*;

public class PositionParser {

    public static Position parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        if (current.get(Constants.INPUTS_KEY).has(TO_KEY)
                || current.get(Constants.INPUTS_KEY).has(TOWARDS_KEY)
                || current.get(Constants.INPUTS_KEY).has(DISTANCETOMENU_KEY)) {
            return parseRelativePos(current, allBlocks);
        } else {
            throw new ParsingException("Could not parse block " + current.toString());
        }
    }

    private static Position parseRelativePos(JsonNode current, JsonNode allBlocks) throws ParsingException {
        JsonNode inputsArray = current.get(INPUTS_KEY);

        JsonNode menuID;
        String opcodeString = current.get(Constants.OPCODE_KEY).asText();
        String positionInputKey;
        if (SpriteMotionStmtOpcode.contains(opcodeString)) {
            SpriteMotionStmtOpcode opcode = valueOf(opcodeString);

            if (motion_goto.equals(opcode) || motion_glideto.equals(opcode)) {
                positionInputKey = TO_KEY;
                menuID = inputsArray.get(positionInputKey).get(POS_INPUT_VALUE);
            } else if (motion_pointtowards.equals(opcode)) {
                positionInputKey = TOWARDS_KEY;
                menuID = inputsArray.get(positionInputKey).get(POS_INPUT_VALUE);
            } else {
                throw new ParsingException(
                        "Cannot parse relative coordinates for a block with opcode "
                                + current.get(Constants.OPCODE_KEY));
            }
        } else if (NumExprOpcode.sensing_distanceto.toString().equals(opcodeString)) {
            positionInputKey = DISTANCETOMENU_KEY;
            menuID = inputsArray.get(positionInputKey).get(POS_INPUT_VALUE);
        } else {
            throw new ParsingException(
                    "Cannot parse relative coordinates for a block with opcode " + current.get(Constants.OPCODE_KEY));
        }

        if (getShadowIndicator((ArrayNode) inputsArray.get(positionInputKey)) == 1) {
            ArrayList<JsonNode> fields = new ArrayList<>();
            allBlocks.get(menuID.asText()).get(Constants.FIELDS_KEY).elements().forEachRemaining(fields::add);
            String posString = fields.get(Constants.FIELD_VALUE).get(0).asText();
            BlockMetadata metadata = BlockMetadataParser.parse(menuID.asText(), allBlocks.get(menuID.asText()));

            if (posString.equals(MOUSE)) {
                return new MousePos(metadata);
            } else if (posString.equals(RANDOM)) {
                return new RandomPos(metadata);
            } else {
                return new FromExpression(new AsString(new StrId(posString)), metadata);
            }
        } else {
            String posName = "";
            if (motion_goto.toString().equals(opcodeString) || motion_glideto.toString().equals(opcodeString)) {
                posName = TO_KEY;
            } else if (motion_pointtowards.toString().equals(opcodeString)) {
                posName = TOWARDS_KEY;
            } else if (NumExprOpcode.sensing_distanceto.toString().equals(opcodeString)) {
                posName = DISTANCETOMENU_KEY;
            }

            final StringExpr stringExpr = StringExprParser.parseStringExpr(current, posName, allBlocks);
            return new FromExpression(stringExpr, new NoBlockMetadata());
        }
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }
}
