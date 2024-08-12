/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt.mblock;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.EmotionStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.OPCODE_KEY;

public class EmotionStmtParser {

    public static EmotionStmt parse(String blockId, JsonNode current, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(EmotionStmtOpcode.contains(opcodeString), "Given blockId does not point to an emotion block.");

        EmotionStmtOpcode opcode = EmotionStmtOpcode.getOpcode(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        return switch (opcode) {
            case emotion_look_up -> new LookUp(metadata);
            case emotion_look_down -> new LookDown(metadata);
            case emotion_look_left -> new LookLeft(metadata);
            case emotion_look_right -> new LookRight(metadata);
            case emotion_look_around -> new LookAround(metadata);
            case emotion_wink -> new Wink(metadata);
            case emotion_smile -> new Smile(metadata);
            case emotion_yeah -> new Yeah(metadata);
            case emotion_naughty -> new Naughty(metadata);
            case emotion_proud -> new Proud(metadata);
            case emotion_coquetry -> new Coquetry(metadata);
            case emotion_awkward -> new Awkward(metadata);
            case emotion_exclaim -> new Exclaim(metadata);
            case emotion_aggrieved -> new Aggrieved(metadata);
            case emotion_sad -> new Sad(metadata);
            case emotion_angry -> new Angry(metadata);
            case emotion_greeting -> new Greeting(metadata);
            case emotion_sprint -> new Sprint(metadata);
            case emotion_startle -> new Startle(metadata);
            case emotion_shiver -> new Shiver(metadata);
            case emotion_dizzy -> new Dizzy(metadata);
            case emotion_sleepy -> new Sleepy(metadata);
            case emotion_sleeping -> new Sleeping(metadata);
            case emotion_revive -> new Revive(metadata);
            case emotion_agree -> new Agree(metadata);
            case emotion_deny -> new Deny(metadata);
        };
    }
}
