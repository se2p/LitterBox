package scratch.newast.model;

import scratch.newast.model.type.Type;
import scratch.newast.model.variable.Identifier;

public class Declaration {
    private Identifier ident;
    private Type type;

    public Declaration(Identifier ident, Type type) {
        this.ident = ident;
        this.type = type;
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}