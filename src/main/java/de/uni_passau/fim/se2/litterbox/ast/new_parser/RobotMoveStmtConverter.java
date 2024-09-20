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

import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RobotDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlock;
import de.uni_passau.fim.se2.litterbox.ast.new_parser.raw_ast.RawBlockId;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.RobotMoveStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;

final class RobotMoveStmtConverter extends StmtConverter<RobotMoveStmt> {

    private static final String POWER_KEY = "POWER";
    private static final String ANGLE_KEY = "ANGLE";
    private static final String LEFT_POWER_KEY = "LEFT_POWER";
    private static final String RIGHT_POWER_KEY = "RIGHT_POWER";
    private static final String MOVE_DIRECTION_KEY = "MOVE_DIRECTION";
    private static final String POWER_LEFT_KEY = "POWER_LEFT";
    private static final String POWER_RIGHT_KEY = "POWER_RIGHT";

    RobotMoveStmtConverter(final ProgramParserState state) {
        super(state);
    }

    @Override
    RobotMoveStmt convertStmt(final RawBlockId blockId, final RawBlock.RawRegularBlock block) {
        final RobotMoveStmtOpcode opcode = RobotMoveStmtOpcode.getOpcode(block.opcode());
        final BlockMetadata metadata = RawBlockMetadataConverter.convertBlockMetadata(blockId, block);

        return switch (opcode) {
            case move_stop -> new MoveStop(metadata);
            case rocky_keep_absolute_forward -> {
                final NumExpr power = getPower(block);
                final NumExpr time = getTime(block);
                yield new KeepForwardTimed(power, time, metadata);
            }
            case rocky_keep_absolute_backward -> {
                final NumExpr power = getPower(block);
                final NumExpr time = getTime(block);
                yield new KeepBackwardTimed(power, time, metadata);
            }
            case move_left_with_angle -> {
                final NumExpr angle = NumExprConverter.convertNumExpr(state, block, block.inputs().get(ANGLE_KEY));
                yield new TurnLeft2(angle, metadata);
            }
            case move_right_with_angle -> {
                final NumExpr angle = NumExprConverter.convertNumExpr(state, block, block.inputs().get(ANGLE_KEY));
                yield new TurnRight2(angle, metadata);
            }
            case forward_time, move_forward_with_time-> {
                final NumExpr power = getPower(block);
                final NumExpr time = getTime(block);
                yield new MoveForwardTimed(power, time, metadata);
            }
            case backward_time, move_backward_with_time -> {
                final NumExpr power = getPower(block);
                final NumExpr time = getTime(block);
                yield new MoveBackwardTimed(power, time, metadata);
            }
            case turnleft_time, move_left_with_time -> {
                final NumExpr power = getPower(block);
                final NumExpr time = getTime(block);
                yield new TurnLeftTimed(power, time, metadata);
            }
            case turnright_time, move_right_with_time -> {
                final NumExpr power = getPower(block);
                final NumExpr time = getTime(block);
                yield new TurnRightTimed(power, time, metadata);
            }
            case move -> {
                final String directionName;
                if (block.opcode().contains("mcore.")) {
                    directionName = block.fields().get(MOVE_DIRECTION_KEY).value().toString();
                } else {
                    directionName = block.fields().get(Constants.DIRECTION_KEY_CAP).value().toString();
                }

                final RobotDirection direction = new RobotDirection(directionName);
                final NumExpr power = getPower(block);

                yield new MoveDirection(direction, power, metadata);
            }
            case move_wheel_speed, move_with_motors -> {
                final NumExpr leftPower;
                final NumExpr rightPower;

                if (block.opcode().contains("mcore.")) {
                    leftPower = NumExprConverter.convertNumExpr(state, block, block.inputs().get(POWER_LEFT_KEY));
                    rightPower = NumExprConverter.convertNumExpr(state, block, block.inputs().get(POWER_RIGHT_KEY));
                } else {
                    leftPower = NumExprConverter.convertNumExpr(state, block, block.inputs().get(LEFT_POWER_KEY));
                    rightPower = NumExprConverter.convertNumExpr(state, block, block.inputs().get(RIGHT_POWER_KEY));
                }

                yield new MoveSides(leftPower, rightPower, metadata);
            }
        };
    }

    private NumExpr getPower(final RawBlock.RawRegularBlock block) {
        return NumExprConverter.convertNumExpr(state, block, block.inputs().get(POWER_KEY));
    }

    private NumExpr getTime(final RawBlock.RawRegularBlock block) {
        return NumExprConverter.convertNumExpr(state, block, block.inputs().get(Constants.TIME_KEY));
    }
}
