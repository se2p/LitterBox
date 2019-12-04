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
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.pen.*;
import scratch.ast.opcodes.PenOpcode;
import scratch.ast.parser.ColorParser;
import scratch.ast.parser.NumExprParser;
import scratch.ast.parser.StringExprParser;
import utils.Preconditions;

import static scratch.ast.Constants.*;

public class PenStmtParser {


    private static final String COLOR_PARAM_OPCODE = "pen_menu_colorParam";

    public static Stmt parse(JsonNode current, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);
        final String opCodeString = current.get(Constants.OPCODE_KEY).asText();
        if (!PenOpcode.contains(opCodeString)) {
            throw new ParsingException(
                    "Called parsePenStmt with a block that does not qualify as such"
                            + " a statement. Opcode is " + opCodeString);
        }
        final PenOpcode opcode = PenOpcode.valueOf(opCodeString);
        switch (opcode) {
            case pen_clear:
                return new PenClearStmt();
            case pen_penDown:
                return new PenDownStmt();
            case pen_penUp:
                return new PenUpStmt();
            case pen_stamp:
                return new PenStampStmt();
            case pen_setPenColorToColor:
                return new SetPenColorToColorStmt(ColorParser.parseColor(current, 0, blocks));
            case pen_changePenColorParamBy:
                NumExpr numExpr = NumExprParser.parseNumExpr(current, VALUE_KEY, blocks);
                StringExpr param = parseParam(current, blocks);
                return new ChangePenColorParamBy(numExpr, param);
            case pen_setPenColorParamTo:
                numExpr = NumExprParser.parseNumExpr(current, VALUE_KEY, blocks);
                param = parseParam(current, blocks);
                return new SetPenColorParamTo(numExpr, param);
            default:
                throw new RuntimeException("Not implemented yet for opcode " + opcode);
        }
    }

    private static StringExpr parseParam(JsonNode current, JsonNode blocks) throws ParsingException {
        String reference = current.get(INPUTS_KEY).get(COLOR_PARAM_BIG_KEY).get(POS_INPUT_VALUE).textValue();
        JsonNode referredBlock = blocks.get(reference);
        Preconditions.checkNotNull(referredBlock);
        StringExpr expr;
        if (referredBlock.get(OPCODE_KEY).textValue().equals(COLOR_PARAM_OPCODE)) {
            JsonNode colorParamNode = referredBlock.get(FIELDS_KEY).get(COLOR_PARAM_LITTLE_KEY);
            Preconditions.checkArgument(colorParamNode.isArray());
            String attribute = colorParamNode.get(FIELD_VALUE).textValue();
            expr = new StringLiteral(attribute);
        } else {
            expr = StringExprParser.parseStringExpr(current, COLOR_PARAM_BIG_KEY, blocks);
        }
        return expr;
    }

}
