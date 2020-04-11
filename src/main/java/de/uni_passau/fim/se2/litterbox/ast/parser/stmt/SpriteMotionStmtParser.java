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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteMotionStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.PositionParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

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
                numExpr = NumExprParser.parseNumExprWithName(current, STEPS_KEY, allBlocks);
                return new MoveSteps(numExpr);
            case motion_turnright:
                numExpr = NumExprParser.parseNumExprWithName(current, DEGREES_KEY, allBlocks);
                return new TurnRight(numExpr);
            case motion_turnleft:
                numExpr = NumExprParser.parseNumExprWithName(current, DEGREES_KEY, allBlocks);
                return new TurnLeft(numExpr);
            case motion_gotoxy:
                NumExpr xExpr = NumExprParser.parseNumExprWithName(current, X, allBlocks);
                NumExpr yExpr = NumExprParser.parseNumExprWithName(current, Y, allBlocks);
                return new GoToPosXY(xExpr, yExpr);
            case motion_goto:
                position = PositionParser.parse(current, allBlocks);
                return new GoToPos(position);
            case motion_glidesecstoxy:
                NumExpr secs = NumExprParser.parseNumExprWithName(current, SECS_KEY, allBlocks);
                NumExpr x = NumExprParser.parseNumExprWithName(current, X, allBlocks);
                NumExpr y = NumExprParser.parseNumExprWithName(current, Y, allBlocks);
                return new GlideSecsToXY(secs, x, y);
            case motion_glideto:
                numExpr = NumExprParser.parseNumExprWithName(current, SECS_KEY, allBlocks);
                position = PositionParser.parse(current, allBlocks);
                return new GlideSecsTo(numExpr, position);
            case motion_pointindirection:
                numExpr = NumExprParser.parseNumExprWithName(current, DIRECTION_KEY_CAP, allBlocks);
                return new PointInDirection(numExpr);
            case motion_pointtowards:
                position = PositionParser.parse(current, allBlocks);
                return new PointTowards(position);
            case motion_changexby:
                numExpr = NumExprParser.parseNumExprWithName(current, DX_KEY, allBlocks);
                return new ChangeXBy(numExpr);
            case motion_changeyby:
                numExpr = NumExprParser.parseNumExprWithName(current, DY_KEY, allBlocks);
                return new ChangeYBy(numExpr);
            case motion_setx:
                numExpr = NumExprParser.parseNumExprWithName(current, X_KEY_CAP, allBlocks);
                return new SetXTo(numExpr);
            case motion_sety:
                numExpr = NumExprParser.parseNumExprWithName(current, Y_KEY_CAP, allBlocks);
                return new SetYTo(numExpr);
            case motion_ifonedgebounce:
                return new IfOnEdgeBounce();
            case sensing_setdragmode:
                return parseSetDragmode(current);
            case motion_setrotationstyle:
                return parseSetRotationStyle(current);
            default:
                throw new RuntimeException("Parsing not implemented yet for opcode " + opcodeString);
        }
    }

    private static SpriteMotionStmt parseSetRotationStyle(JsonNode current) {
        String rota = current.get(FIELDS_KEY).get(STYLE_KEY).get(0).asText();
        return new SetRotationStyle(RotationStyle.fromString(rota));
    }

    private static SpriteMotionStmt parseSetDragmode(JsonNode current) {
        String drag = current.get(FIELDS_KEY).get(DRAGMODE_KEY).get(0).asText();
        return new SetDragMode(DragMode.fromString(drag));
    }
}
