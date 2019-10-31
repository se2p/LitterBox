package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;

public class GoToLayer extends SpriteLookStmt {
    private NumExpr layer;

    public GoToLayer(NumExpr layer) {
        this.layer = layer;
    }

    public NumExpr getLayer() {
        return layer;
    }

    public void setLayer(NumExpr layer) {
        this.layer = layer;
    }
}