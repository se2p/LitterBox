package scratch.newast.parser.symboltable;

import scratch.newast.model.type.Type;

public class VariableInfo {

    private boolean global;
    private String actor;
    private String variableName;
    private String ident;
    private Type type;

    public VariableInfo(boolean global, String actor, String ident,
        Type type, String variableName) {
        this.global = global;
        this.actor = actor;
        this.ident = ident;
        this.type = type;
        this.variableName = variableName;
    }

    public boolean isGlobal() {
        return global;
    }

    public String getActor() {
        return actor;
    }

    public String getVariableName() {
        return variableName;
    }

    public Type getType() {
        return type;
    }

    public String getIdent() {
        return ident;
    }
}
