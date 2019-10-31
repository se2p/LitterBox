package scratch.newast.model.statement;

import scratch.newast.model.sound.Sound;

public class StartSound extends EntitySoundStmt {
    private Sound sound;

    public StartSound(Sound sound) {
        this.sound = sound;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }
}