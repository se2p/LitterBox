package scratch.newast.opcodes;

public enum CommonStmtOpcode {

    control_wait,            //"wait" NumExpr "seconds"
    control_wait_until,      //"wait" "until" BoolExpr
    control_stop,            //"stop" "other" "scripts" "in" "sprite"
    control_create_clone_of, // "create" "clone" "of" Ident
    event_broadcast,         // "broadcast" Message
    event_broadcastandwait,  // "broadcast" Message "and" "wait"
    sensing_resettimer,      // "reset" "timer"
    data_changevariableby    // "change" Variable "by" Expression
    ;
//    sensing_askandwait, //  "ask"  StringExpr  "and" "wait"
//    looks_switchbackdropto, // "switch" "backdrop" "to"  Backdrop
//    looks_changeeffectby, //  "change" "effect"  GraphicEffect  "by" NumExpr
//    looks_seteffectto, // "set" "effect"  GraphicEffect  "to" NumExpr
//    looks_cleargraphiceffects; // "clear" "graphic" "effects"

    public static boolean contains(String opcode) {
        for (CommonStmtOpcode value : CommonStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
