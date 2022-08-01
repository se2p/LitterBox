package de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock;

import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;

public enum IRStmtOpcode implements Opcode {
    comm_send_ir,
    comm_learn_with_time,
    comm_send_learn_result,

    // mcore
    send_ir;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (IRStmtOpcode value : IRStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static IRStmtOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
