package scratch.newast.model.expression;

import scratch.newast.model.timecomp.TimeComp;

public class Current extends NumExpr {
    private TimeComp timeComp;

    public Current(TimeComp timeComp) {
        this.timeComp = timeComp;
    }

    public TimeComp getTimeComp() {
        return timeComp;
    }

    public void setTimeComp(TimeComp timeComp) {
        this.timeComp = timeComp;
    }
}