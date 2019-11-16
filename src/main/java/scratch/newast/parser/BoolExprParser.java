package scratch.newast.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.Key;
import scratch.newast.model.color.Color;
import scratch.newast.model.expression.bool.And;
import scratch.newast.model.expression.bool.BiggerThan;
import scratch.newast.model.expression.bool.Bool;
import scratch.newast.model.expression.bool.BoolExpr;
import scratch.newast.model.expression.bool.ColorTouches;
import scratch.newast.model.expression.bool.Equals;
import scratch.newast.model.expression.bool.IsKeyPressed;
import scratch.newast.model.expression.bool.IsMouseDown;
import scratch.newast.model.expression.bool.LessThan;
import scratch.newast.model.expression.bool.Not;
import scratch.newast.model.expression.bool.Or;
import scratch.newast.model.expression.bool.Touching;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.touchable.Touchable;
import scratch.newast.model.variable.Identifier;
import scratch.newast.model.variable.Qualified;
import scratch.newast.opcodes.BoolExprOpcode;
import scratch.newast.parser.symboltable.ExpressionListInfo;
import scratch.newast.parser.symboltable.VariableInfo;

import static scratch.newast.Constants.FIELDS_KEY;
import static scratch.newast.Constants.INPUTS_KEY;
import static scratch.newast.Constants.OPCODE_KEY;
import static scratch.newast.Constants.POS_BLOCK_ID;
import static scratch.newast.Constants.POS_DATA_ARRAY;
import static scratch.newast.Constants.POS_INPUT_ID;
import static scratch.newast.Constants.POS_INPUT_VALUE;

public class BoolExprParser {

    public static BoolExpr parseBoolExpr(JsonNode block, int pos, JsonNode blocks) throws ParsingException {
        ArrayNode exprArray = ExpressionParser.getExprArrayAtPos(block.get(INPUTS_KEY), pos);
        if (ExpressionParser.getShadowIndicator(exprArray) == 1) {
            return parseBool(block.get(INPUTS_KEY), pos);
        } else if (exprArray.get(POS_BLOCK_ID) instanceof TextNode) {
            String identifier = exprArray.get(POS_BLOCK_ID).asText();
            String opcode = blocks.get(identifier).get(OPCODE_KEY).asText();
            return parseBlockBoolExpr(opcode, identifier, blocks, block.get(FIELDS_KEY));
        } else {
            String idString = exprArray.get(POS_DATA_ARRAY).get(POS_INPUT_ID).asText();
            if (ProgramParser.symbolTable.getVariables().containsKey(idString)) {
                VariableInfo variableInfo = ProgramParser.symbolTable.getVariables().get(idString);

                return new Qualified(new Identifier(variableInfo.getActor()),
                        new Identifier((variableInfo.getVariableName())));

            } else if (ProgramParser.symbolTable.getLists().containsKey(idString)) {
                ExpressionListInfo variableInfo = ProgramParser.symbolTable.getLists().get(idString);
                return new Qualified(new Identifier(variableInfo.getActor()),
                        new Identifier((variableInfo.getVariableName())));
            }
        }

        throw new ParsingException("Could not parse BoolExpr");
    }

    private static Bool parseBool(JsonNode inputs, int pos) {
        boolean value = ExpressionParser.getDataArrayAtPos(inputs, pos).get(POS_INPUT_VALUE).asBoolean();
        return new Bool(value);
    }

    static BoolExpr parseBlockBoolExpr(String opcodeString, String identifier, JsonNode blocks, JsonNode fields)
            throws ParsingException {
        Preconditions
                .checkArgument(BoolExprOpcode.contains(opcodeString), opcodeString + " is not a BoolExprOpcode.");
        BoolExprOpcode opcode = BoolExprOpcode.valueOf(opcodeString);
        switch (opcode) {
        case sensing_touchingcolor:
        case sensing_touchingobject:
            Touchable touchable = TouchableParser.parseTouchable(blocks.get(identifier), blocks);
            return new Touching(touchable);
        case sensing_coloristouchingcolor:
            Color first = ColorParser.parseColor(blocks.get(identifier), 0, blocks);
            Color second = ColorParser.parseColor(blocks.get(identifier), 1, blocks);
            return new ColorTouches(first, second);
        case sensing_keypressed:
            Key key = KeyParser.parse(blocks.get(identifier), blocks);
            return new IsKeyPressed(key);
        case sensing_mousedown:
            return new IsMouseDown();
        case operator_gt:
            NumExpr firstNum = NumExprParser.parseNumExpr(blocks.get(identifier), 0, blocks);
            NumExpr secondNum = NumExprParser.parseNumExpr(blocks.get(identifier), 1, blocks);
            return new BiggerThan(firstNum, secondNum);
        case operator_lt:
            NumExpr lessFirst = NumExprParser.parseNumExpr(blocks.get(identifier), 0, blocks);
            NumExpr lessSecond = NumExprParser.parseNumExpr(blocks.get(identifier), 1, blocks);
            return new LessThan(lessFirst, lessSecond);
        case operator_equals:
            NumExpr eqFirst = NumExprParser.parseNumExpr(blocks.get(identifier), 0, blocks);
            NumExpr eqSecond = NumExprParser.parseNumExpr(blocks.get(identifier), 1, blocks);
            return new Equals(eqFirst, eqSecond);
        case operator_and:
            BoolExpr andFirst = parseBoolExpr(blocks.get(identifier), 0, blocks);
            BoolExpr andSecond = parseBoolExpr(blocks.get(identifier), 1, blocks);
            return new And(andFirst, andSecond);
        case operator_or:
            BoolExpr orFirst = parseBoolExpr(blocks.get(identifier), 0, blocks);
            BoolExpr orSecond = parseBoolExpr(blocks.get(identifier), 1, blocks);
            return new Or(orFirst, orSecond);
        case operator_not:
            BoolExpr notInput = parseBoolExpr(blocks.get(identifier), 0, blocks);
            return new Not(notInput);
        case operator_contains:
        case data_listcontainsitem:
            throw new RuntimeException("Not implemented yet"); // I don't know which Classes should be returned here
        default:
            throw new RuntimeException(
                    opcodeString + " is not covered by parseBlockExpr");
        }
    }
}
