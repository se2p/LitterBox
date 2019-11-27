package scratch.ast.opcodes;

public enum PenOpcode {
    pen_clear, pen_stamp, pen_penDown, pen_penUp, pen_setPenColorToColor, pen_setPenColorParamTo, pen_changePenColorParamBy;

    public static boolean contains(String opcode) {
        for (PenOpcode value : PenOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
