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
package scratch.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.StmtList;
import scratch.ast.model.expression.bool.BoolExpr;
import scratch.ast.model.expression.bool.UnspecifiedBoolExpr;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.control.IfElseStmt;
import scratch.ast.model.statement.control.IfThenStmt;
import scratch.ast.model.statement.control.RepeatForeverStmt;
import scratch.ast.model.statement.control.RepeatTimesStmt;
import scratch.ast.model.statement.control.UntilStmt;
import scratch.ast.model.statement.spritelook.ListOfStmt;
import scratch.ast.opcodes.ControlStmtOpcode;
import scratch.ast.parser.BoolExprParser;
import scratch.ast.parser.NumExprParser;
import scratch.ast.parser.ScriptParser;

public class ControlStmtParser {

    public static final String INPUT_SUBSTACK = "SUBSTACK";
    public static final String INPUT_ELSE_SUBSTACK = "SUBSTACK2";
    public static final String INPUT_CONDITION = "CONDITION";
    public static final String INPUT_TIMES = "TIMES";

    public static Stmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(Constants.OPCODE_KEY).asText();
        ControlStmtOpcode opcode = ControlStmtOpcode.valueOf(opcodeString);

        Stmt stmt;
        BoolExpr boolExpr;
        StmtList stmtList, elseStmtList;
        JsonNode inputs = current.get(Constants.INPUTS_KEY);
        switch (opcode) {
            case control_if:
                stmtList = getSubstackStmtList(allBlocks, inputs, INPUT_SUBSTACK);
                boolExpr = getCondition(current, allBlocks, inputs);
                stmt = new IfThenStmt(boolExpr, stmtList);
                break;
            case control_if_else:
                stmtList = getSubstackStmtList(allBlocks, inputs, INPUT_SUBSTACK);
                boolExpr = getCondition(current, allBlocks, inputs);
                elseStmtList = getSubstackStmtList(allBlocks, inputs, INPUT_ELSE_SUBSTACK);
                stmt = new IfElseStmt(boolExpr, stmtList, elseStmtList);
                break;
            case control_repeat:
                NumExpr numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                stmtList = getSubstackStmtList(allBlocks, inputs, INPUT_SUBSTACK);
                stmt = new RepeatTimesStmt(numExpr, stmtList);
                break;
            case control_repeat_until:
                stmtList = getSubstackStmtList(allBlocks, inputs, INPUT_SUBSTACK);
                boolExpr = getCondition(current, allBlocks, inputs);
                stmt = new UntilStmt(boolExpr, stmtList);
                break;
            case control_forever:
                stmtList = getSubstackStmtList(allBlocks, inputs, INPUT_SUBSTACK);
                stmt = new RepeatForeverStmt(stmtList);
                break;
            default:
                throw new ParsingException("Unknown Opcode " + opcodeString);
        }

        return stmt;
    }

    private static BoolExpr getCondition(JsonNode current, JsonNode allBlocks, JsonNode inputs)
        throws ParsingException {
        BoolExpr boolExpr;
        if (inputs.has(INPUT_CONDITION)) {
            boolExpr = BoolExprParser.parseBoolExpr(current, INPUT_CONDITION, allBlocks);
        } else {
            boolExpr = new UnspecifiedBoolExpr();
        }
        return boolExpr;
    }

    private static StmtList getSubstackStmtList(JsonNode allBlocks, JsonNode inputs, String inputSubstack)
        throws ParsingException {
        JsonNode substackNode;
        StmtList stmtList;
        if (inputs.has(inputSubstack)) {
            substackNode = inputs.get(inputSubstack).get(Constants.POS_INPUT_VALUE);
            stmtList = ScriptParser.parseStmtList(substackNode.asText(), allBlocks);
        } else {
            stmtList = new StmtList(new ListOfStmt(new ArrayList<Stmt>()), null);
        }
        return stmtList;
    }
}
