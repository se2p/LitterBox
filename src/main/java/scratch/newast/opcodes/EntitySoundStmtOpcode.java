package scratch.newast.opcodes;

public enum EntitySoundStmtOpcode {

    ;
//    sensing_askandwait, //  "ask"  StringExpr  "and" "wait"
//    looks_switchbackdropto, // "switch" "backdrop" "to"  Backdrop
//    looks_changeeffectby, //  "change" "effect"  GraphicEffect  "by" NumExpr
//    looks_seteffectto, // "set" "effect"  GraphicEffect  "to" NumExpr
//    looks_cleargraphiceffects; // "clear" "graphic" "effects"

    public static boolean contains(String opcode) {
        for (EntitySoundStmtOpcode value : EntitySoundStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
