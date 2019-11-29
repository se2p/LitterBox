package scratch.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import scratch.ast.Constants;
import scratch.ast.ParsingException;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.expression.string.StringExpr;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.pen.*;
import scratch.ast.opcodes.DependentBlockOpcodes;
import scratch.ast.opcodes.PenOpcode;
import scratch.ast.parser.ColorParser;
import scratch.ast.parser.NumExprParser;
import scratch.ast.parser.StringExprParser;
import utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

import static scratch.ast.Constants.*;

public class PenStmtParser {

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
        List<JsonNode> inputsList = new ArrayList<>();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputsList::add);

        StringExpr expr;
        if (getShadowIndicator((ArrayNode) inputsList.get(0).get(POS_DATA_ARRAY)) == 1) {
            String reference = current.get(INPUTS_KEY).get(COLOR_PARAM_BIG_KEY).get(POS_INPUT_VALUE).textValue();
            JsonNode referredBlock = blocks.get(reference);
            Preconditions.checkNotNull(referredBlock);
            if (referredBlock.get(OPCODE_KEY).textValue().equals(DependentBlockOpcodes.pen_menu_colorParam.name())) {
                JsonNode colorParamNode = referredBlock.get(FIELDS_KEY).get(COLOR_PARAM_LITTLE_KEY);
                Preconditions.checkArgument(colorParamNode.isArray());
                String attribute = colorParamNode.get(FIELD_VALUE).textValue();
                expr = new StringLiteral(attribute);
            } else {
                expr = StringExprParser.parseStringExpr(current, COLOR_PARAM_BIG_KEY, blocks);
            }
        } else {
            expr = StringExprParser.parseStringExpr(current, 0, blocks);
        }

        return expr;
    }

    static int getShadowIndicator(ArrayNode exprArray) {
        return exprArray.get(Constants.POS_INPUT_SHADOW).asInt();
    }

}
