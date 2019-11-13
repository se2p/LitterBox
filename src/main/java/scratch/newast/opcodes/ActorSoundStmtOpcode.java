package scratch.newast.opcodes;

public enum ActorSoundStmtOpcode {

    sound_playuntildone,    //    "play" "sound"  ElementChoice  "until" "done"
    sound_play,             // |  "start" "sound"  ElementChoice
    sound_cleareffects,     // |  "clear" "sound" "effects"
    sound_stopallsounds;    // |  "stop" "all" "sounds"

    public static boolean contains(String opcode) {
        for (ActorSoundStmtOpcode value : ActorSoundStmtOpcode.values()) {
            if (value.name().equals(opcode)) {
                return true;
            }
        }
        return false;
    }
}
