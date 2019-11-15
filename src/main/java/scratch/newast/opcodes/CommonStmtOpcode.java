package scratch.newast.opcodes;

public enum CommonStmtOpcode {

    control_wait,            //"wait" NumExpr "seconds"
    control_wait_until,      //"wait" "until" BoolExpr
    control_stop,            //"stop" "other" "scripts" "in" "sprite"
    control_create_clone_of, // "create" "clone" "of" Ident
    event_broadcast,         // "broadcast" Message
    event_broadcastandwait,  // "broadcast" Message "and" "wait"
    sensing_resettimer,      // "reset" "timer"
    data_changevariableby,    // "change" Variable "by" Expression

    // "change" "attribute" StringExpr "by" NumExpr
    sound_changevolumeby,
    sound_changeeffectby,
    //    looks_changesizeby, //FIXME is this now a common stmt?
    looks_changeeffectby;

    public static boolean contains(String opcode) {
        for (CommonStmtOpcode value : CommonStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
