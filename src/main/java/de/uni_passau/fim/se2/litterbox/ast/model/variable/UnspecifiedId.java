package de.uni_passau.fim.se2.litterbox.ast.model.variable;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class UnspecifiedId extends AbstractNode implements Variable {
    public UnspecifiedId() {
        super();
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
