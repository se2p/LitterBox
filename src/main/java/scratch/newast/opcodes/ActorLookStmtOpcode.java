package scratch.newast.opcodes;

public enum ActorLookStmtOpcode {

    sensing_askandwait, //  "ask"  StringExpr  "and" "wait"
    looks_switchbackdropto, // "switch" "backdrop" "to"  Backdrop
    looks_changeeffectby, //  "change" "effect"  GraphicEffect  "by" NumExpr
    looks_seteffectto, // "set" "effect"  GraphicEffect  "to" NumExpr
    looks_cleargraphiceffects, // "clear" "graphic" "effects"
    data_showvariable,
    data_hidevariable;
//    data_showvariable,
//    data_hidelist;

    public static boolean contains(String opcode) {
        for (ActorLookStmtOpcode value : ActorLookStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
