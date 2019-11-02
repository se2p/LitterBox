package scratch.newast.model.expression.numexpression;

import scratch.newast.model.expression.numexpression.NumExpr;
import scratch.newast.model.timecomp.TimeComp;

public class Current implements NumExpr {
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