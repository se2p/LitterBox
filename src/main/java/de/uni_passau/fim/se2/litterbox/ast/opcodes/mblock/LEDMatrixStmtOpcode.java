package de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock;

import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;

public enum LEDMatrixStmtOpcode implements Opcode {
    // codey
    show_led_matrix_face_with_time,
    show_led_matrix_face,
    show_led_matrix_face_position,
    show_led_matrix_turn_off,
    show_led_matrix,
    show_led_matrix_string,
    show_led_matrix_string_position,
    show_led_matrix_position,
    light_off_led_matrix_position,
    toggle_led_matrix_position,

    // mcore
    show_face_time,
    show_face,
    show_face_position,
    show_text,
    show_text_position,
    show_number,
    show_time,
    show_face_off;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (LEDMatrixStmtOpcode value : LEDMatrixStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static LEDMatrixStmtOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
