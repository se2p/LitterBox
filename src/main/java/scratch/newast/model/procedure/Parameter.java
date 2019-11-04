package scratch.newast.model.procedure;

import scratch.newast.model.type.Type;
import scratch.newast.model.variable.Identifier;

public class Parameter {
    private Identifier ident;
    private Type type;

    public Parameter(Identifier ident, Type type) {
        this.ident = ident;
        this.type = type;
    }

    public Identifier getIdent() {
        return ident;
    }

    public Type getType() {
        return type;
    }
}
