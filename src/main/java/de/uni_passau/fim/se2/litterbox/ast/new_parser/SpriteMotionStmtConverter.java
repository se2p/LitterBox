/*
 * Copyright (C) 2019-2024 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.new_parser;

import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.KnownFields;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.KnownInputs;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteMotionStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class SpriteMotionStmtConverter extends StmtConverter<SpriteMotionStmt> {

    SpriteMotionStmtConverter(ProgramParserState state) {
        super(state);
    }

    @Override
    SpriteMotionStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final SpriteMotionStmtOpcode opcode = SpriteMotionStmtOpcode.valueOf(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case motion_movesteps -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, block, KnownInputs.STEPS);
                yield new MoveSteps(numExpr, metadata);
            }
            case motion_turnright -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, block, KnownInputs.DEGREES);
                yield new TurnRight(numExpr, metadata);
            }
            case motion_turnleft -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, block, KnownInputs.DEGREES);
                yield new TurnLeft(numExpr, metadata);
            }
            case motion_gotoxy -> {
                final NumExpr x = NumExprConverter.convertNumExpr(state, block, KnownInputs.X);
                final NumExpr y = NumExprConverter.convertNumExpr(state, block, KnownInputs.Y);
                yield new GoToPosXY(x, y, metadata);
            }
            case motion_goto -> {
                final Position position = PositionConverter.convertPosition(state, block);
                yield new GoToPos(position, metadata);
            }
            case motion_glideto -> {
                final NumExpr secs = NumExprConverter.convertNumExpr(state, block, KnownInputs.SECS);
                final Position position = PositionConverter.convertPosition(state, block);
                yield new GlideSecsTo(secs, position, metadata);
            }
            case motion_glidesecstoxy -> {
                final NumExpr secs = NumExprConverter.convertNumExpr(state, block, KnownInputs.SECS);
                final NumExpr x = NumExprConverter.convertNumExpr(state, block, KnownInputs.X);
                final NumExpr y = NumExprConverter.convertNumExpr(state, block, KnownInputs.Y);
                yield new GlideSecsToXY(secs, x, y, metadata);
            }
            case motion_pointindirection -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, block, KnownInputs.DIRECTION);
                yield new PointInDirection(numExpr, metadata);
            }
            case motion_pointtowards -> {
                final Position position = PositionConverter.convertPosition(state, block);
                yield new PointTowards(position, metadata);
            }
            case motion_changexby -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, block, KnownInputs.DX);
                yield new ChangeXBy(numExpr, metadata);
            }
            case motion_changeyby -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, block, KnownInputs.DY);
                yield new ChangeYBy(numExpr, metadata);
            }
            case motion_setx -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, block, KnownInputs.X);
                yield new SetXTo(numExpr, metadata);
            }
            case motion_sety -> {
                final NumExpr numExpr = NumExprConverter.convertNumExpr(state, block, KnownInputs.Y);
                yield new SetYTo(numExpr, metadata);
            }
            case motion_ifonedgebounce -> new IfOnEdgeBounce(metadata);
            case motion_setrotationstyle -> {
                final String rotation = block.getFieldValueAsString(KnownFields.STYLE);
                final RotationStyle rotationStyle = new RotationStyle(rotation);
                yield new SetRotationStyle(rotationStyle, metadata);
            }
            case sensing_setdragmode -> {
                final String drag = block.getFieldValueAsString(KnownFields.DRAG_MODE);
                final DragMode dragMode = new DragMode(drag);
                yield new SetDragMode(dragMode, metadata);
            }
        };
    }
}
