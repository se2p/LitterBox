package scratch2.data;

/**
 * Wrapper for Scratch Sounds
 */
public class Sound {

    private String name;
    private int soundId;

    public Sound() {

    }

    @Override
    public String toString() {
        return "Sound{" +
                "name='" + name + '\'' +
                ", soundId=" + soundId +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSoundId() {
        return soundId;
    }

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }
}
