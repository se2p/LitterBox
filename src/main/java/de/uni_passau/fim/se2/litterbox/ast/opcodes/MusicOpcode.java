package de.uni_passau.fim.se2.litterbox.ast.opcodes;

public enum MusicOpcode implements Opcode {

    music_playDrumForBeats, music_restForBeats, music_playNoteForBeats, music_setInstrument, music_setTempo,
    music_changeTempo, music_getTempo;

    public static boolean contains(String opcode) {
        for (MusicOpcode value : MusicOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return name();
    }
}
