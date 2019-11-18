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
package scratch.ast.parser;

import static scratch.ast.Constants.NEXT_KEY;
import static scratch.ast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import scratch.ast.ParsingException;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Script;
import scratch.ast.model.StmtList;
import scratch.ast.model.event.Event;
import scratch.ast.model.event.Never;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.spritelook.ListOfStmt;
import scratch.ast.model.statement.termination.TerminationStmt;
import scratch.ast.opcodes.EventOpcode;
import scratch.ast.parser.stmt.StmtParser;

public class ScriptParser {

    public static String TOP_LEVEL = "topLevel";
    public static String OPCODE = "opcode";

    /**
     * Returns a script where blockID is the ID of the first block in this script. It is expected that blockID points to
     * a topLevel block.
     *
     * @param blockID of the first block in this script
     * @param blocks  all blocks in the {@link ActorDefinition} of this {@link scratch.ast.model.Script}
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
                e.printStackTrace();
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
