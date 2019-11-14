package scratch.newast.parser.symboltable;

import scratch.newast.model.type.Type;

public class VariableInfo {

    private boolean global;
    private String actor;
    private String variableName;
    private Type type;

    public VariableInfo(boolean global, String actor, String ident,
        Type type) {
        this.global = global;
        this.actor = actor;
        this.variableName = ident;
        this.type = type;
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
}
