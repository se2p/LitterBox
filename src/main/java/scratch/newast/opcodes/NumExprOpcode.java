package scratch.newast.opcodes;

public enum NumExprOpcode {
    operator_add, operator_subtract, operator_multiply, operator_divide,
    operator_mod, operator_random, operator_round, operator_length,
    data_lengthoflist, data_itemnumoflist, sensing_timer, sensing_dayssince2000,
    sensing_current, sensing_distanceto, sensing_mousex, sensing_mousey,
    sensing_loudness, operator_mathop;

    public static boolean contains(String opcode) {
        for (NumExprOpcode value : NumExprOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
