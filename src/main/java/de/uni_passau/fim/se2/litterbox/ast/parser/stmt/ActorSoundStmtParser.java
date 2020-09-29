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
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ActorSoundStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ExpressionParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ActorSoundStmtParser {

    public static ActorSoundStmt parse(String blockId, JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opCodeString = current.get(OPCODE_KEY).asText();

        Preconditions.checkArgument(ActorSoundStmtOpcode.contains(opCodeString), "Given block is not an "
                + "ActorStmtBlock");

        ElementChoice elementChoice;
        final ActorSoundStmtOpcode opcode = ActorSoundStmtOpcode.valueOf(opCodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case sound_playuntildone:
                elementChoice = getSoundElement(current, allBlocks);
                return new PlaySoundUntilDone(elementChoice, metadata);

            case sound_play:
                elementChoice = getSoundElement(current, allBlocks);
                return new StartSound(elementChoice, metadata);

            case sound_cleareffects:
                return new ClearSoundEffects(metadata);

            case sound_stopallsounds:
                return new StopAllSounds(metadata);

            case sound_setvolumeto:
                return parseSetVolumeTo(current, allBlocks, metadata);
            case sound_seteffectto:
                return parseSetSoundEffect(current, allBlocks, metadata);
            case sound_changevolumeby:
                NumExpr numExpr = NumExprParser.parseNumExpr(current, VOLUME_KEY_CAPS,
                        allBlocks);
                return new ChangeVolumeBy(numExpr, metadata);
            case sound_changeeffectby:
                numExpr = NumExprParser.parseNumExpr(current, VALUE_KEY, allBlocks);
                String effectName = current.get(FIELDS_KEY).get("EFFECT").get(0).asText();
                return new ChangeSoundEffectBy(new SoundEffect(effectName), numExpr, metadata);

            default:
                throw new RuntimeException("Not implemented yet for opcode " + opCodeString);
        }
    }

    private static ElementChoice getSoundElement(JsonNode current, JsonNode allBlocks) throws ParsingException {
        //Make a list of all elements in inputs
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        if (getShadowIndicator((ArrayNode) inputsList.get(0)) == 1) {
            String soundMenuId = current.get(INPUTS_KEY).get(SOUND_MENU).get(Constants.POS_INPUT_VALUE).asText();
            JsonNode soundMenu = allBlocks.get(soundMenuId);
            String soundValue = soundMenu.get(FIELDS_KEY).get(SOUND_MENU).get(FIELD_VALUE).asText();
            BlockMetadata metadata = BlockMetadataParser.parse(soundMenuId, soundMenu);

            return new WithExpr(new StrId(soundValue), metadata);
        } else {
            final Expression expression = ExpressionParser.parseExpr(current, SOUND_MENU, allBlocks);
            return new WithExpr(expression, new NoBlockMetadata());
        }
    }

    private static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }

    private static ActorSoundStmt parseSetVolumeTo(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        return new SetVolumeTo(NumExprParser.parseNumExpr(current, VOLUME_KEY_CAPS, allBlocks), metadata);
    }

    private static ActorSoundStmt parseSetSoundEffect(JsonNode current, JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        String effect = current.get(FIELDS_KEY).get(EFFECT_KEY).get(0).asText();
        Preconditions.checkArgument(SoundEffect.SoundEffectType.contains(effect));
        return new SetSoundEffectTo(new SoundEffect(effect), NumExprParser.parseNumExpr(current, VALUE_KEY,
                allBlocks), metadata);
    }
}
