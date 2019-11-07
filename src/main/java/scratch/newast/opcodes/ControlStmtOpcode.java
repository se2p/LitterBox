package scratch.newast.opcodes;

public enum ControlStmtOpcode {

    control_if, // "if" BoolExpr "then" StmtList
    control_if_else, // "if" BoolExpr "then" StmtList "else" StmtList
    control_repeat, // "repeat"  NumExpr "times" StmtList
    control_repeat_until, // "until" BoolExpr "repeat" StmtList
    control_forever; //"repeat" "forever" StmtList

    public static boolean contains(String opcode) {
        for (ControlStmtOpcode value : ControlStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
