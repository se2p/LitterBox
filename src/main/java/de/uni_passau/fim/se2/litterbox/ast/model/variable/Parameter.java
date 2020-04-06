package de.uni_passau.fim.se2.litterbox.ast.model.variable;

import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class Parameter extends  Placeholder {

    public Parameter(LocalIdentifier name) {
        super(name);
    }
    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
