package de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock;

import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;

public enum RobotMoveStmtOpcode implements Opcode {
    // codey
    move_forward_with_time,
    move_backward_with_time,
    move_left_with_time,
    move_right_with_time,
    rocky_keep_absolute_forward,
    rocky_keep_absolute_backward,
    move_left_with_angle,
    move_right_with_angle,
    move,                           // mcore too
    move_with_motors,
    move_stop,                      // mcore too

    // mcore
    forward_time,
    backward_time,
    turnleft_time,
    turnright_time,
    move_wheel_speed;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (RobotMoveStmtOpcode value : RobotMoveStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static RobotMoveStmtOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
