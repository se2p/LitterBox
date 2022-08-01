package de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock;

import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;

public enum ResetStmtOpcode implements Opcode {
    // codey
    reset_angle,
    show_reset_time,

    // mcore
    reset_timer;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (ResetStmtOpcode value : ResetStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static ResetStmtOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
