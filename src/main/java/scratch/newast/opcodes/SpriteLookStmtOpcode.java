package scratch.newast.opcodes;

public enum SpriteLookStmtOpcode {


    looks_show,            //   "show"
    looks_hide,            //   |  "hide"
    looks_sayforsecs,      //   |  "say" StringExpr "for" NumExpr  "secs"
    looks_say,             //   |  "say" StringExpr
    looks_thinkforsecs,    //   |  "think" StringExpr "for" NumExpr  "secs"
    looks_think,           //   |  "think" StringExpr
    looks_switchcostumeto,    //   |  "switch" "costume" "to" ElementChoice
    looks_changesizeby,       //   |  "change" "size" "by"  NumExpr
    looks_setsizeto,          //   |  "set" "size" "to"  NumExpr  "percent"
    looks_gotofrontback,      //   |  "go" "to" "layer"  NumExpr TODO this should be a string
    looks_goforwardbackwardlayers;    //   |  "change" "layer" "by"  NumExpr


    public static boolean contains(String opcode) {
        for (SpriteLookStmtOpcode value : SpriteLookStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
