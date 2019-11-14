package scratch.newast.parser.symboltable;

import scratch.newast.model.expression.list.ExpressionList;

public class ExpressionListInfo {

    private boolean global;
    private String actor;
    private String variableName;
    private ExpressionList expressionList;
    private String ident;

    public ExpressionListInfo(boolean global, String actor, String ident,
                              ExpressionList expressionList, String variableName) {
        this.global = global;
        this.actor = actor;
        this.ident = ident;
        this.expressionList = expressionList;
        this.variableName = variableName;
    }

    public boolean isGlobal() {
        return global;
    }

    public String getActor() {
        return actor;
    }

    public String getIdent() {
        return ident;
    }

    public String getVariableName() {
        return variableName;
    }

    public ExpressionList getExpressionList() {
        return expressionList;
    }
}
