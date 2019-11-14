package scratch.newast.parser;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.model.touchable.Edge;
import scratch.newast.model.touchable.MousePointer;
import scratch.newast.model.touchable.Touchable;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.BoolExprOpcode;

public class TouchableParser {

    public static Touchable parseTouchable(JsonNode current, JsonNode allBlocks) {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);
        String opcodeString = current.get(OPCODE_KEY).asText();

        if (BoolExprOpcode.sensing_touchingobject.name().equals(opcodeString)) {
            String menuID = current.get(INPUTS_KEY).get(0).get(1).asText();
            String touchingobject = allBlocks.get(menuID).get(FIELDS_KEY).get(0).get(0).asText();

            if (touchingobject.equals("_mouse_")) {
                return new MousePointer();
            } else if (touchingobject.equals("_edge_")) {
                return new Edge();
            } else {
                return new Identifier(touchingobject);
            }

        } else if (BoolExprOpcode.sensing_touchingcolor.name().equals(opcodeString)) {
            return ColorParser.parseColor(current, 0, allBlocks);
        } else {
            throw new RuntimeException("Not implemented yet");
        }
    }
}
