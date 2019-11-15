package scratch.newast.parser.stmt;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.FIELD_VALUE;
import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.model.elementchoice.ElementChoice;
import scratch.newast.model.elementchoice.WithId;
import scratch.newast.model.statement.actorsound.ActorSoundStmt;
import scratch.newast.model.statement.actorsound.ClearSoundEffects;
import scratch.newast.model.statement.actorsound.PlaySoundUntilDone;
import scratch.newast.model.statement.actorsound.StartSound;
import scratch.newast.model.statement.actorsound.StopAllSounds;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.ActorSoundStmtOpcode;

public class ActorSoundStmtParser {

    private static final String SOUND_MENU = "SOUND_MENU";

    public static ActorSoundStmt parse(JsonNode current, JsonNode allBlocks) {
        String opCodeString = current.get(OPCODE_KEY).asText();

        Preconditions.checkArgument(ActorSoundStmtOpcode.contains(opCodeString), "Given block is not an "
            + "ActorStmtBlock");

        ActorSoundStmtOpcode opcode = ActorSoundStmtOpcode.valueOf(opCodeString);
        ActorSoundStmt stmt;
        JsonNode soundMenu;
        String soundValue;
        String soundMenuId;
        ElementChoice elementChoice;

        switch (opcode) {
            case sound_playuntildone:
                soundMenuId = current.get(INPUTS_KEY).get(SOUND_MENU).get(Constants.POS_INPUT_VALUE).asText();
                soundMenu = allBlocks.get(soundMenuId);
                soundValue = soundMenu.get(FIELDS_KEY).get(SOUND_MENU).get(FIELD_VALUE).asText();
                elementChoice = new WithId(new Identifier(soundValue));
                stmt = new PlaySoundUntilDone(elementChoice);
                break;
            case sound_play:
                soundMenuId = current.get(INPUTS_KEY).get(SOUND_MENU).get(Constants.POS_INPUT_VALUE).asText();
                soundMenu = allBlocks.get(soundMenuId);
                soundValue = soundMenu.get(FIELDS_KEY).get(SOUND_MENU).get(FIELD_VALUE).asText();
                elementChoice = new WithId(new Identifier(soundValue));
                stmt = new StartSound(elementChoice);
                break;
            case sound_cleareffects:
                stmt = new ClearSoundEffects();
                break;
            case sound_stopallsounds:
                stmt = new StopAllSounds();
                break;
            default:
                throw new RuntimeException("Not implemented yet for opcode " + opCodeString);
        }

        return stmt;
    }

}
