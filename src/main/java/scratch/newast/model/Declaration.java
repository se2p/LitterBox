package scratch.newast.model;

import scratch.newast.model.variable.Identifier;

public class Declaration {
    private Identifier ident;
    private Class type;

    public Declaration(Identifier ident, Class type) {
        this.ident = ident;
        this.type = type;
    }

    public Identifier getIdent() {
        return ident;
    }

    public void setIdent(Identifier ident) {
        this.ident = ident;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }
}