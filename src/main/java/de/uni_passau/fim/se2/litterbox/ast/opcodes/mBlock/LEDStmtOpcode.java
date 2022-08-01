package de.uni_passau.fim.se2.litterbox.ast.opcodes.mBlock;

import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;

public enum LEDStmtOpcode implements Opcode {
    // codey
    show_led_with_time,
    show_led,                   // mcore too
    show_led_rgb,               // mcore too
    turn_off_led,
    rocky_show_led_color,
    rocky_turn_off_led_color,

    // mcore
    show_led_time;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (LEDStmtOpcode value : LEDStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static LEDStmtOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
