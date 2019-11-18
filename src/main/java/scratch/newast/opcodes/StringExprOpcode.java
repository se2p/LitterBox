package scratch.newast.opcodes;

public enum StringExprOpcode {

    operator_join, operator_letter_of, sensing_username, data_itemoflist,
    sound_volume, motion_xposition, motion_yposition, motion_direction,
    looks_costumenumbername, looks_backdropnumbername, looks_size, sensing_answer, sensing_of;

    public static boolean contains(String opcode) {
        for (StringExprOpcode value : StringExprOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
