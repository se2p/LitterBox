package scratch.newast.model.statement.entitylook;

import scratch.newast.model.expression.string.StringExpr;

public class AskAndWait implements EntityLookStmt {
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