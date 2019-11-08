package scratch.newast.parser.symboltable;

import scratch.newast.model.type.Type;
import scratch.newast.model.variable.Variable;

public class VariableInfo {

    boolean global;
    String scriptGroup;
    String variableName;
    Type type;

    public VariableInfo(boolean global, String scriptGroup, String ident,
        Type type) {
        this.global = global;
        this.scriptGroup = scriptGroup;
        this.variableName = ident;
        this.type = type;
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

    public Type getType() {
        return type;
    }
}
