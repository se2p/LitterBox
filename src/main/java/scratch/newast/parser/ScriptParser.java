package scratch.newast.parser;

import static scratch.newast.Constants.NEXT_KEY;
import static scratch.newast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import scratch.newast.ParsingException;
import scratch.newast.model.ActorDefinition;
import scratch.newast.model.Script;
import scratch.newast.model.StmtList;
import scratch.newast.model.event.Event;
import scratch.newast.model.event.Never;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.spritelook.ListOfStmt;
import scratch.newast.model.statement.termination.TerminationStmt;
import scratch.newast.opcodes.EventOpcode;
import scratch.newast.parser.stmt.StmtParser;

public class ScriptParser {

    public static String TOP_LEVEL = "topLevel";
    public static String OPCODE = "opcode";

    /**
     * Returns a script where blockID is the ID of the first block in this script. It is expected that blockID points to
     * a topLevel block.
     *
     * @param blockID of the first block in this script
     * @param blocks  all blocks in the {@link ActorDefinition} of this {@link scratch.newast.model.Script}
     * @return Script that was parsed
     */
    public static Script parse(String blockID, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockID);
        Preconditions.checkNotNull(blocks);

        JsonNode current = blocks.get(blockID);
        Event event;
        StmtList stmtList;

        if (isEvent(current)) {
            event = EventParser.parse(blockID, blocks);
            stmtList = parseStmtList(current.get(NEXT_KEY).asText(), blocks);
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

    public static StmtList parseStmtList(String blockID, JsonNode blocks) throws ParsingException {
        TerminationStmt terminationStmt = null;
        List<Stmt> list = new LinkedList<>();
        JsonNode current = blocks.get(blockID);

        while (current != null && !current.isNull()) {
            try {
                Stmt stmt = StmtParser.parse(blockID, blocks);
                if (stmt instanceof TerminationStmt) {
                    terminationStmt = (TerminationStmt) stmt;
                    if (current.get(NEXT_KEY) == null) {
                        throw new ParsingException(
                            "TerminationStmt found but there still is a next key for block " + blockID);
                    }
                } else {
                    list.add(stmt);
                }
            } catch (ParsingException | RuntimeException e) { // FIXME Runtime Exception is temporary for development and needs to be removed
                Logger.getGlobal().warning("Could not parse block with ID " + blockID + " and opcode "
                    + current.get(OPCODE_KEY));
                Logger.getGlobal().warning(e.getMessage());
                if (e instanceof NullPointerException) {
                    throw e;
                }
            }
            blockID = current.get(NEXT_KEY).asText();
            current = blocks.get(blockID);
        }

        ListOfStmt listOfStmt = new ListOfStmt(list);
        return new StmtList(listOfStmt, terminationStmt);
    }
}
