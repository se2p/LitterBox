package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.newast.model.Script;
import scratch.newast.model.StmtList;
import scratch.newast.model.event.Event;
import scratch.newast.model.event.Never;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.spritelook.ListOfStmt;
import scratch.newast.model.statement.termination.TerminationStmt;
import scratch.newast.opcodes.EventOpcode;

import java.util.LinkedList;
import java.util.List;

import static scratch.newast.Constants.NEXT_KEY;

public class ScriptParser {

    public static String TOP_LEVEL = "topLevel";
    public static String OPCODE = "opcode";

    /**
     * Returns a script where blockID is the ID of the first block in this script.
     * It is expected that blockID points to a topLevel block.
     *
     * @param blockID of the first block in this script
     * @param blocks  all blocks in the {@link scratch.newast.model.ScriptGroup} of this {@link scratch.newast.model.Script}
     * @return Script that was parsed
     */
    public static Script parse(String blockID, JsonNode blocks) {

        JsonNode current = blocks.get(blockID);
        Event event;
        StmtList stmtList;

        if (isEvent(current)) {
            event = EventParser.parse(blockID, blocks);
            stmtList = parseStmtList(current.get("next").asText(), blocks);
        } else {
            event = new Never();
            stmtList = parseStmtList(blockID, blocks);
        }

        return new Script(event, stmtList);
    }

    private static boolean isEvent(JsonNode current) {
        String opcode = current.get(OPCODE).asText();
        return EventOpcode.contains(opcode);
    }

    private static StmtList parseStmtList(String blockID, JsonNode blocks) {
        TerminationStmt terminationStmt = null;
        List<Stmt> list = new LinkedList<>();
        JsonNode current = blocks.get(blockID);

        while (!current.isNull()) {
            Stmt stmt = StmtParser.parse(current, blocks);
            if (stmt instanceof TerminationStmt) {
                terminationStmt = (TerminationStmt) stmt;
                if (!current.get(NEXT_KEY).isNull()) { //TODO check if this is the correct way to check this
                    throw new RuntimeException("TerminationStmt found but there still is a next key");
                }
            } else {
                list.add(stmt);
            }
            current = current.get(NEXT_KEY);
        }

        ListOfStmt listOfStmt = new ListOfStmt(list);
        return new StmtList(listOfStmt, terminationStmt);
    }
}
