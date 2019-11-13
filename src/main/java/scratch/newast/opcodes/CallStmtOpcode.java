package scratch.newast.opcodes;

public enum CallStmtOpcode {
    procedures_call;

    public static boolean contains(String opcode) {
        for (ProcedureOpcode value : ProcedureOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
