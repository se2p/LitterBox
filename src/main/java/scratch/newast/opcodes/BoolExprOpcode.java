package scratch.newast.opcodes;

public enum BoolExprOpcode {

    none;

    public static boolean contains(String opcode) {
        for (BoolExprOpcode value : BoolExprOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
