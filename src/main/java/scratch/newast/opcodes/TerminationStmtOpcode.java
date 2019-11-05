package scratch.newast.opcodes;


public enum TerminationStmtOpcode {
    control_stop,
    control_delete_this_clone;

    public static boolean contains(String opcode) {
        for (TerminationStmtOpcode value : TerminationStmtOpcode.values()) {
            if (value.toString().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
