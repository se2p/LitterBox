package scratch.newast.model.event;

import scratch.newast.model.Key;

public class KeyPressed implements Event {
    private Key key;

    public KeyPressed(Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
}