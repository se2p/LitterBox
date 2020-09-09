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
import de.uni_passau.fim.se2.litterbox.ast.model.Key;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class KeyParser {
    public static final int UPARROW = 38;
    public static final int DOWNARROW = 40;
    public static final int RIGHTARROW = 39;
    public static final int LEFTARROW = 37;
    public static final int SPACE = 32;
    public static final int ANYKEY = 0;

    public static Key parse(JsonNode current, JsonNode allBlocks) throws ParsingException {

        JsonNode block;
        final String opcodeString = current.get(OPCODE_KEY).asText();
        BlockMetadata metadata = new NoBlockMetadata();
        if (BoolExprOpcode.sensing_keypressed.name().equals(opcodeString)) {

            List<JsonNode> inputsList = new ArrayList<>();
            current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);
            if (getShadowIndicator((ArrayNode) inputsList.get(0)) == 1) {
                // If there is only the menu in the inputs, we evaluate the menu
                String menuBlockId = current.get(INPUTS_KEY).get(KEY_OPTION).get(POS_INPUT_VALUE).asText();
                block = allBlocks.get(menuBlockId);
                metadata = BlockMetadataParser.parse(menuBlockId, block);
            } else {
                // If there is a variable or expression we evaluate it and use it as key;
                final NumExpr numExpr = NumExprParser.parseNumExpr(current, KEY_OPTION, allBlocks);
                return new Key(numExpr, new NoBlockMetadata());
            }
        } else {
            block = current;
        }
        String keyValue = block.get(FIELDS_KEY).get(KEY_OPTION).get(FIELD_VALUE).asText();
        switch (keyValue) {
            case "space":
                return new Key(new NumberLiteral(SPACE), metadata);
            case "up arrow":
                return new Key(new NumberLiteral(UPARROW), metadata);
            case "down arrow":
                return new Key(new NumberLiteral(DOWNARROW), metadata);
            case "left arrow":
                return new Key(new NumberLiteral(LEFTARROW), metadata);
            case "right arrow":
                return new Key(new NumberLiteral(RIGHTARROW), metadata);
            case "any":
                return new Key(new NumberLiteral(ANYKEY), metadata);
            default:
                if (keyValue.length() > 0) {
                    return new Key(new NumberLiteral(keyValue.charAt(0)), metadata);
                } else {
                    // It is not clear how this can happen, but it happens sometimtes.
                    return new Key(new NumberLiteral(0), metadata);
                }
        }
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }
}
