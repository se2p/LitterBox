package de.uni_passau.fim.se2.litterbox.ast.parser.stmt.mblock;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.option.RobotDirection;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.statement.movement.*;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.RobotMoveStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class RobotMoveStmtParser {

    private static final String POWER_KEY = "POWER";
    private static final String ANGLE_KEY = "ANGLE";
    private static final String LEFT_POWER_KEY = "LEFT_POWER";
    private static final String RIGHT_POWER_KEY = "RIGHT_POWER";
    private static final String MOVE_DIRECTION_KEY = "MOVE_DIRECTION";
    private static final String POWER_LEFT_KEY = "POWER_LEFT";
    private static final String POWER_RIGHT_KEY = "POWER_RIGHT";

    public static RobotMoveStmt parse(final ProgramParserState state, String blockId, JsonNode current, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(blocks);

        String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(RobotMoveStmtOpcode.contains(opcodeString), "Given blockId does not point to an Speaker block.");

        RobotMoveStmtOpcode opcode = RobotMoveStmtOpcode.getOpcode(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);
        switch (opcode) {
            case forward_time:
            case move_forward_with_time:
                NumExpr power = NumExprParser.parseNumExpr(state, current, POWER_KEY, blocks);
                NumExpr time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new MoveForwardTimed(power, time, metadata);

            case backward_time:
            case move_backward_with_time:
                power = NumExprParser.parseNumExpr(state, current, POWER_KEY, blocks);
                time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new MoveBackwardTimed(power, time, metadata);

            case turnleft_time:
            case move_left_with_time:
                power = NumExprParser.parseNumExpr(state, current, POWER_KEY, blocks);
                time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new TurnLeftTimed(power, time, metadata);

            case turnright_time:
            case move_right_with_time:
                power = NumExprParser.parseNumExpr(state, current, POWER_KEY, blocks);
                time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new TurnRightTimed(power, time, metadata);

            case rocky_keep_absolute_forward:
                power = NumExprParser.parseNumExpr(state, current, POWER_KEY, blocks);
                time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new KeepForwardTimed(power, time, metadata);

            case rocky_keep_absolute_backward:
                power = NumExprParser.parseNumExpr(state, current, POWER_KEY, blocks);
                time = NumExprParser.parseNumExpr(state, current, TIME_KEY, blocks);
                return new KeepBackwardTimed(power, time, metadata);

            case move_left_with_angle:
                NumExpr angle = NumExprParser.parseNumExpr(state, current, ANGLE_KEY, blocks);
                return new TurnLeft2(angle, metadata);

            case move_right_with_angle:
                angle = NumExprParser.parseNumExpr(state, current, ANGLE_KEY, blocks);
                return new TurnRight2(angle, metadata);

            case move:
                String directionName;
                if (opcodeString.contains("mcore.")) {
                    directionName = current.get(FIELDS_KEY).get(MOVE_DIRECTION_KEY).get(0).asText();
                } else {
                    directionName = current.get(FIELDS_KEY).get(DIRECTION_KEY_CAP).get(0).asText();
                }
                RobotDirection direction = new RobotDirection(directionName);
                power = NumExprParser.parseNumExpr(state, current, POWER_KEY, blocks);
                return new MoveDirection(direction, power, metadata);

            case move_wheel_speed:
            case move_with_motors:
                NumExpr leftPower;
                NumExpr rightPower;
                if (opcodeString.contains("mcore.")) {
                    leftPower = NumExprParser.parseNumExpr(state, current, POWER_LEFT_KEY, blocks);
                    rightPower = NumExprParser.parseNumExpr(state, current, POWER_RIGHT_KEY, blocks);
                } else {
                    leftPower = NumExprParser.parseNumExpr(state, current, LEFT_POWER_KEY, blocks);
                    rightPower = NumExprParser.parseNumExpr(state, current, RIGHT_POWER_KEY, blocks);
                }
                return new MoveSides(leftPower, rightPower, metadata);

            case move_stop:
                return new MoveStop(metadata);

            default:
                throw new IllegalStateException("SpeakerStmt Block with opcode " + opcode + " was not parsed");
        }
    }
}
