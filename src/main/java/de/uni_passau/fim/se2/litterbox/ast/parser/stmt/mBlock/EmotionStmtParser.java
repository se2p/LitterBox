package de.uni_passau.fim.se2.litterbox.ast.parser.stmt.mBlock;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.emotion.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock.EmotionStmtOpcode;
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
        switch (opcode) {
            case emotion_look_up:
                return new LookUp(metadata);

            case emotion_look_down:
                return new LookDown(metadata);

            case emotion_look_left:
                return new LookLeft(metadata);

            case emotion_look_right:
                return new LookRight(metadata);

            case emotion_look_around:
                return new LookAround(metadata);

            case emotion_wink:
                return new Wink(metadata);

            case emotion_smile:
                return new Smile(metadata);

            case emotion_yeah:
                return new Yeah(metadata);

            case emotion_naughty:
                return new Naughty(metadata);

            case emotion_proud:
                return new Proud(metadata);

            case emotion_coquetry:
                return new Coquetry(metadata);

            case emotion_awkward:
                return new Awkward(metadata);

            case emotion_exclaim:
                return new Exclaim(metadata);

            case emotion_aggrieved:
                return new Aggrieved(metadata);

            case emotion_sad:
                return new Sad(metadata);

            case emotion_angry:
                return new Angry(metadata);

            case emotion_greeting:
                return new Greeting(metadata);

            case emotion_sprint:
                return new Sprint(metadata);

            case emotion_startle:
                return new Startle(metadata);

            case emotion_shiver:
                return new Shiver(metadata);

            case emotion_dizzy:
                return new Dizzy(metadata);

            case emotion_sleepy:
                return new Sleepy(metadata);

            case emotion_sleeping:
                return new Sleeping(metadata);

            case emotion_revive:
                return new Revive(metadata);

            case emotion_agree:
                return new Agree(metadata);

            case emotion_deny:
                return new Deny(metadata);

            default:
                throw new IllegalStateException("EmotionStmtBlock with opcode " + opcode + " was not parsed");
        }
    }
}
