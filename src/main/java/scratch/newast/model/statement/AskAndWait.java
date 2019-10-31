package scratch.newast.model.statement;

import scratch.newast.model.expression.StringExpr;

public class AskAndWait extends EntityLookStmt {
    private StringExpr question;

    public AskAndWait(StringExpr question) {
        this.question = question;
    }

    public StringExpr getQuestion() {
        return question;
    }

    public void setQuestion(StringExpr question) {
        this.question = question;
    }
}