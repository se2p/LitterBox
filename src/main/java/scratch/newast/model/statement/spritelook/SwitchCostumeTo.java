package scratch.newast.model.statement.spritelook;

import scratch.newast.model.costume.Costume;

public class SwitchCostumeTo implements SpriteLookStmt {
    private Costume costume;

    public SwitchCostumeTo(Costume costume) {
        this.costume = costume;
    }

    public Costume getCostume() {
        return costume;
    }

    public void setCostume(Costume costume) {
        this.costume = costume;
    }
}