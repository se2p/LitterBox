package scratch.newast.parser;

import static scratch.newast.opcodes.SpriteMotionStmtOpcode.motion_glidesecstoxy;
import static scratch.newast.opcodes.SpriteMotionStmtOpcode.motion_glideto;
import static scratch.newast.opcodes.SpriteMotionStmtOpcode.motion_goto;
import static scratch.newast.opcodes.SpriteMotionStmtOpcode.motion_gotoxy;
import static scratch.newast.opcodes.SpriteMotionStmtOpcode.motion_pointtowards;
import static scratch.newast.opcodes.SpriteMotionStmtOpcode.valueOf;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import scratch.newast.Constants;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.position.CoordinatePosition;
import scratch.newast.model.position.MousePos;
import scratch.newast.model.position.PivotOf;
import scratch.newast.model.position.Position;
import scratch.newast.model.position.RandomPos;
import scratch.newast.model.variable.Identifier;
import scratch.newast.opcodes.SpriteMotionStmtOpcode;

public class PositionParser {

    public static Position parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        if (current.get(Constants.INPUTS_KEY).has("X") && current.get(Constants.INPUTS_KEY).has("Y")) {
            return parseCoordinate(current, allBlocks);
        } else if (current.get(Constants.INPUTS_KEY).has("TO") ||
            current.get(Constants.INPUTS_KEY).has("TOWARDS") ||
            current.get(Constants.INPUTS_KEY).has("DISTANCETO")) {
            return parseRelativePos(current, allBlocks);
        } else {
            throw new ParsingException("Could not parse block " + current.toString());
        }
    }

    private static Position parseRelativePos(JsonNode current, JsonNode allBlocks) throws ParsingException {
        ArrayList<JsonNode> inputs = new ArrayList();
        current.get(Constants.INPUTS_KEY).elements().forEachRemaining(inputs::add);

        JsonNode menuID;
        SpriteMotionStmtOpcode opcode = valueOf(current.get(Constants.OPCODE_KEY).asText());

        if (motion_goto.equals(opcode) || motion_pointtowards.equals(opcode)) {
            menuID = inputs.get(0).get(Constants.POS_INPUT_VALUE);
        } else if (motion_glideto.equals(opcode)) {
            menuID = inputs.get(1).get(Constants.POS_INPUT_VALUE);
        } else {
            throw new ParsingException(
                "Cannot parse relative coordinates for a block with opcode " + current.get(Constants.OPCODE_KEY));
        }

        ArrayList<JsonNode> fields = new ArrayList();
        allBlocks.get(menuID.asText()).get(Constants.FIELDS_KEY).elements().forEachRemaining(fields::add);
        String posString = fields.get(Constants.FIELD_VALUE).get(0).asText();

        if (posString.equals("_mouse_")) {
            return new MousePos();
        } else if (posString.equals("_random_")) {
            return new RandomPos();
        } else {
            return new PivotOf(new Identifier(posString));
        }
    }

    private static Position parseCoordinate(JsonNode current, JsonNode allBlocks) throws ParsingException {
        SpriteMotionStmtOpcode spriteMotionStmtOpcode = valueOf(current.get(Constants.OPCODE_KEY).asText());
        if (motion_glidesecstoxy.equals(spriteMotionStmtOpcode)) {
            NumExpr xExpr = NumExprParser.parseNumExpr(current, 1, allBlocks);
            NumExpr yExpr = NumExprParser.parseNumExpr(current, 2, allBlocks);
            return new CoordinatePosition(xExpr, yExpr);
        } else if (motion_gotoxy.equals(spriteMotionStmtOpcode)) {
            NumExpr xExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
            NumExpr yExpr = NumExprParser.parseNumExpr(current, 1, allBlocks);
            return new CoordinatePosition(xExpr, yExpr);
        } else {
            throw new ParsingException(
                "Cannot parse x and y coordinates for a block with opcode " + current.get(Constants.OPCODE_KEY));
        }

    }

}
