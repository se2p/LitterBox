package scratch.newast.opcodes;

public enum StringExprOpcode {

    operator_join, operator_letter_of, sensing_username, data_itemoflist, sound_volume;
    // FIXME attribute of, resource of are missing

    public static boolean contains(String opcode) {
        for (StringExprOpcode value : StringExprOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
