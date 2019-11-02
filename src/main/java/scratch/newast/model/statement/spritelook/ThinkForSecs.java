package scratch.newast.model.statement.spritelook;

import scratch.newast.model.expression.num.NumExpr;

public class ThinkForSecs implements SpriteLookStmt {
    private String thought;
    private NumExpr secs;

    public ThinkForSecs(String thought, NumExpr secs) {
        this.thought = thought;
        this.secs = secs;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public NumExpr getSecs() {
        return secs;
    }

    public void setSecs(NumExpr secs) {
        this.secs = secs;
    }
}