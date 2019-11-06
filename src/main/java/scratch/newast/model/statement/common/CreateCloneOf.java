package scratch.newast.model.statement.common;

import scratch.newast.model.variable.Identifier;

public class CreateCloneOf implements CommonStmt {
    private Identifier identifier;

    public CreateCloneOf(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}