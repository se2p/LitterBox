package scratch.newast.model.statement;

import scratch.newast.model.expression.NumExpr;

public class SayForSecs implements SpriteLookStmt {
    private String string;
    private NumExpr secs;

    public SayForSecs(String string, NumExpr secs) {
        this.string = string;
        this.secs = secs;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public NumExpr getSecs() {
        return secs;
    }

    public void setSecs(NumExpr secs) {
        this.secs = secs;
    }
}