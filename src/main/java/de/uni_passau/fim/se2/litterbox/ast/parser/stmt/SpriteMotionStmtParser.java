/*
 * Copyright (C) 2020 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteMotionStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.PositionParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class SpriteMotionStmtParser {

    public static SpriteMotionStmt parse(String identifier, JsonNode current, JsonNode allBlocks)
            throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(SpriteMotionStmtOpcode.contains(opcodeString),
                        "Given blockID does not point to a sprite motion block.");

        final SpriteMotionStmtOpcode opcode = SpriteMotionStmtOpcode.valueOf(opcodeString);
        NumExpr numExpr;
        Position position;
        BlockMetadata metadata = BlockMetadataParser.parse(identifier, current);
        switch (opcode) {
            case motion_movesteps:
                numExpr = NumExprParser.parseNumExpr(current, STEPS_KEY, allBlocks);
                return new MoveSteps(numExpr, metadata);
            case motion_turnright:
                numExpr = NumExprParser.parseNumExpr(current, DEGREES_KEY, allBlocks);
                return new TurnRight(numExpr, metadata);
            case motion_turnleft:
                numExpr = NumExprParser.parseNumExpr(current, DEGREES_KEY, allBlocks);
                return new TurnLeft(numExpr, metadata);
            case motion_gotoxy:
                NumExpr xExpr = NumExprParser.parseNumExpr(current, X, allBlocks);
                NumExpr yExpr = NumExprParser.parseNumExpr(current, Y, allBlocks);
                return new GoToPosXY(xExpr, yExpr, metadata);
            case motion_goto:
                position = PositionParser.parse(current, allBlocks);
                return new GoToPos(position, metadata);
            case motion_glidesecstoxy:
                NumExpr secs = NumExprParser.parseNumExpr(current, SECS_KEY, allBlocks);
                NumExpr x = NumExprParser.parseNumExpr(current, X, allBlocks);
                NumExpr y = NumExprParser.parseNumExpr(current, Y, allBlocks);
                return new GlideSecsToXY(secs, x, y, metadata);
            case motion_glideto:
                numExpr = NumExprParser.parseNumExpr(current, SECS_KEY, allBlocks);
                position = PositionParser.parse(current, allBlocks);
                return new GlideSecsTo(numExpr, position, metadata);
            case motion_pointindirection:
                numExpr = NumExprParser.parseNumExpr(current, DIRECTION_KEY_CAP, allBlocks);
                return new PointInDirection(numExpr, metadata);
            case motion_pointtowards:
                position = PositionParser.parse(current, allBlocks);
                return new PointTowards(position, metadata);
            case motion_changexby:
                numExpr = NumExprParser.parseNumExpr(current, DX_KEY, allBlocks);
                return new ChangeXBy(numExpr, metadata);
            case motion_changeyby:
                numExpr = NumExprParser.parseNumExpr(current, DY_KEY, allBlocks);
                return new ChangeYBy(numExpr, metadata);
            case motion_setx:
                numExpr = NumExprParser.parseNumExpr(current, X, allBlocks);
                return new SetXTo(numExpr, metadata);
            case motion_sety:
                numExpr = NumExprParser.parseNumExpr(current, Y, allBlocks);
                return new SetYTo(numExpr, metadata);
            case motion_ifonedgebounce:
                return new IfOnEdgeBounce(metadata);
            case sensing_setdragmode:
                return parseSetDragmode(current, metadata);
            case motion_setrotationstyle:
                return parseSetRotationStyle(current, metadata);
            default:
                throw new RuntimeException("Parsing not implemented yet for opcode " + opcodeString);
        }
    }

    private static SpriteMotionStmt parseSetRotationStyle(JsonNode current, BlockMetadata metadata) {
        String rota = current.get(FIELDS_KEY).get(STYLE_KEY).get(0).asText();
        return new SetRotationStyle(new RotationStyle(rota), metadata);
    }

    private static SpriteMotionStmt parseSetDragmode(JsonNode current, BlockMetadata metadata) {
        String drag = current.get(FIELDS_KEY).get(DRAGMODE_KEY).get(0).asText();
        return new SetDragMode(new DragMode(drag), metadata);
    }
}
