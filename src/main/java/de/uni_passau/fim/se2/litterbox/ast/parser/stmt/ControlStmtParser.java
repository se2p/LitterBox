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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ControlStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.BoolExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ScriptParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ControlStmtParser {

    public static Stmt parse(String identifier, JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(Constants.OPCODE_KEY).asText();
        final ControlStmtOpcode opcode = ControlStmtOpcode.valueOf(opcodeString);
        final JsonNode inputs = current.get(Constants.INPUTS_KEY);

        BoolExpr boolExpr;
        StmtList stmtList;
        StmtList elseStmtList;
        BlockMetadata metadata = BlockMetadataParser.parse(identifier, current);

        switch (opcode) {
            case control_if:
                stmtList = getSubstackStmtList(allBlocks, inputs, SUBSTACK_KEY);
                boolExpr = getCondition(current, allBlocks, inputs);
                return new IfThenStmt(boolExpr, stmtList, metadata);

            case control_if_else:
                stmtList = getSubstackStmtList(allBlocks, inputs, SUBSTACK_KEY);
                boolExpr = getCondition(current, allBlocks, inputs);
                elseStmtList = getSubstackStmtList(allBlocks, inputs, SUBSTACK2_KEY);
                return new IfElseStmt(boolExpr, stmtList, elseStmtList, metadata);

            case control_repeat:
                NumExpr numExpr = NumExprParser.parseNumExpr(current, TIMES_KEY, allBlocks);
                stmtList = getSubstackStmtList(allBlocks, inputs, SUBSTACK_KEY);
                return new RepeatTimesStmt(numExpr, stmtList, metadata);

            case control_repeat_until:
                stmtList = getSubstackStmtList(allBlocks, inputs, SUBSTACK_KEY);
                boolExpr = getCondition(current, allBlocks, inputs);
                return new UntilStmt(boolExpr, stmtList, metadata);

            case control_forever:
                stmtList = getSubstackStmtList(allBlocks, inputs, SUBSTACK_KEY);
                return new RepeatForeverStmt(stmtList, metadata);

            default:
                throw new ParsingException("Unknown Opcode " + opcodeString);
        }
    }

    private static BoolExpr getCondition(JsonNode current, JsonNode allBlocks, JsonNode inputs)
            throws ParsingException {

        if (inputs.has(CONDITION_KEY)) {
            return BoolExprParser.parseBoolExpr(current, CONDITION_KEY, allBlocks);
        } else {
            return new UnspecifiedBoolExpr();
        }
    }

    private static StmtList getSubstackStmtList(JsonNode allBlocks, JsonNode inputs, String inputSubstack)
            throws ParsingException {
        JsonNode substackNode;

        if (inputs.has(inputSubstack)) {
            substackNode = inputs.get(inputSubstack).get(Constants.POS_INPUT_VALUE);
            return ScriptParser.parseStmtList(substackNode.asText(), allBlocks);
        } else {
            return new StmtList(new ArrayList<Stmt>());
        }
    }
}
