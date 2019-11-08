package scratch.newast.opcodes;

public enum ProcedureOpcode {
procedures_definition, procedures_prototype, argument_reporter_string_number, argument_reporter_boolean,
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
