package de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock;

import de.uni_passau.fim.se2.litterbox.ast.opcodes.Opcode;

public enum SpeakerStmtOpcode implements Opcode {
    // codey
    show_play_sound,
    show_play_sound_wait,
    show_stop_allsound,
    show_play_note_with_string,
    show_pause_note,
    show_play_hz,               // mcore too
    show_change_volume,
    show_set_volume,

    // mcore
    sound_play_note,
    sound_play_hz;

    public static boolean contains(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        for (SpeakerStmtOpcode value : SpeakerStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    public static SpeakerStmtOpcode getOpcode(String opcode) {
        opcode = Opcode.removePrefix(opcode);
        return valueOf(opcode);
    }

    @Override
    public String getName() {
        return name();
    }
}
