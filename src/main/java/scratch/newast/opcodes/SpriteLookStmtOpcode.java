package scratch.newast.opcodes;

public enum SpriteLookStmtOpcode {

    ;
//    sensing_askandwait, //  "ask"  StringExpr  "and" "wait"
//    looks_switchbackdropto, // "switch" "backdrop" "to"  Backdrop
//    looks_changeeffectby, //  "change" "effect"  GraphicEffect  "by" NumExpr
//    looks_seteffectto, // "set" "effect"  GraphicEffect  "to" NumExpr
//    looks_cleargraphiceffects; // "clear" "graphic" "effects"

    public static boolean contains(String opcode) {
        for (SpriteLookStmtOpcode value : SpriteLookStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
