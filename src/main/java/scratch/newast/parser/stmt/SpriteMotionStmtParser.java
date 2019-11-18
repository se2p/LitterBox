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
import scratch.newast.parser.NumExprParser;
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
