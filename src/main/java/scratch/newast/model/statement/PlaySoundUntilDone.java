package scratch.newast.model.statement;

import scratch.newast.model.sound.Sound;

public class PlaySoundUntilDone implements EntitySoundStmt {
    private Sound sound;

    public PlaySoundUntilDone(Sound sound) {
        this.sound = sound;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }
}