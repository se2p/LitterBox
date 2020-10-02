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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteLookStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.CostumeChoiceParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteLookStmtOpcode.looks_gotofrontback;

public class SpriteLookStmtParser {

    public static SpriteLookStmt parse(String identifier, JsonNode current, JsonNode allBlocks)
            throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(SpriteLookStmtOpcode.contains(opcodeString),
                        "Given blockID does not point to a sprite look block. Opcode is " + opcodeString);

        final SpriteLookStmtOpcode opcode = SpriteLookStmtOpcode.valueOf(opcodeString);
        StringExpr stringExpr;
        NumExpr numExpr;
        BlockMetadata metadata = BlockMetadataParser.parse(identifier, current);
        switch (opcode) {
            case looks_show:
                return new Show(metadata);
            case looks_hide:
                return new Hide(metadata);
            case looks_sayforsecs:
                stringExpr = StringExprParser.parseStringExpr(current, MESSAGE_KEY, allBlocks);
                numExpr = NumExprParser.parseNumExpr(current, SECS_KEY, allBlocks);
                return new SayForSecs(stringExpr, numExpr, metadata);
            case looks_say:
                stringExpr = StringExprParser.parseStringExpr(current, MESSAGE_KEY, allBlocks);
                return new Say(stringExpr, metadata);
            case looks_thinkforsecs:
                stringExpr = StringExprParser.parseStringExpr(current, MESSAGE_KEY, allBlocks);
                numExpr = NumExprParser.parseNumExpr(current, SECS_KEY, allBlocks);
                return new ThinkForSecs(stringExpr, numExpr, metadata);
            case looks_think:
                stringExpr = StringExprParser.parseStringExpr(current, MESSAGE_KEY, allBlocks);
                return new Think(stringExpr, metadata);
            case looks_nextcostume:
                return new NextCostume(metadata);
            case looks_switchcostumeto:
                ElementChoice costumeChoice = CostumeChoiceParser.parse(current, allBlocks);
                return new SwitchCostumeTo(costumeChoice, metadata);
            case looks_changesizeby:
                numExpr = NumExprParser.parseNumExpr(current, CHANGE_KEY, allBlocks);
                return new ChangeSizeBy(numExpr, metadata);
            case looks_setsizeto:
                numExpr = NumExprParser.parseNumExpr(current, SIZE_KEY_CAP, allBlocks);
                return new SetSizeTo(numExpr, metadata);
            case looks_gotofrontback:
                return parseGoToLayer(current, allBlocks, metadata);
            case looks_goforwardbackwardlayers:
                return parseGoForwardBackwardLayer(current, allBlocks, metadata);
            default:
                throw new RuntimeException("Not implemented for opcode " + opcodeString);
        }
    }

    private static SpriteLookStmt parseGoForwardBackwardLayer(JsonNode current, JsonNode allBlocks,
                                                              BlockMetadata metadata)
            throws ParsingException {
        JsonNode front_back = current.get(FIELDS_KEY).get("FORWARD_BACKWARD").get(FIELD_VALUE);

        NumExpr num = NumExprParser.parseNumExpr(current, NUM_KEY, allBlocks);

        String layerOption = front_back.asText();
        return new ChangeLayerBy(num, new ForwardBackwardChoice(layerOption), metadata);
    }

    private static SpriteLookStmt parseGoToLayer(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        Preconditions.checkArgument(current.get(OPCODE_KEY).asText().equals(looks_gotofrontback.toString()));

        JsonNode front_back = current.get(FIELDS_KEY).get("FRONT_BACK").get(FIELD_VALUE);
        String layerOption = front_back.asText();
        try {
            return new GoToLayer(new LayerChoice(layerOption), metadata);
        } catch (IllegalArgumentException e) {
            throw new ParsingException("Unknown LayerChoice label for GoToLayer.");
        }
    }
}
