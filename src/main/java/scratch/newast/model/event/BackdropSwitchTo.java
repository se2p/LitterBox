package scratch.newast.model.event;

import scratch.newast.model.variable.Identifier;

public class BackdropSwitchTo implements Event {
    private Identifier backdrop;

    public BackdropSwitchTo(Identifier backdrop) {
        this.backdrop = backdrop;
    }

    public Identifier getBackdrop() {
        return backdrop;
    }

    public void setBackdrop(Identifier backdrop) {
        this.backdrop = backdrop;
    }
}