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
package scratch.newast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.StmtList;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.bool.UnspecifiedBoolExpr;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.statement.Stmt;
import scratch.newast.model.statement.control.IfElseStmt;
import scratch.newast.model.statement.control.IfThenStmt;
import scratch.newast.model.statement.control.RepeatForeverStmt;
import scratch.newast.model.statement.control.RepeatTimesStmt;
import scratch.newast.model.statement.control.UntilStmt;
import scratch.newast.opcodes.ControlStmtOpcode;
import scratch.newast.parser.BoolExprParser;
import scratch.newast.parser.NumExprParser;
import scratch.newast.parser.ScriptParser;

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
        JsonNode conditionNode, substackNode, elseSubstackNode;
        BoolExpr boolExpr;
        StmtList stmtList, elseStmtList;
        JsonNode inputs = current.get(Constants.INPUTS_KEY);
        switch (opcode) {
            case control_if:
                if (inputs.has(INPUT_CONDITION)) {
                    boolExpr = BoolExprParser.parseBoolExpr(current, 1, allBlocks);
                } else {
                    boolExpr = new UnspecifiedBoolExpr();
                }

                substackNode = inputs.get(INPUT_SUBSTACK).get(Constants.POS_INPUT_VALUE);
                stmtList = ScriptParser.parseStmtList(substackNode.asText(), allBlocks);

                stmt = new IfThenStmt(boolExpr, stmtList);
                break;
            case control_if_else:
                if (inputs.has(INPUT_CONDITION)) {
                    boolExpr = BoolExprParser.parseBoolExpr(current, 1, allBlocks); // TODO maybe the position differs if there are no substacks?
                } else {
                    boolExpr = new UnspecifiedBoolExpr();
                }
                substackNode = inputs.get(INPUT_SUBSTACK).get(Constants.POS_INPUT_VALUE);
                stmtList = ScriptParser.parseStmtList(substackNode.asText(), allBlocks);

                elseSubstackNode = inputs.get(INPUT_ELSE_SUBSTACK).get(Constants.POS_INPUT_VALUE);
                elseStmtList = ScriptParser.parseStmtList(elseSubstackNode.asText(), allBlocks);

                stmt = new IfElseStmt(boolExpr, stmtList, elseStmtList); // FIXME
                break;
            case control_repeat:
                NumExpr numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);

                substackNode = inputs.get(INPUT_SUBSTACK).get(Constants.POS_INPUT_VALUE);
                stmtList = ScriptParser.parseStmtList(substackNode.asText(), allBlocks);

                stmt = new RepeatTimesStmt(numExpr, stmtList);
                break;
            case control_repeat_until:
                if (inputs.has(INPUT_CONDITION)) {
                    boolExpr = BoolExprParser.parseBoolExpr(current, 1, allBlocks); // TODO maybe the position differs if there are no substacks?
                } else {
                    boolExpr = new UnspecifiedBoolExpr();
                }
                substackNode = inputs.get(INPUT_SUBSTACK).get(Constants.POS_INPUT_VALUE);
                stmtList = ScriptParser.parseStmtList(substackNode.asText(), allBlocks);
                stmt = new UntilStmt(boolExpr, stmtList);
                break;
            case control_forever:
                substackNode = inputs.get(INPUT_SUBSTACK).get(Constants.POS_INPUT_VALUE);
                stmtList = ScriptParser.parseStmtList(substackNode.asText(), allBlocks);
                stmt = new RepeatForeverStmt(stmtList);
                break;
            default:
                throw new ParsingException("Unknown Opcode " + opcodeString);
        }

        return stmt;
    }
}
