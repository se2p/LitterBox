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
package ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import ast.Constants;
import ast.ParsingException;
import ast.model.StmtList;
import ast.model.expression.bool.BoolExpr;
import ast.model.expression.bool.UnspecifiedBoolExpr;
import ast.model.expression.num.NumExpr;
import ast.model.statement.Stmt;
import ast.model.statement.control.*;
import ast.model.statement.spritelook.ListOfStmt;
import ast.opcodes.ControlStmtOpcode;
import ast.parser.BoolExprParser;
import ast.parser.NumExprParser;
import ast.parser.ScriptParser;
import utils.Preconditions;

import java.util.ArrayList;

public class ControlStmtParser {

    public static final String INPUT_SUBSTACK = "SUBSTACK";
    public static final String INPUT_ELSE_SUBSTACK = "SUBSTACK2";
    public static final String INPUT_CONDITION = "CONDITION";
    public static final String INPUT_TIMES = "TIMES";

    public static Stmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(Constants.OPCODE_KEY).asText();
        final ControlStmtOpcode opcode = ControlStmtOpcode.valueOf(opcodeString);
        final JsonNode inputs = current.get(Constants.INPUTS_KEY);

        BoolExpr boolExpr;
        StmtList stmtList, elseStmtList;

        switch (opcode) {
            case control_if:
                stmtList = getSubstackStmtList(allBlocks, inputs, INPUT_SUBSTACK);
                boolExpr = getCondition(current, allBlocks, inputs);
                return new IfThenStmt(boolExpr, stmtList);

            case control_if_else:
                stmtList = getSubstackStmtList(allBlocks, inputs, INPUT_SUBSTACK);
                boolExpr = getCondition(current, allBlocks, inputs);
                elseStmtList = getSubstackStmtList(allBlocks, inputs, INPUT_ELSE_SUBSTACK);
                return new IfElseStmt(boolExpr, stmtList, elseStmtList);

            case control_repeat:
                NumExpr numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                stmtList = getSubstackStmtList(allBlocks, inputs, INPUT_SUBSTACK);
                return new RepeatTimesStmt(numExpr, stmtList);

            case control_repeat_until:
                stmtList = getSubstackStmtList(allBlocks, inputs, INPUT_SUBSTACK);
                boolExpr = getCondition(current, allBlocks, inputs);
                return new UntilStmt(boolExpr, stmtList);

            case control_forever:
                stmtList = getSubstackStmtList(allBlocks, inputs, INPUT_SUBSTACK);
                return new RepeatForeverStmt(stmtList);

            default:
                throw new ParsingException("Unknown Opcode " + opcodeString);
        }
    }

    private static BoolExpr getCondition(JsonNode current, JsonNode allBlocks, JsonNode inputs)
        throws ParsingException {

        if (inputs.has(INPUT_CONDITION)) {
            return BoolExprParser.parseBoolExpr(current, INPUT_CONDITION, allBlocks);
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
            return new StmtList(new ListOfStmt(new ArrayList<Stmt>()));
        }
    }
}
