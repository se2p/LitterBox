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

import static scratch.ast.Constants.OPCODE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import scratch.utils.Preconditions;
import scratch.ast.ParsingException;
import scratch.ast.model.expression.num.NumExpr;
import scratch.ast.model.position.Position;
import scratch.ast.model.statement.spritemotion.ChangeXBy;
import scratch.ast.model.statement.spritemotion.ChangeYBy;
import scratch.ast.model.statement.spritemotion.GlideSecsTo;
import scratch.ast.model.statement.spritemotion.GoToPos;
import scratch.ast.model.statement.spritemotion.IfOnEdgeBounce;
import scratch.ast.model.statement.spritemotion.MoveSteps;
import scratch.ast.model.statement.spritemotion.PointInDirection;
import scratch.ast.model.statement.spritemotion.PointTowards;
import scratch.ast.model.statement.spritemotion.SetXTo;
import scratch.ast.model.statement.spritemotion.SetYTo;
import scratch.ast.model.statement.spritemotion.SpriteMotionStmt;
import scratch.ast.model.statement.spritemotion.TurnLeft;
import scratch.ast.model.statement.spritemotion.TurnRight;
import scratch.ast.opcodes.SpriteMotionStmtOpcode;
import scratch.ast.parser.NumExprParser;
import scratch.ast.parser.PositionParser;

public class SpriteMotionStmtParser {

    public static SpriteMotionStmt parse(JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
            .checkArgument(SpriteMotionStmtOpcode.contains(opcodeString),
                "Given blockID does not point to a sprite motion block.");

        final SpriteMotionStmtOpcode opcode = SpriteMotionStmtOpcode.valueOf(opcodeString);
        NumExpr numExpr;
        Position position;

        switch (opcode) {
            case motion_movesteps:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                return new MoveSteps(numExpr);
            case motion_turnright:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                return new TurnRight(numExpr);
            case motion_turnleft:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                return new TurnLeft(numExpr);
            case motion_gotoxy:
            case motion_goto:
                position = PositionParser.parse(current, allBlocks);
                return new GoToPos(position);
            case motion_glideto:
            case motion_glidesecstoxy:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                position = PositionParser.parse(current, allBlocks);
                return new GlideSecsTo(numExpr, position);
            case motion_pointindirection:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                return new PointInDirection(numExpr);
            case motion_pointtowards:
                position = PositionParser.parse(current, allBlocks);
                return new PointTowards(position);
            case motion_changexby:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                return new ChangeXBy(numExpr);
            case motion_changeyby:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                return new ChangeYBy(numExpr);
            case motion_setx:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                return new SetXTo(numExpr);
            case motion_sety:
                numExpr = NumExprParser.parseNumExpr(current, 0, allBlocks);
                return new SetYTo(numExpr);
            case motion_ifonedgebounce:
                return new IfOnEdgeBounce();
            default:
                throw new RuntimeException("Parsing not implemented yet for opcode " + opcodeString);
        }
    }
}
