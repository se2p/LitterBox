package scratch.newast.parser.symboltable;

import scratch.newast.model.variable.Variable;

public class VariableInfo {

    boolean global;
    String scriptGroup;
    String variableName;
    Variable variable;

    public VariableInfo(boolean global, String scriptGroup, String ident,
        Variable variable) {
        this.global = global;
        this.scriptGroup = scriptGroup;
        this.variableName = ident;
        this.variable = variable;
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

    public Variable getVariable() {
        return variable;
    }
}
