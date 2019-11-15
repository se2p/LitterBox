package scratch.newast.opcodes;

public enum SetStmtOpcode {
    data_setvariableto, sensing_setdragmode, motion_setrotationstyle, looks_seteffectto,
    sound_seteffectto, sound_setvolumeto;

    //TODO maybe size too

    public static boolean contains(String opcode) {
        for (SetStmtOpcode value : SetStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
