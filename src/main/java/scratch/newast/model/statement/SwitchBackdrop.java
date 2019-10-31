package scratch.newast.model.statement;

import scratch.newast.model.backdrop.Backdrop;

public class SwitchBackdrop extends EntityLookStmt {
    private Backdrop backdrop;

    public SwitchBackdrop(Backdrop backdrop) {
        this.backdrop = backdrop;
    }

    public Backdrop getBackdrop() {
        return backdrop;
    }

    public void setBackdrop(Backdrop backdrop) {
        this.backdrop = backdrop;
    }
}