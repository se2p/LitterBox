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
package scratch.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.ast.Constants;
import scratch.ast.model.elementchoice.ElementChoice;
import scratch.ast.model.elementchoice.WithId;
import scratch.ast.model.statement.actorsound.*;
import scratch.ast.model.variable.StrId;
import scratch.ast.opcodes.ActorSoundStmtOpcode;
import scratch.utils.Preconditions;

import static scratch.ast.Constants.*;

public class ActorSoundStmtParser {

    private static final String SOUND_MENU = "SOUND_MENU";

    public static ActorSoundStmt parse(JsonNode current, JsonNode allBlocks) {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opCodeString = current.get(OPCODE_KEY).asText();

        Preconditions.checkArgument(ActorSoundStmtOpcode.contains(opCodeString), "Given block is not an "
            + "ActorStmtBlock");

        JsonNode soundMenu;
        String soundValue;
        String soundMenuId;
        ElementChoice elementChoice;

        final ActorSoundStmtOpcode opcode = ActorSoundStmtOpcode.valueOf(opCodeString);

        switch (opcode) {
            case sound_playuntildone:
                soundMenuId = current.get(INPUTS_KEY).get(SOUND_MENU).get(Constants.POS_INPUT_VALUE).asText();
                soundMenu = allBlocks.get(soundMenuId);
                soundValue = soundMenu.get(FIELDS_KEY).get(SOUND_MENU).get(FIELD_VALUE).asText();
                elementChoice = new WithId(new StrId(soundValue));
                return new PlaySoundUntilDone(elementChoice);

            case sound_play:
                soundMenuId = current.get(INPUTS_KEY).get(SOUND_MENU).get(Constants.POS_INPUT_VALUE).asText();
                soundMenu = allBlocks.get(soundMenuId);
                soundValue = soundMenu.get(FIELDS_KEY).get(SOUND_MENU).get(FIELD_VALUE).asText();
                elementChoice = new WithId(new StrId(soundValue));
                return new StartSound(elementChoice);

            case sound_cleareffects:
                return new ClearSoundEffects();

            case sound_stopallsounds:
                return new StopAllSounds();

            default:
                throw new RuntimeException("Not implemented yet for opcode " + opCodeString);
        }
    }

}
