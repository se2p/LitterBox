package scratch.newast.parser.symboltable;

import scratch.newast.model.expression.list.ExpressionList;

class ExpressionListInfo {

    boolean global;
    String scriptGroup;
    String variableName;
    ExpressionList expressionList;

    public ExpressionListInfo(boolean global, String scriptGroup, String ident,
        ExpressionList expressionList) {
        this.global = global;
        this.scriptGroup = scriptGroup;
        this.variableName = ident;
        this.expressionList = expressionList;
    }

    public boolean isGlobal() {
        return global;
    }

    public String getScriptGroup() {
        return scriptGroup;
    }

    public String getVariableName() {
        return variableName;
    }

    public ExpressionList getExpressionList() {
        return expressionList;
    }
}
