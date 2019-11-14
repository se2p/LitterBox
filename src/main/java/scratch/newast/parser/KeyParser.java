package scratch.newast.parser;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.FIELD_VALUE;
import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.Constants.POS_INPUT_VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.model.Key;
import scratch.newast.opcodes.BoolExprOpcode;

public class KeyParser {

    public static final String KEY_OPTION = "KEY_OPTION";

    public static Key parse(JsonNode current, JsonNode allBlocks) {

        JsonNode block;
        String opcodeString = current.get(OPCODE_KEY).asText();
        if (BoolExprOpcode.sensing_keypressed.name().equals(opcodeString)) {
            String menuBlockID = current.get(INPUTS_KEY).get(KEY_OPTION).get(POS_INPUT_VALUE).asText();
            block = allBlocks.get(menuBlockID);
        } else {
            block = current;
        }

        String keyValue = block.get(FIELDS_KEY).get(KEY_OPTION).get(FIELD_VALUE).asText();
        return new Key(keyValue);
    }

}
