/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.DependentBlockOpcodes;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.EventOpcode;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.stmt.StmtParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.NEXT_KEY;
import static de.uni_passau.fim.se2.litterbox.ast.Constants.OPCODE_KEY;

public class ScriptParser {

    public static final String TOP_LEVEL = "topLevel";
    public static final String OPCODE = "opcode";

    /**
     * Returns a script where blockID is the ID of the first block in this script. It is expected that blockID points to
     * a topLevel block.
     *
     * @param blockID of the first block in this script
     * @param blocks  all blocks in the {@link ActorDefinition} of this
     *                {@link de.uni_passau.fim.se2.litterbox.ast.model.Script}
     * @return Script that was parsed
     */
    public static Script parse(String blockID, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockID);
        Preconditions.checkNotNull(blocks);

        final JsonNode current = blocks.get(blockID);
        final Event event;
        final StmtList stmtList;

        if (isEvent(current)) {
            event = EventParser.parse(blockID, blocks);
            stmtList = parseStmtList(current.get(NEXT_KEY).asText(), blocks);
        } else {
            event = new Never();
            stmtList = parseStmtList(blockID, blocks);
            if (stmtList == null) {
                return null;
            }
        }

        return new Script(event, stmtList);
    }

    private static boolean isEvent(JsonNode current) {
        if (current.has(OPCODE)) {
            String opcode = current.get(OPCODE).asText();
            return EventOpcode.contains(opcode);
        }
        return false;
    }

    public static StmtList parseStmtList(String blockId, JsonNode blocks) throws ParsingException {
        List<Stmt> list = new LinkedList<>();
        JsonNode current = blocks.get(blockId);

        if (current instanceof ArrayNode) {
            Stmt stmt = StmtParser.parse(blockId, blocks);
            list.add(stmt);
        } else {

            while (current != null && !current.isNull()) {
                try {
                    if (ProcedureOpcode.contains(blocks.get(blockId).get(OPCODE_KEY).asText())
                            || DependentBlockOpcodes.contains(blocks.get(blockId).get(OPCODE_KEY).asText())) {
                        //Ignore ProcedureOpcodes
                        //                    blockId = current.get(NEXT_KEY).asText();
                        //                    current = blocks.get(blockId);
                        return null;
                    }

                    Stmt stmt = StmtParser.parse(blockId, blocks);
                    list.add(stmt);
                } catch (ParsingException e) {
                    // and needs to be removed
                    Logger.getGlobal().warning("Could not parse block with ID " + blockId + " and opcode "
                            + current.get(OPCODE_KEY));
                }
                blockId = current.get(NEXT_KEY).asText();
                current = blocks.get(blockId);
            }
        }
        return new StmtList(list);
    }
}
