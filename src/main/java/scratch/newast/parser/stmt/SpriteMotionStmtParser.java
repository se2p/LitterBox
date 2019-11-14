package scratch.newast.parser.stmt;

import static scratch.newast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import scratch.newast.ParsingException;
import scratch.newast.model.expression.num.NumExpr;
import scratch.newast.model.position.Position;
import scratch.newast.model.statement.spritemotion.ChangeXBy;
import scratch.newast.model.statement.spritemotion.ChangeYBy;
import scratch.newast.model.statement.spritemotion.GlideSecsTo;
import scratch.newast.model.statement.spritemotion.GoToPos;
import scratch.newast.model.statement.spritemotion.IfOnEdgeBounce;
import scratch.newast.model.statement.spritemotion.MoveSteps;
import scratch.newast.model.statement.spritemotion.PointInDirection;
import scratch.newast.model.statement.spritemotion.PointTowards;
import scratch.newast.model.statement.spritemotion.SetXTo;
import scratch.newast.model.statement.spritemotion.SetYTo;
import scratch.newast.model.statement.spritemotion.SpriteMotionStmt;
import scratch.newast.model.statement.spritemotion.TurnLeft;
import scratch.newast.model.statement.spritemotion.TurnRight;
import scratch.newast.opcodes.SpriteMotionStmtOpcode;
import scratch.newast.parser.ExpressionParser;
import scratch.newast.parser.PositionParser;

public class SpriteMotionStmtParser {

    public static SpriteMotionStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(SpriteMotionStmtOpcode.contains(opcodeString),
                "Given blockID does not point to a sprite motion block.");

        SpriteMotionStmtOpcode opcode = SpriteMotionStmtOpcode.valueOf(opcodeString);
        NumExpr numExpr;
        Position position;

        switch (opcode) {
            case motion_movesteps:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                return new MoveSteps(numExpr);
            case motion_turnright:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                return new TurnRight(numExpr);
            case motion_turnleft:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                return new TurnLeft(numExpr);
            case motion_gotoxy:
            case motion_goto:
                position = PositionParser.parse(current, allBlocks);
                return new GoToPos(position);
            case motion_glideto:
            case motion_glidesecstoxy:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                position = PositionParser.parse(current, allBlocks);
                return new GlideSecsTo(numExpr, position);
            case motion_pointindirection:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                return new PointInDirection(numExpr);
            case motion_pointtowards:
                position = PositionParser.parse(current, allBlocks);
                return new PointTowards(position);
            case motion_changexby:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                return new ChangeXBy(numExpr);
            case motion_changeyby:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                return new ChangeYBy(numExpr);
            case motion_setx:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                return new SetXTo(numExpr);
            case motion_sety:
                numExpr = ExpressionParser.parseNumExpr(current, 0, allBlocks);
                return new SetYTo(numExpr);
            case motion_ifonedgebounce:
                return new IfOnEdgeBounce();
            default:
                throw new RuntimeException("Parsing not implemented yet for opcode " + opcodeString);
        }
    }
}
