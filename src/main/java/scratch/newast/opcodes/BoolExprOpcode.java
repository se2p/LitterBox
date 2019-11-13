package scratch.newast.opcodes;

public enum BoolExprOpcode {

    sensing_touchingobject, sensing_touchingcolor, sensing_coloristouchingcolor,
    sensing_keypressed, sensing_mousedown, operator_gt, operator_lt, operator_equals,
    operator_and, operator_or, operator_not, operator_contains, data_listcontainsitem;

    public static boolean contains(String opcode) {
        for (BoolExprOpcode value : BoolExprOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
