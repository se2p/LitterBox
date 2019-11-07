package scratch.newast.opcodes;

public enum StringExprOpcode {

    none;

    public static boolean contains(String opcode) {
        for (StringExprOpcode value : StringExprOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
