package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.data.OpCodeMapping;
import scratch.newast.model.Script;
import scratch.newast.model.StmtList;
import scratch.newast.model.event.Event;
import scratch.newast.model.event.Never;

public class ScriptParser {

    public static String TOP_LEVEL = "topLevel";
    public static String OPCODE = "opcode";

    /**
     * Returns a script where blockID is the ID of the first block in this script.
     * It is expected that blockID points to a topLevel block.
     *
     * @param blockID of the first block in this script
     * @param blocks  all blocks in the {@link scratch.newast.model.ScriptGroup} of this {@link scratch.newast.model.Script}
     * @return
     */
    public static Script parse(String blockID, JsonNode blocks) {

        JsonNode current = blocks.get(blockID);
        Event event = null;
        StmtList stmtList = null;

        if (isEvent(current)) {
            event = parseEvent(current, blocks);
            stmtList = parseStmtList(current.get("next"), blocks);
        } else {
            event = new Never();
            stmtList = parseStmtList(current, blocks);
        }

        return new Script(event, stmtList);
    }

    private static boolean isEvent(JsonNode current) {
        String opcode = current.get(OPCODE).asText();
        return OpCodeMapping.hatBlockOpcodes.contains(opcode); //TODO Check if all these are events. Watchout for self defined procedures
    }

    private static StmtList parseStmtList(JsonNode current, JsonNode blocks) {
        return null;
    }

    private static Event parseEvent(JsonNode current, JsonNode blocks) {
        return null;
    }
}
